package io.opendata.db

import com.intellij.psi.TokenType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.opendata.db.grid.TableDataController
import io.opendata.db.lang.SqlCompletionContributor
import io.opendata.db.lang.SqlLexer
import io.opendata.db.lang.SqlTokens
import io.opendata.db.session.DbError
import io.opendata.db.session.SqlSplitter

/** Лексер, разбиение на выражения, completion-помощники, преобразования DML — без БД. */
class SqlCoreTest : BasePlatformTestCase() {

    fun testSplitterRespectsQuotesCommentsAndDollarQuoting() {
        val sql = """
            SELECT 'a;b' AS x; -- comment; here
            CREATE FUNCTION f() RETURNS int LANGUAGE plpgsql AS $$ BEGIN RETURN 1; END $$;
            /* block ; */ SELECT "we;ird" FROM t
        """.trimIndent()
        val chunks = SqlSplitter.split(sql)
        assertEquals(3, chunks.size)
        assertTrue(chunks[1].text.endsWith("END $$"))
        assertTrue(chunks[2].text.contains("\"we;ird\""))
        val caret = sql.indexOf("RETURN 1")
        assertEquals(chunks[1], SqlSplitter.statementAt(sql, caret))
    }

    fun testLexerTokens() {
        val lexer = SqlLexer()
        lexer.start("SELECT u.id, 'x' -- c\nFROM \"Users\" u WHERE id = \$1 AND n > 1.5e3")
        val types = generateSequence { lexer.tokenType?.also { lexer.advance() } }.filter { it != TokenType.WHITE_SPACE }.toList()
        assertEquals(SqlTokens.KEYWORD, types.first())
        assertTrue(types.contains(SqlTokens.STRING))
        assertTrue(types.contains(SqlTokens.LINE_COMMENT))
        assertTrue(types.contains(SqlTokens.QUOTED_IDENTIFIER))
        assertTrue(types.contains(SqlTokens.PARAMETER))
        assertTrue(types.contains(SqlTokens.NUMBER))
    }

    fun testSyntaxHighlightingInEditor() {
        myFixture.configureByText("q.sql", "SELECT 1 FROM t;")
        assertEquals("OpenDataSQL", myFixture.file.language.id)
    }

    fun testAliasResolution() {
        assertEquals("users", SqlCompletionContributor.resolveAlias("SELECT u.\nFROM users u", "u"))
        assertEquals("orders", SqlCompletionContributor.resolveAlias("select * from public.orders as o join x", "o"))
        assertEquals("u", SqlCompletionContributor.qualifierBefore("select u.", 9))
    }

    fun testClickHouseDml() {
        assertEquals(
            "ALTER TABLE opendata.events UPDATE name = ? WHERE id = ? SETTINGS mutations_sync = 2",
            TableDataController.clickHouseDml("UPDATE opendata.events SET name = ? WHERE id = ?"),
        )
        assertEquals("DELETE FROM t WHERE id = ?", TableDataController.clickHouseDml("DELETE FROM t WHERE id = ?"))
    }

    fun testClickHouseErrorPosition() {
        val e = DbError.from(java.sql.SQLException("Code: 62. DB::Exception: Syntax error: failed at position 8 ('FROMM')"))
        assertEquals(8, e.position)
    }

    fun testDataSourceStateRoundTrip() {
        val state = io.opendata.db.model.DataSourceStorage.State()
        state.dataSources += io.opendata.db.model.DataSourceConfig().apply {
            id = "pg"; name = "PostgreSQL Local"; kind = io.opendata.db.model.DbKind.POSTGRESQL
            port = 54329; database = "opendata"; user = "opendata"; properties["sslmode"] = "disable"
        }
        val xml = com.intellij.util.xmlb.XmlSerializer.serialize(state)
        val text = com.intellij.openapi.util.JDOMUtil.write(xml)
        println(text)
        val back = com.intellij.util.xmlb.XmlSerializer.deserialize(xml, io.opendata.db.model.DataSourceStorage.State::class.java)
        val ds = back.dataSources.single()
        assertEquals("PostgreSQL Local", ds.name)
        assertEquals(54329, ds.port)
        assertEquals("disable", ds.properties["sslmode"])
    }
}
