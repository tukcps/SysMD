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
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.TabModel
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
     * Line in cell in which the error occurred. Set to -1 for identifying the case that no line is given.
     * If a negative number is passed, no line is displayed.
     */
    errorLine: Int = -1,
    /**
     * Wiki link, defaults to main page
     */
    link: WikiLink = exceptionLink(errorType),
    /**
     * Preferably an EditorTabModel, as one is necessary to make use of the jump
     * function from error card to failing cell. If none given, then nothing happens on click
     */
    editorTabModel: TabModel?,
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
    editorTabModel: TabModel?,
    description: TextualRepresentation?
) {
    Card(
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                Spacer(
                    modifier = Modifier
                        .weight(1.0f)
                        .clickable { expanded.value = !expanded.value })
                ElementButtons(link, editorTabModel = editorTabModel, description = description)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 16.dp)
                    .clickable { expanded.value = !expanded.value }
            ) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = title, style = MaterialTheme.typography.labelMedium,)
                    Text(text = errorOrigin(description, editorTabModel, line) + "\n" + message, style = MaterialTheme.typography.labelSmall)
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
    editorTabModel: TabModel?,
    description: TextualRepresentation?
) {
    Card(
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                Text(text = title, overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.labelMedium,)
                Text(
                    text = errorOrigin(description, editorTabModel, line),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            ElementButtons(link, editorTabModel = editorTabModel, description = description)
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
private fun ElementButtons(
    link: WikiLink,
    editorTabModel: TabModel?,
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
        Icon(Icons.Filled.Code, contentDescription = null)
    }
    FilledIconButton(
        onClick = { uriHandler.openUri(link) },
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            Icons.Filled.Language,
            contentDescription = null
        )
    }
}

/**
 * Returns formatted String with cell number and line in which the error occurred, depending on the available information
 */
private fun errorOrigin(
    description: TextualRepresentation?,
    editorTabModel: TabModel?,
    line: Int
): String {
    val index = if (description != null && editorTabModel is EditorTabModel) {
        // Gets index of cell by id
        // Add one to adapt human indexing starting with 1
        editorTabModel.cells.indexOfFirst { cell -> cell.textualRepresentation.elementId == description.elementId } + 1
    } else {
        "Program error"
    }
    return if (index is String) {
        index
    } else {
        "Cell: $index" + if (line > 0) ", Line: $line" else ""
    }
}
