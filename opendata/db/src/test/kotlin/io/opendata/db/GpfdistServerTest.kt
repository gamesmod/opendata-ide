package io.opendata.db

import io.opendata.db.bulk.GpfdistServer
import io.opendata.db.data.CsvReader
import junit.framework.TestCase
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.StringReader
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Встроенный gpfdist против «сегментов», которые ведут себя как url_curl.c: один GET на сегмент с X-GP-XID/CID/SN,
 * протокол 1 (F/O/L/D, конец — D нулевой длины), общий поток блоков на сессию, POST с X-GP-SEQ/X-GP-DONE.
 */
class GpfdistServerTest : TestCase() {
    private lateinit var dir: Path

    override fun setUp() {
        dir = Files.createTempDirectory("gpfdist")
    }

    override fun tearDown() {
        dir.toFile().deleteRecursively()
    }

    class Response(val status: String, val headers: Map<String, String>, val rawHeaders: String, val data: List<ByteArray>, val error: String?, val eof: Boolean)

    private fun request(port: Int, method: String, path: String, headers: Map<String, String>, body: ByteArray = ByteArray(0)): Response =
        Socket("127.0.0.1", port).use { s ->
            s.soTimeout = 30_000
            val head = buildString {
                append("$method $path HTTP/1.1\r\nHost: localhost\r\n")
                headers.forEach { (k, v) -> append("$k: $v\r\n") }
                if (method == "POST") append("Content-Length: ${body.size}\r\n")
                append("\r\n")
            }
            s.getOutputStream().write(head.toByteArray()); s.getOutputStream().write(body); s.getOutputStream().flush()
            val input = DataInputStream(s.getInputStream().buffered())
            val raw = readHead(input)
            val lines = raw.split("\r\n").filter { it.isNotEmpty() }
            val hdrs = lines.drop(1).associate { it.substringBefore(':') to it.substringAfter(':').trim() }
            val data = ArrayList<ByteArray>()
            var error: String? = null
            var eof = false
            // Сегмент выбирает протокол по заголовку X-GP-PROTO (с учётом регистра, как strncmp в url_curl.c).
            if (method == "GET" && lines.first().contains(" 200 ") && raw.contains("\r\nX-GP-PROTO: 1\r\n")) {
                while (true) {
                    val type = input.read()
                    if (type < 0) break
                    val len = input.readInt()
                    val payload = ByteArray(len).also { input.readFully(it) }
                    when (type.toChar()) {
                        'F' -> assertTrue(len in 1..256)
                        'O', 'L' -> assertEquals(8, len)
                        'D' -> if (len == 0) { eof = true } else data += payload
                        'E' -> error = String(payload)
                        else -> fail("unknown frame $type")
                    }
                    if (eof || error != null) break
                }
            } else data += input.readAllBytes()
            Response(lines.first(), hdrs, raw, data, error, eof)
        }

    private fun readHead(input: InputStream): String {
        val b = ByteArrayOutputStream()
        while (!b.toString(Charsets.ISO_8859_1).endsWith("\r\n\r\n")) {
            val c = input.read(); if (c < 0) break; b.write(c)
        }
        return b.toString(Charsets.ISO_8859_1)
    }

    private fun seg(xid: String, segId: Int, count: Int, csvopt: String) = mapOf(
        "X-GP-PROTO" to "1", "X-GP-XID" to xid, "X-GP-CID" to "1", "X-GP-SN" to "0",
        "X-GP-SEGMENT-ID" to segId.toString(), "X-GP-SEGMENT-COUNT" to count.toString(), "X-GP-CSVOPT" to csvopt,
        "X-GP-LINE-DELIM-LENGTH" to "-1",
    )

    private fun bigCsv(rows: Int): String = buildString {
        append("id,name,note\r\n".replace("\r", ""))
        for (i in 1..rows) {
            append(i).append(',')
            append(if (i % 7 == 0) "\"quoted, with comma\"" else "name$i").append(',')
            // Каждая 13-я запись содержит перевод строки и удвоенную кавычку внутри кавычек.
            append(if (i % 13 == 0) "\"line1\nline2 \"\"q\"\"\"" else "n$i").append('\n')
        }
    }

