package io.opendata.db.bulk

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.TreeMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/**
 * Встроенный gpfdist: HTTP-сервер файлов для внешних таблиц Greenplum / Cloudberry
 * (`LOCATION ('gpfdist://host:port/file.csv')`), совместимый с протоколом сегментов GP6, GP7 и Cloudberry.
 * Работает на любой ОС, включая Windows, без нативного gpfdist.exe.
 *
 * Протокол (по исходникам gpfdist.c / url_curl.c):
 *  - чтение: каждый сегмент делает один GET с X-GP-XID/CID/SN/PROTO/CSVOPT; сегменты одного запроса делят одну
 *    «сессию» и забирают блоки целых строк по мере готовности (pull); блок — записи F/O/L/D, конец — `D` нулевой длины;
 *    заголовок CSV пропускает сервер (по одному на файл); завершённая сессия хранится, чтобы опоздавшие сегменты
 *    получили пустой ответ, а не повтор файла;
 *  - запись: POST с X-GP-SEQ (1 — открытие, далее данные, повтор того же SEQ — дубликат) и X-GP-DONE; файл
 *    открывается на дозапись.
 */
class GpfdistServer(
    val root: Path,
    port: Int = 0,
    private val bindAddress: InetAddress? = null,
    /** Размер блока данных, отдаваемого сегменту за раз. */
    private val blockSize: Int = 256 * 1024,
    /** Максимальная длина одной записи (gpfdist: «line too long» при превышении). */
    private val maxRecord: Int = 64 * 1024 * 1024,
) : Closeable {

    private val server = ServerSocket().apply {
        reuseAddress = true
        bind(InetSocketAddress(bindAddress, port), 128)
    }
    val port: Int get() = server.localPort

    private val pool: ExecutorService = Executors.newCachedThreadPool { r -> Thread(r, "opendata-gpfdist").apply { isDaemon = true } }
    private val readSessions = ConcurrentHashMap<String, ReadSession>()
    private val writeSessions = ConcurrentHashMap<String, WriteSession>()
    @Volatile private var closed = false

    /** Статистика для UI. */
    val bytesSent = AtomicLong()
    val bytesReceived = AtomicLong()
    val requests = AtomicLong()
    @Volatile var lastError: String? = null
        private set

    init {
        pool.execute {
            while (!closed) {
                val s = try { server.accept() } catch (e: IOException) { if (closed) break else continue }
                pool.execute { handle(s) }
            }
        }
    }

    override fun close() {
        closed = true
        runCatching { server.close() }
        writeSessions.values.forEach { runCatching { it.close() } }
        readSessions.values.forEach { runCatching { it.reader?.close() } }
        pool.shutdownNow()
    }

    // ------------------------------------------------------------------ HTTP

    private class HttpError(val code: Int, message: String) : Exception(message)

    private class Request(val method: String, val target: String, val headers: Map<String, String>) {
        fun header(name: String): String? = headers[name]?.takeIf { it.isNotEmpty() }
    }

    private fun handle(socket: Socket) {
        requests.incrementAndGet()
        socket.use { s ->
            s.tcpNoDelay = true
            s.soTimeout = 15_000
            val input = BufferedInputStream(s.getInputStream(), 64 * 1024)
            val out = BufferedOutputStream(s.getOutputStream(), 256 * 1024)
            try {
                val req = readHead(input)
                s.soTimeout = 120_000 // тело POST; на запись в сокет таймаут не влияет
                when {
                    req.method == "GET" && req.target.substringBefore('?') == "/gpfdist/status" -> status(out)
                    req.method == "GET" -> get(req, out)
                    req.method == "POST" -> post(req, input, out)
                    else -> throw HttpError(400, "invalid request")
                }
            } catch (e: HttpError) {
                lastError = "${e.code} ${e.message}"
                runCatching { error(out, e.code, e.message ?: "error") }
            } catch (e: SocketTimeoutException) {
                runCatching { error(out, 408, "time out") }
            } catch (e: IOException) {
                // Сегмент разорвал соединение (например, LIMIT в запросе) — ничего не отвечаем.
            } catch (e: Exception) {
                lastError = e.toString()
                runCatching { error(out, 500, e.message ?: e.toString()) }
            }
            runCatching { out.flush() }
            // Как gpfdist: сначала закрываем вывод, дочитываем вход, чтобы хвост потока не потерялся из-за RST.
            runCatching {
                s.shutdownOutput()
                s.soTimeout = 10_000
                val buf = ByteArray(4096)
                while (input.read(buf) >= 0) Unit
            }
        }
    }

    private fun readHead(input: InputStream): Request {
        val head = java.io.ByteArrayOutputStream()
        var last4 = 0
        while (true) {
            val b = input.read()
            if (b < 0) throw HttpError(400, "invalid request")
            head.write(b)
            if (head.size() > 64 * 1024) throw HttpError(400, "forbidden")
            last4 = (last4 shl 8) or b
            // \r\n\r\n или \n\n
            if (last4 == 0x0D0A0D0A || (last4 and 0xFFFF) == 0x0A0A) break
        }
        val lines = head.toString(Charsets.ISO_8859_1).replace("\r", "").split('\n').filter { it.isNotEmpty() }
        val parts = lines.first().split(' ').filter { it.isNotEmpty() }
        if (parts.size != 3 || !parts[2].startsWith("HTTP/1.")) throw HttpError(400, "invalid request")
        val headers = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
        lines.drop(1).forEach { l ->
            val i = l.indexOf(':')
            if (i > 0) headers[l.substring(0, i).trim()] = l.substring(i + 1).trim()
        }
        return Request(parts[0], parts[1], headers)
    }

    private fun writeHead(out: OutputStream, vararg lines: String) {
        out.write((lines.joinToString("\r\n") + "\r\n\r\n").toByteArray(Charsets.ISO_8859_1))
    }

    private fun ok(out: OutputStream, proto: Int) = writeHead(out,
        "HTTP/1.0 200 ok", "Content-type: text/plain", "Expires: 0", "X-GPFDIST-VERSION: opendata",
        // Регистр важен: сегмент сравнивает имя заголовка через strncmp("X-GP-PROTO").
        "X-GP-PROTO: $proto", "Cache-Control: no-cache", "Connection: close")

    /** Пустой ответ без X-GP-PROTO: сегмент считает его концом данных (сессия уже прочитана). */
    private fun empty(out: OutputStream) = writeHead(out,
        "HTTP/1.0 200 ok", "Content-type: text/plain", "Content-length: 0", "Expires: 0", "Cache-Control: no-cache", "Connection: close")

    private fun error(out: OutputStream, code: Int, message: String) {
        val msg = message.replace(Regex("[\\r\\n]+"), " ").filter { it.code in 32..126 }.take(500)
        writeHead(out, "HTTP/1.0 $code $msg", "Content-length: 0", "Expires: 0", "Cache-Control: no-cache", "Connection: close")
    }

    private fun status(out: OutputStream) {
        val body = "read_bytes ${bytesSent.get()}\r\nwritten_bytes ${bytesReceived.get()}\r\ntotal_sessions ${readSessions.size + writeSessions.size}\r\n"
        writeHead(out, "HTTP/1.0 200 ok", "Content-type: text/plain", "Content-length: ${body.length}", "Connection: close")
        out.write(body.toByteArray())
    }

    // ------------------------------------------------------------------ пути

    /** Путь запроса → файлы под root: %XX, несколько путей через пробел, маски * и ?, каталог → все файлы. */
    internal fun resolve(target: String, forWrite: Boolean): List<Path> {
        val decoded = java.net.URLDecoder.decode(target.substringBefore('?').replace("+", "%2B"), Charsets.UTF_8)
        if (decoded.contains("..") || decoded.contains('\\') || decoded.contains(':')) throw HttpError(400, "invalid request due to relative path")
        val base = root.toAbsolutePath().normalize()
        val result = ArrayList<Path>()
        for (part in decoded.split(' ').map { it.trimStart('/') }.filter { it.isNotEmpty() }) {
            val p = base.resolve(part).normalize()
            if (!p.startsWith(base)) throw HttpError(400, "invalid request due to relative path")
            if (part.any { it == '*' || it == '?' || it == '[' }) {
                val dir = p.parent ?: base
                val matcher = FileSystems.getDefault().getPathMatcher("glob:" + p.fileName.toString())
                if (Files.isDirectory(dir)) Files.list(dir).use { s -> s.filter { matcher.matches(it.fileName) && Files.isRegularFile(it) }.sorted().forEach { result.add(it) } }
            } else if (!forWrite && Files.isDirectory(p)) {
                Files.list(p).use { s -> s.filter { Files.isRegularFile(it) }.sorted().forEach { result.add(it) } }
            } else result.add(p)
        }
        if (forWrite) {
            if (result.size != 1) throw HttpError(404, "More than 1 file found for writing. Unsupported operation.")
            return result
        }
        if (result.isEmpty()) throw HttpError(404, "No matching file(s) found")
        result.firstOrNull { !Files.isRegularFile(it) || !Files.isReadable(it) }?.let { throw HttpError(404, "file open failure ${root.relativize(it)}") }
        return result
    }

    private fun relativeName(p: Path): String = root.toAbsolutePath().normalize().relativize(p).toString().replace('\\', '/').take(255)

    // ------------------------------------------------------------------ чтение (GET)

    /** Параметры формата из X-GP-CSVOPT: "m%1dx%3dq%3dn%1dh%1d" (поля десятичные, с пробелами). */
    internal data class CsvOpt(val csv: Boolean, val escape: Int, val quote: Int, val eol: Int, val header: Boolean) {
        companion object {
            private val MAIN = Regex("""m\s*(\d+)x\s*(\d+)q\s*(\d+)n\s*(\d+)h\s*(\d+)""")
            private val GP4 = Regex("""m\s*(\d+)x\s*(\d+)q\s*(\d+)h\s*(\d+)n\s*(\d+)""")
            fun parse(s: String?): CsvOpt {
                if (s.isNullOrBlank()) return CsvOpt(false, 0, 0, 0, false)
                MAIN.find(s)?.destructured?.let { (m, x, q, n, h) -> return CsvOpt(m == "1", x.toInt(), q.toInt(), n.toInt(), h == "1") }
                GP4.find(s)?.destructured?.let { (m, x, q, h, n) -> return CsvOpt(m == "1", x.toInt(), q.toInt(), n.toInt(), h == "1") }
                throw HttpError(400, "invalid request (csvopt doesn't match the format)")
            }
        }
    }

    private enum class State { ACTIVE, EOF, ERROR }

    private inner class ReadSession(val reader: BlockReader?) {
        @Volatile var state = State.ACTIVE
        @Volatile var errorMessage = ""
        @Volatile var touched = System.currentTimeMillis()
        val isGet = true
    }

    private fun get(req: Request, out: OutputStream) {
        val proto = req.header("X-GP-PROTO")?.trim()?.toIntOrNull() ?: throw HttpError(400, "invalid request (no gp-proto)")
        if (proto != 0 && proto != 1) throw HttpError(400, "invalid request (invalid gp-proto)")
        if (req.header("X-GP-TRANSFORM") != null) throw HttpError(400, "invalid request (unsupported input #transform)")
        val ids = listOf("X-GP-XID", "X-GP-CID", "X-GP-SN").map { req.header(it) }
        if (ids.count { it != null } in 1..2) throw HttpError(400, "invalid request (missing X-GP-* header)")
        val opt = CsvOpt.parse(req.header("X-GP-CSVOPT"))
        val delim = lineDelimiter(req)
        val files = resolve(req.target, forWrite = false)
        val key = if (ids[0] == null) "auto.${System.nanoTime()}" else "${ids[0]}.${ids[1]}.${ids[2]}.$proto:" + files.joinToString(" ")
        if (writeSessions.containsKey(key)) throw HttpError(400, "can't write to and read from the same gpfdist server simultaneously")
        cleanup()
        val session = synchronized(readSessions) {
            readSessions.getOrPut(key) { ReadSession(BlockReader(files, opt, delim)) }
        }
        session.touched = System.currentTimeMillis()
        when (session.state) {
            State.ERROR -> throw HttpError(500, session.errorMessage)
            State.EOF -> { empty(out); return }
            State.ACTIVE -> {}
        }
        ok(out, proto)
        try {
            while (true) {
                val block: Block? = try {
                    synchronized(session) {
                        if (session.state != State.ACTIVE) null
                        else session.reader!!.next().also { if (it == null) { session.state = State.EOF; session.reader.close() } }
                    }
                } catch (e: Exception) {
                    synchronized(session) {
                        session.state = State.ERROR
                        session.errorMessage = e.message ?: e.toString()
                        session.reader?.close()
                    }
                    if (proto == 1) message(out, 'E'.code, (e.message ?: e.toString()).toByteArray())
                    return
                }
                if (block == null) break
                if (proto == 1) {
                    message(out, 'F'.code, block.file.toByteArray())
                    message(out, 'O'.code, be64(block.offset))
                    message(out, 'L'.code, be64(block.line))
                    message(out, 'D'.code, block.data, block.length)
                } else out.write(block.data, 0, block.length)
                bytesSent.addAndGet(block.length.toLong())
            }
            if (proto == 1) message(out, 'D'.code, ByteArray(0))
        } catch (e: IOException) {
            // Как gpfdist: обрыв соединения сегментом завершает сессию (например, LIMIT).
            synchronized(session) { if (session.state == State.ACTIVE) { session.state = State.EOF; session.reader?.close() } }
            throw e
        } finally {
            session.touched = System.currentTimeMillis()
        }
    }

    private fun lineDelimiter(req: Request): ByteArray {
        val hex = req.header("X-GP-LINE-DELIM-STR") ?: return ByteArray(0)
        if (hex.length % 2 != 0 || !hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) throw HttpError(400, "invalid EOL encoding")
        return ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    private fun message(out: OutputStream, type: Int, payload: ByteArray, length: Int = payload.size) {
        out.write(type)
        out.write(be32(length))
        out.write(payload, 0, length)
    }

    private fun be32(v: Int) = byteArrayOf((v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte())
    private fun be64(v: Long) = ByteArray(8) { (v ushr (56 - 8 * it)).toByte() }

    /** Завершённые сессии хранятся 5 минут (как gpfdist), чтобы опоздавший сегмент не прочитал файл повторно. */
    private fun cleanup() {
        val now = System.currentTimeMillis()
        readSessions.entries.removeIf { (_, s) -> s.state != State.ACTIVE && now - s.touched > 300_000 }
    }

    internal class Block(val file: String, val offset: Long, val line: Long, val data: ByteArray, val length: Int)

    /**
     * Общий поток файлов сессии: блоки целых записей (как fstream_read): остаток неполной записи переносится в
     * следующий блок, блок не пересекает границу файла, CSV режется с учётом кавычек и экранирования, заголовок
     * пропускается в каждом файле.
     */
    internal inner class BlockReader(private val files: List<Path>, private val opt: CsvOpt, private val delim: ByteArray) : Closeable {
        private var fidx = -1
        private var stream: InputStream? = null
        private var name = ""
        private var foff = 0L
        private var line = 1L
        private var skipHeader = false
        private var fileEof = false
        private var buf = ByteArray(blockSize)
        private var len = 0 // остаток с прошлого блока в начале buf

        private fun openNext(): Boolean {
            stream?.close(); stream = null
            fidx++
            if (fidx >= files.size) return false
            stream = BufferedInputStream(Files.newInputStream(files[fidx]), 1 shl 20)
            name = relativeName(files[fidx])
            foff = 0; line = 1; skipHeader = opt.header; fileEof = false; len = 0
            return true
        }

        /** Дочитывает buf до capacity; false — файл закончился. */
        private fun fill(capacity: Int): Boolean {
            if (buf.size < capacity) buf = buf.copyOf(capacity)
            while (len < capacity) {
                val n = stream!!.read(buf, len, capacity - len)
                if (n < 0) { fileEof = true; return false }
                len += n
            }
            return true
        }

        fun next(): Block? {
            while (true) {
                if (stream == null && !openNext()) return null
                if (skipHeader) {
                    var cap = blockSize
                    while (true) {
                        val full = fill(cap)
                        val end = firstRecordEnd()
                        if (end >= 0) { consume(end); line = 2; break }
                        if (!full) { foff += len; len = 0; break } // файл без конца строки — всё содержимое считается заголовком
                        if (cap >= maxRecord) throw IOException("line too long in file $name near ($foff bytes)")
                        cap = minOf(cap * 2, maxRecord)
                    }
                    skipHeader = false
                }
                val startOff = foff
                val startLine = line
                var cap = blockSize
                while (true) {
                    val full = !fileEof && fill(cap)
                    if (!full) {
                        if (len == 0) { stream?.close(); stream = null; break } // к следующему файлу
                        val data = buf.copyOf(len)
                        if (opt.csv) line += countRecords(data, len)
                        foff += len
                        val b = Block(name, startOff, if (opt.csv || startLine == 1L) startLine else 0, data, len)
                        len = 0
                        stream?.close(); stream = null
                        return b
                    }
                    val end = lastRecordEnd()
                    if (end > 0) {
                        val data = buf.copyOf(end)
                        if (opt.csv) line += countRecords(data, end) else line = 0
                        consume(end)
                        return Block(name, startOff, if (opt.csv || startLine == 1L) startLine else 0, data, end)
                    }
                    if (cap >= maxRecord) throw IOException("line too long in file $name near ($foff bytes)")
                    cap = minOf(cap * 2, maxRecord)
                }
            }
        }

        private fun consume(n: Int) {
            System.arraycopy(buf, n, buf, 0, len - n)
            len -= n
            foff += n
        }

        private fun firstRecordEnd(): Int = if (opt.csv) scanCsv(buf, len, first = true) else {
            if (delim.isNotEmpty()) indexOf(buf, len, delim, last = false).let { if (it < 0) -1 else it + delim.size }
            else (0 until len).firstOrNull { buf[it] == '\n'.code.toByte() }?.plus(1) ?: -1
        }

        private fun lastRecordEnd(): Int = if (opt.csv) scanCsv(buf, len, first = false) else {
            if (delim.isNotEmpty()) indexOf(buf, len, delim, last = true).let { if (it < 0) -1 else it + delim.size }
            else (len - 1 downTo 0).firstOrNull { buf[it] == '\n'.code.toByte() }?.plus(1) ?: -1
        }

        private fun countRecords(data: ByteArray, n: Int): Long {
            var count = 0L
            scanCsv(data, n, first = false) { count++ }
            return count
        }

        /** scan_csv_records: конец первой/последней полной записи с учётом кавычек; −1 — записи нет. */
        private fun scanCsv(data: ByteArray, n: Int, first: Boolean, onRecord: (() -> Unit)? = null): Int {
            val quote = opt.quote
            val escape = opt.escape
            var inQuote = false
            var lastWasEsc = false
            var lastEnd = -1
            for (i in 0 until n) {
                val ch = data[i].toInt() and 0xFF
                if (inQuote) {
                    if (!lastWasEsc) {
                        if (ch == quote) inQuote = false else if (ch == escape) lastWasEsc = true
                    } else lastWasEsc = false
                } else if (isRecordEnd(data, i, ch)) {
                    lastEnd = i + 1
                    onRecord?.invoke()
                    if (first) return lastEnd
                } else if (ch == quote) inQuote = true
            }
            return lastEnd
        }

        private fun isRecordEnd(data: ByteArray, i: Int, ch: Int): Boolean = when (opt.eol) {
            3 -> ch == '\n'.code && i > 0 && data[i - 1] == '\r'.code.toByte()
            2 -> ch == '\r'.code
            else -> ch == '\n'.code
        }

        private fun indexOf(data: ByteArray, n: Int, pattern: ByteArray, last: Boolean): Int {
            val range = if (last) (n - pattern.size downTo 0) else (0..n - pattern.size)
            for (i in range) if ((pattern.indices).all { data[i + it] == pattern[it] }) return i
            return -1
        }

        override fun close() {
            runCatching { stream?.close() }
            stream = null
        }
    }

    // ------------------------------------------------------------------ запись (POST)

    private inner class WriteSession(val file: Path) : Closeable {
        private val out = Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)
        val lastSeq = HashMap<Int, Long>()
        val active = HashSet<Int>()
        var inFlight = 0

        fun write(data: ByteArray) {
            out.write(data)
            out.flush()
        }

        override fun close() = out.close()
    }

    private fun post(req: Request, input: InputStream, out: OutputStream) {
        val proto = req.header("X-GP-PROTO")?.trim()?.toIntOrNull() ?: throw HttpError(400, "invalid request (no gp-proto)")
        if (proto != 0 && proto != 1) throw HttpError(400, "invalid request (invalid gp-proto)")
        val ids = listOf("X-GP-XID", "X-GP-CID", "X-GP-SN").map { req.header(it) }
        if (ids.count { it != null } in 1..2) throw HttpError(400, "invalid request (missing X-GP-* header)")
        val seq = req.header("X-GP-SEQ")?.trim()?.toLongOrNull() ?: 0L
        if (req.header("X-GP-SEQ") != null && seq <= 0) throw HttpError(400, "invalid sequence number")
        val done = req.header("X-GP-DONE") != null
        val segid = req.header("X-GP-SEGMENT-ID")?.trim()?.toIntOrNull() ?: 0
        val length = req.header("Content-Length")?.trim()?.toLongOrNull() ?: 0L
        if (length < 0 || length > (1L shl 30)) throw HttpError(400, "invalid Content-Length")
        if (req.header("Expect")?.equals("100-continue", ignoreCase = true) == true) {
            out.write("HTTP/1.1 100 Continue\r\n\r\n".toByteArray()); out.flush()
        }
        val body = input.readNBytes(length.toInt())
        if (body.size.toLong() != length) throw HttpError(408, "failed to receive the request body")
        bytesReceived.addAndGet(length)

        val file = resolve(req.target, forWrite = true).single()
        val key = "${ids[0]}.${ids[1]}.${ids[2]}.$proto:$file"
        if (readSessions.containsKey(key)) throw HttpError(400, "can't write to and read from the same gpfdist server simultaneously")
        val session = synchronized(writeSessions) {
            if (done && !writeSessions.containsKey(key)) { empty(out); return }
            writeSessions.getOrPut(key) {
                file.parent?.let { Files.createDirectories(it) }
                WriteSession(file)
            }.also { it.inFlight++ }
        }
        try {
            synchronized(session) {
                if (done) session.active.remove(segid) else session.active.add(segid)
                val last = session.lastSeq[segid] ?: 0L
                when {
                    seq == 1L -> session.lastSeq[segid] = 1L
                    seq == 0L && last > 0 -> throw HttpError(400, "invalid request due to missing sequence number")
                    seq != 0L && seq == last -> {} // повтор после потерянного ответа — уже записано
                    seq != 0L && seq != last + 1 && !done -> throw HttpError(400, "invalid request due to wrong sequence number")
                    else -> {
                        if (body.isNotEmpty()) session.write(body)
                        if (seq > 0) session.lastSeq[segid] = seq
                    }
                }
            }
            writeHead(out, "HTTP/1.0 200 ok", "Content-type: text/plain", "Expires: 0", "X-GP-PROTO: 0", "Cache-Control: no-cache", "Connection: close")
        } finally {
            synchronized(writeSessions) {
                session.inFlight--
                if (session.inFlight == 0 && session.active.isEmpty()) {
                    writeSessions.remove(key)
                    session.close()
                }
            }
        }
    }

    companion object {
        /** Внешний URL файла для LOCATION: gpfdist://host:port/относительный/путь */
        fun location(host: String, port: Int, relative: String): String {
            val h = if (host.contains(':') && !host.startsWith("[")) "[$host]" else host
            val path = relative.replace('\\', '/').trimStart('/').split('/').joinToString("/") {
                java.net.URLEncoder.encode(it, Charsets.UTF_8).replace("+", "%20")
            }
            return "gpfdist://$h:$port/$path"
        }
    }
}
