@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel

/**
 * Function that maps the Exception type to a human-readable string.
 * @param errorType The exception that was thrown or generated
 */
fun sysMDExceptionToString(errorType: SysMDException) = when (errorType) {
    is SysMDInconsistency -> "Inconsistency in model"
    is SysMDInfo -> "Info"
    is ExpressionError -> "Expression error"
    is ElementNotFoundException -> "Element not found"
    is InheritanceException -> "Inheritance issue"
    is InitialisationException -> "Initialization issue"
    is LexicalError -> "Lexical error"
    is SyntaxError -> "Syntax error"
    is SysMDError -> "SysMD error"
    else -> "General SysMD error"
}

/**
 * Function that maps the exception type to a color
 */
fun errorTypeToColor(errorType: SysMDException) = when (errorType) {
    is SysMDInconsistency -> AppTheme.colors.warning
    is SysMDInfo -> AppTheme.colors.warning
    is ExpressionError -> AppTheme.colors.iconRed
    is ElementNotFoundException -> AppTheme.colors.iconRed
    is InheritanceException -> AppTheme.colors.warning
    is InitialisationException -> AppTheme.colors.warning
    is LexicalError -> AppTheme.colors.iconRed
    is SyntaxError -> AppTheme.colors.iconRed
    is SysMDError -> AppTheme.colors.iconRed
    else -> AppTheme.colors.warning
}


typealias WikiLink = String

/**
 * Resolves links to wiki depending on exception class, defaults to the main page if class unknown
 */
fun exceptionLink(exceptionType: SysMDException): WikiLink = when (exceptionType) {
    is SysMDError -> when (exceptionType) {
        is SyntaxError -> "https://cps-mediawiki.cs.rptu.de/index.php/SyntaxError"
        is LexicalError -> "https://cps-mediawiki.cs.rptu.de/index.php/LexicalError"
        is SemanticError -> "https://cps-mediawiki.cs.rptu.de/index.php/SemanticError"
        is InternalError -> "https://cps-mediawiki.cs.rptu.de/index.php/InternalError"
        is ElementNotFoundException -> "https://cps-mediawiki.cs.rptu.de/index.php/ElementNotFoundException"
        is StaticException -> when (exceptionType) {
            is InitialisationException -> "https://cps-mediawiki.cs.rptu.de/index.php/InitilisationException"
            is ImportException -> "https://cps-mediawiki.cs.rptu.de/index.php/ImportException"
            else -> "https://cps-mediawiki.cs.rptu.de/index.php/StaticException"
        }

        is SolverException -> when (exceptionType) {
            is InheritanceException -> "https://cps-mediawiki.cs.rptu.de/index.php/InheritanceException"
            else -> "https://cps-mediawiki.cs.rptu.de/index.php/SolverException"
        }

        else -> "https://cps-mediawiki.cs.rptu.de/index.php/SysMDError"
    }

    is ExportIssue -> "https://cps-mediawiki.cs.rptu.de/index.php/ExportIssue"
    is SysMDInfo -> "https://cps-mediawiki.cs.rptu.de/index.php/SysMDInfo"
    else -> "https://cps-mediawiki.cs.rptu.de"
}



/**
 * Top Composable defining the agenda GUI
 */
@Composable
fun Agenda(agenda: AgendaViewModel, editorTabModel: EditorTabModel?) {
    val style = SpanStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp),
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Text(
                modifier = Modifier.padding(8.dp),
                text = buildAnnotatedString {
                withStyle(style = style) { append("Summary: ") }
                withStyle(style.merge(SpanStyle(fontWeight = FontWeight.SemiBold))) {
                    append(agenda.issues().size.toString())
                }
                withStyle(style = style) { append(" issues") }
            })
        },
        bottomBar = {},
        snackbarHost = {},
        floatingActionButton = {},
    ) {
        innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LazyColumn {
                items(agenda.issues()) { item: AgendaElement ->
                    AgendaListElement(
                        editorTabModel = editorTabModel,
                        message = item.errorMessage,
                        errorLine = item.line,
                        errorType = item.exceptionClass,
                        description = item.textualRepresentation
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}