package io.opendata.db.session

/** Фрагмент скрипта: одно SQL-выражение и его границы в исходном тексте. */
data class SqlChunk(val text: String, val start: Int, val end: Int)

/**
 * Разбивает скрипт на выражения по ';' с учётом строк '...', идентификаторов "..." и `...`,
 * комментариев -- и /* */, а также dollar-quoting PostgreSQL ($$...$$, $tag$...$tag$).
 */
object SqlSplitter {

    fun split(text: String): List<SqlChunk> {
        val result = ArrayList<SqlChunk>()
        var stmtStart = 0
        var i = 0
        val n = text.length
        while (i < n) {
            val c = text[i]
            when {
                c == '-' && i + 1 < n && text[i + 1] == '-' -> i = text.indexOf('\n', i).let { if (it < 0) n else it }
                c == '/' && i + 1 < n && text[i + 1] == '*' -> i = text.indexOf("*/", i + 2).let { if (it < 0) n else it + 2 }
                c == '\'' || c == '"' || c == '`' -> i = skipQuoted(text, i, c)
                c == '$' -> {
                    val tag = dollarTag(text, i)
                    i = if (tag != null) text.indexOf(tag, i + tag.length).let { if (it < 0) n else it + tag.length } else i + 1
                }
                c == ';' -> {
                    addChunk(text, stmtStart, i, result)
                    stmtStart = i + 1
                    i++
                }
                else -> i++
            }
        }
        addChunk(text, stmtStart, n, result)
        return result
    }

    /** Выражение под кареткой (или ближайшее предыдущее, если каретка стоит после ';'). */
    fun statementAt(text: String, offset: Int): SqlChunk? {
        val chunks = split(text)
        return chunks.firstOrNull { offset >= it.start && offset <= it.end }
            ?: chunks.lastOrNull { it.end <= offset }
            ?: chunks.firstOrNull()
    }

    private fun addChunk(text: String, from: Int, to: Int, out: MutableList<SqlChunk>) {
        var s = from
        var e = to
        while (s < e && text[s].isWhitespace()) s++
        while (e > s && text[e - 1].isWhitespace()) e--
        if (e > s && !isOnlyComments(text.substring(s, e))) out += SqlChunk(text.substring(s, e), s, e)
    }

    private fun isOnlyComments(sql: String): Boolean {
        var i = 0
        while (i < sql.length) {
            val c = sql[i]
            when {
                c.isWhitespace() -> i++
                sql.startsWith("--", i) -> i = sql.indexOf('\n', i).let { if (it < 0) sql.length else it }
                sql.startsWith("/*", i) -> i = sql.indexOf("*/", i + 2).let { if (it < 0) sql.length else it + 2 }
                else -> return false
            }
        }
        return true
    }

    private fun skipQuoted(text: String, start: Int, quote: Char): Int {
        var i = start + 1
        while (i < text.length) {
            val c = text[i]
            if (c == '\\' && quote == '\'' ) { i += 2; continue } // MySQL/ClickHouse-style escapes
            if (c == quote) {
                if (i + 1 < text.length && text[i + 1] == quote) { i += 2; continue } // '' внутри строки
                return i + 1
            }
            i++
        }
        return text.length
    }

    /** "$tag$" или "$$", если в позиции start начинается dollar-quote; иначе null ($1 — параметр). */
    private fun dollarTag(text: String, start: Int): String? {
        var i = start + 1
        while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
        if (i >= text.length || text[i] != '$') return null
        val tag = text.substring(start, i + 1)
        if (tag.length > 2 && tag[1].isDigit()) return null
        return tag
    }
}