    /** Четыре сегмента одного запроса: каждая запись ровно один раз, блоки — только целые записи, заголовок пропущен. */
    fun testParallelSegmentsGetWholeRecordsExactlyOnce() {
        val rows = 20_000
        Files.writeString(dir.resolve("data.csv"), bigCsv(rows))
        GpfdistServer(dir, blockSize = 4096).use { srv ->
            val pool = Executors.newFixedThreadPool(4)
            val responses = (0 until 4).map { id ->
                pool.submit(Callable { request(srv.port, "GET", "/data.csv", seg("100", id, 4, "m1x 34q 34n0h1")) })
            }.map { it.get() }
            pool.shutdown()
            responses.forEach { r ->
                assertEquals("HTTP/1.0 200 ok", r.status)
                assertTrue("X-GP-PROTO в точном регистре", r.rawHeaders.contains("\r\nX-GP-PROTO: 1\r\n"))
                assertFalse(r.rawHeaders.contains("X-GP-ZSTD"))
                assertTrue("каждый поток завершается D0", r.eof)
                assertNull(r.error)
            }
            val ids = ArrayList<Int>()
            responses.flatMap { it.data }.forEach { block ->
                // Каждый блок самостоятельно разбирается в целые записи.
                CsvReader(StringReader(String(block)), ',').use { csv ->
                    while (true) {
                        val rec = csv.next() ?: break
                        assertEquals("запись разрезана между блоками: $rec", 3, rec.size)
                        ids += rec[0]!!.toInt()
                        if (rec[0]!!.toInt() % 13 == 0) assertEquals("line1\nline2 \"q\"", rec[2])
                    }
                }
            }
            assertEquals((1..rows).toList(), ids.sorted())
            assertTrue("данные распределены между сегментами", responses.count { it.data.isNotEmpty() } >= 2)

            // Опоздавший сегмент той же сессии получает пустой ответ, а не повтор файла.
            val late = request(srv.port, "GET", "/data.csv", seg("100", 3, 4, "m1x 34q 34n0h1"))
            assertTrue(late.rawHeaders.contains("Content-length: 0"))
            assertFalse(late.rawHeaders.contains("X-GP-PROTO"))
            assertEquals(0, late.data.sumOf { it.size })

            // Новый запрос (другой XID) читает файл заново.
            assertEquals(rows, request(srv.port, "GET", "/data.csv", seg("101", 0, 1, "m1x 34q 34n0h1")).data.sumOf { b -> String(b).count { it == '\n' } } - rows / 13)
        }
    }

    /** TEXT, без заголовка, несколько файлов по маске; блок не пересекает границу файла; файл без \n в конце. */
    fun testTextFormatGlobAndLastLineWithoutNewline() {
        Files.writeString(dir.resolve("a_1.txt"), "1\tone\n2\ttwo\n")
        Files.writeString(dir.resolve("a_2.txt"), "3\tthree\n4\tfour")
        Files.writeString(dir.resolve("b.txt"), "x\n")
        GpfdistServer(dir, blockSize = 4096).use { srv ->
            val r = request(srv.port, "GET", "/a_*.txt", seg("200", 0, 1, "m0x 92q  0n0h0"))
            assertTrue(r.eof)
            assertEquals(listOf("1\tone\n2\ttwo\n", "3\tthree\n4\tfour"), r.data.map { String(it) })
        }
    }

