package io.opendata.db.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Commenter
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.icons.AllIcons
import javax.swing.Icon

object OpenDataSqlLanguage : Language("OpenDataSQL") {
    private fun readResolve(): Any = OpenDataSqlLanguage
    override fun getDisplayName(): String = "SQL"
    override fun isCaseSensitive(): Boolean = false
}

object SqlFileType : LanguageFileType(OpenDataSqlLanguage) {
    override fun getName(): String = "OpenData SQL"
    override fun getDescription(): String = "SQL"
    override fun getDefaultExtension(): String = "sql"
    override fun getIcon(): Icon = AllIcons.FileTypes.Text
}

class SqlTokenType(debugName: String) : IElementType(debugName, OpenDataSqlLanguage)

object SqlTokens {
    @JvmField val KEYWORD = SqlTokenType("KEYWORD")
    @JvmField val IDENTIFIER = SqlTokenType("IDENTIFIER")
    @JvmField val QUOTED_IDENTIFIER = SqlTokenType("QUOTED_IDENTIFIER")
    @JvmField val STRING = SqlTokenType("STRING")
    @JvmField val NUMBER = SqlTokenType("NUMBER")
    @JvmField val LINE_COMMENT = SqlTokenType("LINE_COMMENT")
    @JvmField val BLOCK_COMMENT = SqlTokenType("BLOCK_COMMENT")
    @JvmField val OPERATOR = SqlTokenType("OPERATOR")
    @JvmField val SEMICOLON = SqlTokenType("SEMICOLON")
    @JvmField val COMMA = SqlTokenType("COMMA")
    @JvmField val DOT = SqlTokenType("DOT")
    @JvmField val LPAREN = SqlTokenType("LPAREN")
    @JvmField val RPAREN = SqlTokenType("RPAREN")
    @JvmField val PARAMETER = SqlTokenType("PARAMETER")

    val COMMENTS: TokenSet = TokenSet.create(LINE_COMMENT, BLOCK_COMMENT)
    val STRINGS: TokenSet = TokenSet.create(STRING)
}

class SqlFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, OpenDataSqlLanguage) {
    override fun getFileType(): FileType = SqlFileType
    override fun toString(): String = "OpenData SQL file"
}

/** Плоский PSI: файл из токенов. Подсветка, комментарии и completion работают по токенам. */
class SqlParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = SqlLexer()
    override fun createParser(project: Project?): PsiParser = PsiParser { root, builder: PsiBuilder ->
        val marker = builder.mark()
        while (!builder.eof()) builder.advanceLexer()
        marker.done(root)
        builder.treeBuilt
    }
    override fun getFileNodeType(): IFileElementType = FILE
    override fun getCommentTokens(): TokenSet = SqlTokens.COMMENTS
    override fun getStringLiteralElements(): TokenSet = SqlTokens.STRINGS
    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)
    override fun createFile(viewProvider: FileViewProvider): PsiFile = SqlFile(viewProvider)

    companion object {
        val FILE = IFileElementType(OpenDataSqlLanguage)
    }
}

class SqlCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "-- "
    override fun getBlockCommentPrefix(): String = "/*"
    override fun getBlockCommentSuffix(): String = "*/"
    override fun getCommentedBlockCommentPrefix(): String? = null
    override fun getCommentedBlockCommentSuffix(): String? = null
}
