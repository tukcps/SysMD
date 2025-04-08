package com.github.tukcps.sysmd.ui.paneright

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel

/**
 * This class stores additional information to the qualified names of the
 * elements on which an error occurred
 */
class IssueViewModel(
    val issue: Issue? = null,
    val wikiLink: String = ""
) {
    /**
     * Link to wiki
     */
    val qualifiedName: String? = issue?.elementPath

    fun getTitle() = when (issue?.kind) {
        Issue.Kind.TRACE -> "Trace"
        Issue.Kind.DEBUG -> "Debug"
        Issue.Kind.INFO -> "Information"
        Issue.Kind.WARN -> "Warning"
        Issue.Kind.WARN_ITERATIONS_EXCEEDED -> "#iterations exceeded"
        Issue.Kind.WARN_UNRESOLVED_OWNER -> "Unresolved owner"
        Issue.Kind.WARN_UNRESOLVED_TYPE -> "Unresolved type"
        Issue.Kind.WARN_INCONSISTENCY -> "Inconsistency"
        Issue.Kind.ERROR -> "Error"
        Issue.Kind.ERROR_UNRESOLVED_NAME -> "Unresolved name"
        Issue.Kind.ERROR_TYPE_WRONG -> "Wrong type"
        Issue.Kind.ERROR_CYCLIC_DEPENDENCY -> "Cyclic dependency"
        Issue.Kind.ERROR_SEMANTIC -> "Semantic error"
        Issue.Kind.ERROR_SYNTACTICAL -> "Syntax error"
        Issue.Kind.ERROR_LEXICAL -> "Lexical error"
        Issue.Kind.FATAL -> "Fatal error"
        null -> "No report present"
    }

    fun getLine() = issue?.token?.lineNo
    fun getInput() = issue?.input
    fun getMessage(): String {
        val where = if (issue?.token != null) "at '${issue.token.toString()}': " else ""
        val str = "$where${issue?.message?:"(no message)"}"
        return str
    }

    /**
     * Returns formatted String with cell number and line in which the error occurred,
     * depending on the available information
     */
    fun errorOriginString(
        editorTabsModel: EditorTabsViewModel?,
    ): String {
        // val editorTabModel = editorTabsModel?.active
        var str = "Causing input not related to particular tab"
        if (getInput() != null) {
            editorTabsModel?.editorTabs?.forEach { editorTabModel ->
                val index = editorTabModel.cells.indexOfFirst { cell -> cell.body.text == getInput() }
                if (index >= 0) {
                    str = "${editorTabModel.tabTitle.value}, cell ${index+1}" + if (getLine() != null && getLine()!! > 0) ", line: ${getLine()}" else ""
                }
            }
        }
        return str
    }


    /**
     * Function that maps the exception type to a color
     */
    fun errorTypeToColor(kind: Issue.Kind) = when (kind) {
        Issue.Kind.TRACE -> AppTheme.colors.info
        Issue.Kind.DEBUG -> AppTheme.colors.info
        Issue.Kind.INFO -> AppTheme.colors.info
        Issue.Kind.WARN -> AppTheme.colors.warning
        Issue.Kind.WARN_ITERATIONS_EXCEEDED -> AppTheme.colors.warning
        Issue.Kind.WARN_UNRESOLVED_OWNER -> AppTheme.colors.warning
        Issue.Kind.WARN_UNRESOLVED_TYPE -> AppTheme.colors.warning
        Issue.Kind.WARN_INCONSISTENCY -> AppTheme.colors.warning
        Issue.Kind.ERROR -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_UNRESOLVED_NAME -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_TYPE_WRONG -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_CYCLIC_DEPENDENCY -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_SEMANTIC -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_SYNTACTICAL -> AppTheme.colors.iconRed
        Issue.Kind.ERROR_LEXICAL -> AppTheme.colors.iconRed
        Issue.Kind.FATAL -> AppTheme.colors.iconRed
    }

    /**
     * Resolves links to wiki depending on exception class, defaults to the main page if class unknown
     */
    fun wikiLink(): String = when (issue?.kind) {
        Issue.Kind.ERROR_SEMANTIC -> "https://cps-mediawiki.cs.rptu.de/index.php/SemanticError"
        Issue.Kind.TRACE    -> "https://cps-mediawiki.cs.rptu.de"
        Issue.Kind.DEBUG    -> "https://cps-mediawiki.cs.rptu.de"
        Issue.Kind.INFO     -> "https://cps-mediawiki.cs.rptu.de/index.php/Information"
        Issue.Kind.WARN     -> "https://cps-mediawiki.cs.rptu.de/index.php/Warning"
        Issue.Kind.WARN_ITERATIONS_EXCEEDED -> "https://cps-mediawiki.cs.rptu.de"
        Issue.Kind.WARN_UNRESOLVED_OWNER -> "https://cps-mediawiki.cs.rptu.de"
        Issue.Kind.WARN_UNRESOLVED_TYPE -> "https://cps-mediawiki.cs.rptu.de"
        Issue.Kind.WARN_INCONSISTENCY -> "https://cps-mediawiki.cs.rptu.de/index.php/Inconsistency"
        Issue.Kind.ERROR -> "https://cps-mediawiki.cs.rptu.de/index.php/Error"
        Issue.Kind.ERROR_TYPE_WRONG -> "https://cps-mediawiki.cs.rptu.de/index.php/Error"
        Issue.Kind.ERROR_CYCLIC_DEPENDENCY -> "https://cps-mediawiki.cs.rptu.de/index.php/Error"
        Issue.Kind.ERROR_UNRESOLVED_NAME -> "https://cps-mediawiki.cs.rptu.de/index.php/UnresolvedName"
        Issue.Kind.ERROR_SYNTACTICAL -> "https://cps-mediawiki.cs.rptu.de/index.php/ErrorSyntactical"
        Issue.Kind.ERROR_LEXICAL -> "https://cps-mediawiki.cs.rptu.de/index.php/ErrorLexical"
        Issue.Kind.FATAL -> "https://cps-mediawiki.cs.rptu.de/index.php/Fatal"
        null ->  "https://cps-mediawiki.cs.rptu.de"
    }


    override fun toString(): String {
        return "AgendaElement: (QualifiedName: $qualifiedName, ErrorMessage: ${issue?.message}, Line: ${issue?.token?.lineNo})"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is IssueViewModel -> false
            else -> (issue.toString() == other.issue?.toString())
        }
    }

    override fun hashCode(): Int = issue.hashCode()
}