    fun testErrors() {
        Files.writeString(dir.resolve("long.csv"), "a,b\n" + "x".repeat(10_000) + ",1\n")
        GpfdistServer(dir, blockSize = 1024, maxRecord = 4096).use { srv ->
            assertTrue(request(srv.port, "GET", "/long.csv", seg("1", 0, 1, "m1x 34q 34n0h1").minus("X-GP-PROTO")).status.startsWith("HTTP/1.0 400"))
            assertTrue(request(srv.port, "GET", "/../etc/passwd", seg("2", 0, 1, "m1x 34q 34n0h1")).status.startsWith("HTTP/1.0 400"))
            assertTrue(request(srv.port, "GET", "/missing.csv", seg("3", 0, 1, "m1x 34q 34n0h1")).status.startsWith("HTTP/1.0 404"))
            assertTrue(request(srv.port, "GET", "/long.csv", seg("4", 0, 1, "garbage")).status.startsWith("HTTP/1.0 400"))
            val tooLong = request(srv.port, "GET", "/long.csv", seg("5", 0, 1, "m1x 34q 34n0h1"))
            assertNotNull("строка длиннее предела — сообщение E", tooLong.error)
            assertTrue(tooLong.error!!, tooLong.error!!.contains("line too long"))
            // Сессия в ошибке: следующий сегмент получает 500.
            assertTrue(request(srv.port, "GET", "/long.csv", seg("5", 1, 2, "m1x 34q 34n0h1")).status.startsWith("HTTP/1.0 500"))
        }
    }

    /** Запись (writable external table): SEQ 1 — открытие, повтор SEQ не дублирует данные, пропуск SEQ — 400, DONE закрывает. */
    fun testWritableSequence() {
        GpfdistServer(dir).use { srv ->
            fun post(seg: Int, seq: Int?, body: String = "", done: Boolean = false): String {
                val h = linkedMapOf("X-GP-PROTO" to "0", "X-GP-XID" to "900", "X-GP-CID" to "1", "X-GP-SN" to "0",
                    "X-GP-SEGMENT-ID" to seg.toString(), "X-GP-SEGMENT-COUNT" to "2")
                seq?.let { h["X-GP-SEQ"] = it.toString() }
                if (done) h["X-GP-DONE"] = "1"
                val r = request(srv.port, "POST", "/out/result.csv", h, body.toByteArray())
                if (r.status.contains(" 200 ")) assertTrue(r.rawHeaders.contains("X-GP-PROTO: 0"))
                return r.status
            }
            assertTrue(post(0, 1).contains(" 200 "))
            assertTrue(post(1, 1).contains(" 200 "))
            assertTrue(post(0, 2, "1,a\n2,b\n").contains(" 200 "))
            assertTrue(post(0, 2, "1,a\n2,b\n").contains(" 200 ")) // повтор после потерянного ответа
            assertTrue(post(1, 2, "3,c\n").contains(" 200 "))
            assertTrue(post(1, 5, "9,z\n").startsWith("HTTP/1.0 400"))
            assertTrue(post(0, 2, done = true).contains(" 200 "))
            assertTrue(post(1, 2, done = true).contains(" 200 "))
            val lines = Files.readAllLines(dir.resolve("out/result.csv")).sorted()
            assertEquals(listOf("1,a", "2,b", "3,c"), lines)
        }
    }

    /** Файл с пробелом и скобками в имени публикуется под служебным именем (gpfdist делит путь по пробелам). */
    fun testPublishedAlias() {
        val f = dir.resolve("my data (1).csv")
        Files.writeString(f, "a,b\n1,2\n")
        GpfdistServer(dir).use { srv ->
            val name = srv.publish(f)
            assertTrue(name, name.matches(Regex("opendata-[0-9a-f]{16}\\.csv")))
            val r = request(srv.port, "GET", "/$name", seg("700", 0, 1, "m1x 34q 34n0h1"))
            assertEquals(listOf("1,2\n"), r.data.map { String(it) })
        }
    }

    fun testLocationUrl() {
        assertEquals("gpfdist://10.0.0.5:8081/dir/my%20file.csv", GpfdistServer.location("10.0.0.5", 8081, "dir\\my file.csv"))
        assertEquals("gpfdist://[fe80::1]:8081/a.csv", GpfdistServer.location("fe80::1", 8081, "a.csv"))
    }
}
