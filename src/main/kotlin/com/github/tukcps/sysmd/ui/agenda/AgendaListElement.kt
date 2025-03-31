@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import kotlinx.coroutines.launch

/**
 * List element for agenda
 */
@Composable
fun AgendaListElement(
    /**
     * Exception class
     */
    errorType: SysMDException,
    /**
     * Short, understandable summary of the error
     */
    title: String = sysMDExceptionToString(errorType),
    /**
     * Detailed error message only displayed on click on card
     */
    message: String = "",
    /**
     * Line in the cell in which the error occurred.
     * Set to -1 for identifying the case that no line is given.
     * If a negative number is passed, no line is displayed.
     */
    errorLine: Int = -1,
    /**
     * Wiki link, defaults to the main page
     */
    link: WikiLink = exceptionLink(errorType),
    /**
     * Preferably an EditorTabModel, as one is necessary to make use of the jump
     * function from error card to failing cell.
     * If none is given, then nothing happens on click
     */
    editorTabModel: EditorTabModel?,
    /**
     * Necessary for identifying the correct cell in editorTabModel to jump to
     */
    description: TextualRepresentation?
) {
    val expanded: MutableState<Boolean> = remember { mutableStateOf(false) }
    if (!expanded.value) {
        FoldedCard(expanded, errorType, title, errorLine, link, editorTabModel, description)
    } else {
        ExpandedCard(expanded, errorType, title, message, errorLine, link, editorTabModel, description)
    }
}

@Composable
private fun ExpandedCard(
    expanded: MutableState<Boolean>,
    errorType: SysMDException,
    title: String,
    message: String,
    line: Int,
    link: WikiLink,
    editorTabModel: EditorTabModel?,
    description: TextualRepresentation?
) {
    Card(
        modifier = Modifier.padding(all = 5.dp),
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 8.dp)
            ) {
                LeadingIcon(errorType)
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .clickable { expanded.value = !expanded.value }
                ) {
                    Text(text = title, overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = errorOriginString(description, editorTabModel, line),
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                AgendaButtons(link, editorTabModel = editorTabModel, description = description)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 16.dp)
                    .clickable { expanded.value = !expanded.value }
            ) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = "Concrete error message: ", style = MaterialTheme.typography.labelLarge)
                    Text(text =  message, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Explanation: ", style = MaterialTheme.typography.labelLarge)
                    Text(text = errorType.explanation, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

}

@Composable
private fun FoldedCard(
    expanded: MutableState<Boolean>,
    errorType: SysMDException,
    title: String,
    line: Int,
    link: WikiLink,
    editorTabModel: EditorTabModel?,
    description: TextualRepresentation?
) {
    Card(
        modifier = Modifier.padding(all = 5.dp),
        colors = CardDefaults.cardColors(),
        // elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LeadingIcon(errorType)
            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .clickable { expanded.value = !expanded.value }
            ) {
                Text(text = title, overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = errorOriginString(description, editorTabModel, line),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            AgendaButtons(link, editorTabModel = editorTabModel, description = description)
        }
    }
}

@Composable
private fun LeadingIcon(errorType: SysMDException) {
    Icon(
        Icons.Filled.Error,
        contentDescription = "Issue",
        tint = errorTypeToColor(errorType),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun AgendaButtons(
    link: WikiLink,
    editorTabModel: EditorTabModel?,
    description: TextualRepresentation?
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler: UriHandler = LocalUriHandler.current

    FilledIconButton(
        onClick = {
            if (description != null && editorTabModel is EditorTabModel) {
                // Gets index of cell by id
                val index =
                    editorTabModel.cells.indexOfFirst { cell -> cell.textualRepresentation.elementId == description.elementId }
                // Scroll to cell
                if (index >= 0) coroutineScope.launch {
                    editorTabModel.scrollState.animateScrollToItem(
                        index = index
                    )
                }
            }
        },
        modifier = Modifier.size(24.dp)
    ) {
        SysMDTooltipArea("Navigate to cell with error (if in open tab)", Modifier, { Icon(Icons.Filled.Code, contentDescription = "Navigate to cell" )} )
    }
    FilledIconButton(
        onClick = { uriHandler.openUri(link) },
        modifier = Modifier.size(24.dp)
    ) {
        SysMDTooltipArea("Open URL with explanation", Modifier, {Icon(Icons.Filled.Language, contentDescription = null)} )
    }
}

/**
 * Returns formatted String with cell number and line in which the error occurred, depending on the available information
 */
private fun errorOriginString(
    description: TextualRepresentation?,
    editorTabModel: EditorTabModel?,
    line: Int
): String {
    val index = if (description != null && editorTabModel is EditorTabModel) {
        // Gets index of cell by id
        // Add one to adapt human indexing starting with 1
        editorTabModel.cells.indexOfFirst { cell -> cell.textualRepresentation.elementId == description.elementId } + 1
    } else {
        null
    }
    return if (index == null) {
        "No relationship to any concrete cell."
    } else if (index >= 0) {
        "Cell: $index" + if (line > 0) ", Line: $line" else ""
    } else {
        "Cause of issue in other tab of editor."
    }
}
