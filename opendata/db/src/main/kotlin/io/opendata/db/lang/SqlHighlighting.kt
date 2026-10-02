package io.opendata.db.lang

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as C
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.util.ProcessingContext
import io.opendata.db.meta.MetadataCache
import io.opendata.db.ui.ConsoleFiles

class SqlSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = SqlLexer()

    override fun getTokenHighlights(type: IElementType?): Array<TextAttributesKey> = when (type) {
        SqlTokens.KEYWORD -> pack(KEYWORD)
        SqlTokens.STRING -> pack(STRING)
        SqlTokens.NUMBER -> pack(NUMBER)
        SqlTokens.LINE_COMMENT -> pack(LINE_COMMENT)
        SqlTokens.BLOCK_COMMENT -> pack(BLOCK_COMMENT)
        SqlTokens.QUOTED_IDENTIFIER -> pack(QUOTED)
        SqlTokens.PARAMETER -> pack(PARAMETER)
        SqlTokens.OPERATOR -> pack(OPERATOR)
        SqlTokens.SEMICOLON -> pack(SEMICOLON)
        SqlTokens.COMMA -> pack(COMMA)
        SqlTokens.DOT -> pack(DOT)
        SqlTokens.LPAREN, SqlTokens.RPAREN -> pack(PARENS)
        TokenType.BAD_CHARACTER -> pack(C.INVALID_STRING_ESCAPE)
        else -> TextAttributesKey.EMPTY_ARRAY
    }

    companion object {
        val KEYWORD = createTextAttributesKey("OPENDATA_SQL_KEYWORD", C.KEYWORD)
        val STRING = createTextAttributesKey("OPENDATA_SQL_STRING", C.STRING)
        val NUMBER = createTextAttributesKey("OPENDATA_SQL_NUMBER", C.NUMBER)
        val LINE_COMMENT = createTextAttributesKey("OPENDATA_SQL_LINE_COMMENT", C.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("OPENDATA_SQL_BLOCK_COMMENT", C.BLOCK_COMMENT)
        val QUOTED = createTextAttributesKey("OPENDATA_SQL_QUOTED_IDENTIFIER", C.INSTANCE_FIELD)
        val PARAMETER = createTextAttributesKey("OPENDATA_SQL_PARAMETER", C.PARAMETER)
        val OPERATOR = createTextAttributesKey("OPENDATA_SQL_OPERATOR", C.OPERATION_SIGN)
        val SEMICOLON = createTextAttributesKey("OPENDATA_SQL_SEMICOLON", C.SEMICOLON)
        val COMMA = createTextAttributesKey("OPENDATA_SQL_COMMA", C.COMMA)
        val DOT = createTextAttributesKey("OPENDATA_SQL_DOT", C.DOT)
        val PARENS = createTextAttributesKey("OPENDATA_SQL_PARENS", C.PARENTHESES)
    }
}

class SqlSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter = SqlSyntaxHighlighter()
}

/**
 * Completion (ТЗ 16): ключевые слова, таблицы и колонки из кэша метаданных источника данных консоли.
 * После "alias." или "table." предлагаются колонки соответствующей таблицы.
 */
class SqlCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(OpenDataSqlLanguage), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                val file = parameters.originalFile.virtualFile
                val dsId = file?.let { ConsoleFiles.dataSourceIdOf(it) }
                val text = parameters.editor.document.charsSequence
                val offset = parameters.offset
                val tables = dsId?.let { MetadataCache.getInstance().tables(it) }.orEmpty()

                val qualifier = qualifierBefore(text, offset - result.prefixMatcher.prefix.length)
                if (qualifier != null) {
                    val tableName = resolveAlias(text.toString(), qualifier) ?: qualifier
                    val table = tables.firstOrNull { it.name.equals(tableName, true) }
                    if (table != null) {
                        table.columns.forEach { col ->
                            result.addElement(LookupElementBuilder.create(col).withIcon(AllIcons.Nodes.Field).withTypeText(table.name))
                        }
                        result.stopHere()
                        return
                    }
                    tables.filter { it.schema.equals(qualifier, true) }.forEach { t ->
                        result.addElement(LookupElementBuilder.create(t.name).withIcon(AllIcons.Nodes.DataTables).withTypeText(t.schema ?: ""))
                    }
                    return
                }
                tables.forEach { t ->
                    result.addElement(LookupElementBuilder.create(t.name).withIcon(AllIcons.Nodes.DataTables).withTypeText(t.schema ?: ""))
                }
                tables.mapNotNull { it.schema }.distinct().forEach { s ->
                    result.addElement(LookupElementBuilder.create(s).withIcon(AllIcons.Nodes.Folder).withTypeText("schema"))
                }
                SqlKeywords.ALL.forEach { kw ->
                    result.addElement(LookupElementBuilder.create(kw).withBoldness(true).withCaseSensitivity(false))
                }
            }
        })
    }

    companion object {
        /** Идентификатор перед точкой: "u." → "u". */
        fun qualifierBefore(text: CharSequence, start: Int): String? {
            var i = start - 1
            if (i < 0 || text[i] != '.') return null
            i--
            val end = i + 1
            while (i >= 0 && (text[i].isLetterOrDigit() || text[i] == '_' || text[i] == '"')) i--
            return text.subSequence(i + 1, end).toString().trim('"').ifEmpty { null }
        }

        /** "FROM users u" / "JOIN orders AS o" → таблица для алиаса. */
        fun resolveAlias(sql: String, alias: String): String? {
            val re = Regex("""(?i)\b(?:from|join)\s+(?:[\w"]+\.)?"?(\w+)"?\s+(?:as\s+)?${Regex.escape(alias)}\b""")
            return re.find(sql)?.groupValues?.get(1)
        }
    }
}
