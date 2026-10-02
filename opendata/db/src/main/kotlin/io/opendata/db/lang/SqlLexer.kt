package io.opendata.db.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/** Ручной лексер SQL: PostgreSQL/Cloudberry, ClickHouse, Dremio. Без состояния между токенами. */
class SqlLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var tokenType: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.endOffset = endOffset
        this.tokenEnd = startOffset
        advance()
    }

    override fun getState(): Int = 0
    override fun getTokenType(): IElementType? = tokenType
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= endOffset) {
            tokenType = null
            return
        }
        var i = tokenStart
        val c = buffer[i]
        tokenType = when {
            c.isWhitespace() -> {
                while (i < endOffset && buffer[i].isWhitespace()) i++
                TokenType.WHITE_SPACE
            }
            c == '-' && peek(i + 1) == '-' -> {
                while (i < endOffset && buffer[i] != '\n') i++
                SqlTokens.LINE_COMMENT
            }
            c == '/' && peek(i + 1) == '*' -> {
                i += 2
                while (i < endOffset && !(buffer[i] == '*' && peek(i + 1) == '/')) i++
                i = minOf(endOffset, i + 2)
                SqlTokens.BLOCK_COMMENT
            }
            c == '\'' || ((c == 'E' || c == 'e' || c == 'N' || c == 'n') && peek(i + 1) == '\'') -> {
                if (c != '\'') i++
                i = quoted(i, '\'')
                SqlTokens.STRING
            }
            c == '"' || c == '`' -> {
                i = quoted(i, c)
                SqlTokens.QUOTED_IDENTIFIER
            }
            c == '$' -> {
                val tagEnd = dollarTagEnd(i)
                if (tagEnd > 0) {
                    val tag = buffer.subSequence(i, tagEnd).toString()
                    val close = indexOf(tag, tagEnd)
                    i = if (close < 0) endOffset else close + tag.length
                    SqlTokens.STRING
                } else {
                    i++
                    while (i < endOffset && buffer[i].isDigit()) i++
                    SqlTokens.PARAMETER
                }
            }
            c.isDigit() || (c == '.' && peek(i + 1)?.isDigit() == true) -> {
                while (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '.' || buffer[i] == '_')) {
                    if ((buffer[i] == 'e' || buffer[i] == 'E') && (peek(i + 1) == '+' || peek(i + 1) == '-')) i++
                    i++
                }
                SqlTokens.NUMBER
            }
            c.isLetter() || c == '_' -> {
                while (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '_' || buffer[i] == '$')) i++
                if (SqlKeywords.isKeyword(buffer.subSequence(tokenStart, i))) SqlTokens.KEYWORD else SqlTokens.IDENTIFIER
            }
            c == ':' && peek(i + 1)?.let { it.isLetter() || it == '_' } == true -> {
                i++
                while (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '_')) i++
                SqlTokens.PARAMETER
            }
            c == '?' -> { i++; SqlTokens.PARAMETER }
            c == ';' -> { i++; SqlTokens.SEMICOLON }
            c == ',' -> { i++; SqlTokens.COMMA }
            c == '.' -> { i++; SqlTokens.DOT }
            c == '(' -> { i++; SqlTokens.LPAREN }
            c == ')' -> { i++; SqlTokens.RPAREN }
            else -> {
                i++
                while (i < endOffset && buffer[i] in "<>=!|&+-*/%^~:#@" && !(buffer[i] == '-' && peek(i + 1) == '-')) i++
                SqlTokens.OPERATOR
            }
        }
        tokenEnd = maxOf(i, tokenStart + 1).coerceAtMost(endOffset)
    }

    private fun peek(i: Int): Char? = if (i < endOffset) buffer[i] else null

    private fun quoted(start: Int, q: Char): Int {
        var i = start + 1
        while (i < endOffset) {
            val ch = buffer[i]
            if (ch == '\\' && q == '\'') { i += 2; continue }
            if (ch == q) {
                if (peek(i + 1) == q) { i += 2; continue }
                return i + 1
            }
            i++
        }
        return endOffset
    }

    private fun dollarTagEnd(start: Int): Int {
        var i = start + 1
        if (i < endOffset && buffer[i].isDigit()) return -1
        while (i < endOffset && (buffer[i].isLetterOrDigit() || buffer[i] == '_')) i++
        return if (i < endOffset && buffer[i] == '$') i + 1 else -1
    }

    private fun indexOf(s: String, from: Int): Int {
        var i = from
        while (i + s.length <= endOffset) {
            if (buffer[i] == s[0] && buffer.subSequence(i, i + s.length).toString() == s) return i
            i++
        }
        return -1
    }
}

object SqlKeywords {
    val ALL: Set<String> = """
        ABORT ADD ALL ALTER ANALYZE AND ANY ARRAY AS ASC ASOF ATTACH BEGIN BETWEEN BIGINT BOOLEAN BOTH BY CALL CASCADE CASE CAST
        CHAR CHARACTER CHECK CLUSTER COALESCE COLLATE COLUMN COMMENT COMMIT CONCURRENTLY CONFLICT CONSTRAINT COPY CREATE CROSS CUBE
        CURRENT_DATE CURRENT_TIME CURRENT_TIMESTAMP CURRENT_USER DATABASE DATE DAY DECIMAL DECLARE DEFAULT DEFERRABLE DELETE DESC
        DESCRIBE DETACH DICTIONARY DISTINCT DISTRIBUTED DO DOUBLE DROP ELSE END ENGINE ENUM ESCAPE EXCEPT EXCLUDE EXECUTE EXISTS EXPLAIN
        EXTENSION EXTRACT FALSE FETCH FILTER FINAL FIRST FLOAT FOLLOWING FOR FOREIGN FORMAT FROM FULL FUNCTION GLOBAL GRANT GROUP
        GROUPING HAVING IF ILIKE IN INDEX INHERITS INNER INSERT INT INTEGER INTERSECT INTERVAL INTO IS ISNULL JOIN JSON JSONB KEY
        LANGUAGE LAST LATERAL LEADING LEFT LIKE LIMIT LOCAL LOCK MATERIALIZED MERGE MINUS MONTH NATURAL NOT NOTHING NOTNULL NULL NULLS
        NUMERIC OF OFFSET ON ONLY OPTIMIZE OR ORDER OUTER OVER OVERLAPS PARTITION PARTITIONED PRECEDING PREWHERE PRIMARY PROCEDURE
        RANDOMLY RANGE REAL RECURSIVE REFERENCES REFRESH RENAME REPLACE REPLICATED RESTRICT RETURNING RETURNS REVOKE RIGHT ROLE ROLLBACK
        ROLLUP ROW ROWS SAMPLE SAVEPOINT SCHEMA SELECT SEQUENCE SERIAL BIGSERIAL SET SETTINGS SHOW SIMILAR SMALLINT SOME SYSTEM TABLE
        TABLESAMPLE TEMP TEMPORARY TEXT THEN TIME TIMESTAMP TIMESTAMPTZ TO TOP TOTALS TRAILING TRANSACTION TRIGGER TRUE TRUNCATE TTL TYPE
        UNBOUNDED UNION UNIQUE UNNEST UPDATE USE USING VACUUM VALUES VARCHAR VARIADIC VIEW VOLATILE WHEN WHERE WINDOW WITH WITHIN
        WITHOUT WORK YEAR ZONE
    """.trim().split(Regex("\\s+")).toSet()

    fun isKeyword(text: CharSequence): Boolean = text.length <= 20 && text.toString().uppercase() in ALL
}
