@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.services.Agenda
import com.github.tukcps.sysmd.services.AgendaElement
import com.github.tukcps.sysmd.ui.helper.fitMaxSize
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.TabModel

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
 * Resolves links to wiki depending on exception class, defaults to main page if class unknown
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
 * Header of error messages list
 */
@Composable
fun ListHeader(modifier: Modifier = Modifier, agenda: Agenda) {
    val style = SpanStyle(
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize
    )
    Text(buildAnnotatedString {
        withStyle(style = style) { append("Summary: ") }
        withStyle(style.merge(SpanStyle(fontWeight = FontWeight.Bold))) {
            append(agenda.issues().size.toString())
        }
        withStyle(style = style) { append(" Issues") }
    }, modifier = modifier)
    Box(
        modifier = modifier.fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp)
    )
}

/**
 * Composable defining the list of all error messages
 */
@Composable
fun ListView(modifier: Modifier = Modifier, agenda: Agenda, editorTabModel: TabModel?) {
    LazyColumn(modifier = modifier) {
        //TODO adapt to new agenda
        //if(dropDownMenuState != "Agenda"){
        item { ListHeader(modifier, agenda) }

        //}
        items(agenda.issues()) { item: AgendaElement ->
            // item.textualRepresentation?.let {
            AgendaListElement(
                editorTabModel = editorTabModel,
                message = item.errorMessage,
                errorLine = item.line,
                errorType = item.exceptionClass,
                description = item.textualRepresentation
            )
            // }
        }
    }
}

/**
 * Top Composable defining the agenda GUI
 */
@Composable
fun AgendaView(agenda: Agenda, editorTabModel: TabModel?) {
    Surface(
        modifier = Modifier.fitMaxSize(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.1.dp)
    ) {
        Column {
            ListView(modifier = Modifier.padding(8.dp), agenda, editorTabModel)
        }
    }
}