@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.paneright

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
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.explanation
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import kotlinx.coroutines.launch

/**
 * List element for agenda
 */
@Composable
fun Issue(
    issueViewModel: IssueViewModel,
    editorTabsModel: EditorTabsViewModel
) {
    val expanded: MutableState<Boolean> = remember { mutableStateOf(false) }
    if (!expanded.value) {
        FoldedIssue(expanded, issueViewModel, editorTabsModel)
    } else {
        ExpandedIssue(expanded, issueViewModel, editorTabsModel)
    }
}

@Composable
private fun ExpandedIssue(
    expanded: MutableState<Boolean>,
    issueViewModel: IssueViewModel,
    editorTabsModel: EditorTabsViewModel
) {
    val editorTabModel = editorTabsModel.active
    Card(
        modifier = Modifier.padding(all = 5.dp).clickable { expanded.value = !expanded.value },
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
                LeadingIcon(issueViewModel.issue?.kind?:Issue.Kind.DEBUG, issueViewModel = issueViewModel)
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                ) {
                    Text(text = issueViewModel.getTitle(), overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = issueViewModel.errorOriginString(editorTabsModel),
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                AgendaButtons(issueViewModel.wikiLink, editorTabModel = editorTabModel, issueViewModel)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp, 16.dp, 16.dp)
            ) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(text = "Error message: ", style = MaterialTheme.typography.labelLarge)
                    Text(text =  issueViewModel.getMessage(), style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Explanation: ", style = MaterialTheme.typography.labelLarge)
                    Text(text = explanation[issueViewModel.issue?.kind]?:"", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


@Composable
private fun FoldedIssue(
    expanded: MutableState<Boolean>,
    issueViewModel: IssueViewModel,
    editorTabsModel: EditorTabsViewModel,
) {
    val editorTabModel = editorTabsModel.active
    Card(
        modifier = Modifier.padding(all = 5.dp).clickable { expanded.value = !expanded.value },
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LeadingIcon(issueViewModel.issue?.kind?:Issue.Kind.DEBUG, issueViewModel = issueViewModel)
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                Text(text = issueViewModel.getTitle(), overflow = TextOverflow.Ellipsis, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = issueViewModel.errorOriginString(editorTabsModel),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            AgendaButtons(issueViewModel.wikiLink(), editorTabModel = editorTabModel, issueViewModel)
        }
    }
}

@Composable
private fun LeadingIcon(kind: Issue.Kind, issueViewModel: IssueViewModel) {
    Icon(
        Icons.Filled.Error,
        contentDescription = "Issue",
        tint = issueViewModel.errorTypeToColor(kind),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun AgendaButtons(
    link: String,
    editorTabModel: EditorTabModel?,
    issueViewModel: IssueViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler: UriHandler = LocalUriHandler.current

    FilledIconButton(
        onClick = {
            if (editorTabModel != null) {
                // Gets index of cell by id
                val index = editorTabModel.cells.indexOfFirst { cell -> cell.body.text == issueViewModel.getInput() }

                // Scroll to cell
                if (index >= 0) coroutineScope.launch {
                    editorTabModel.scrollState.animateScrollToItem(index = index)
                }
            }
        },
        modifier = Modifier.size(24.dp)
    ) {
        SysMDTooltipArea("Navigate to cell with error (if in open tab)", Modifier) {
            Icon(Icons.Filled.Code, contentDescription = "Navigate to cell")
        }
    }
    FilledIconButton(
        onClick = { uriHandler.openUri(link) },
        modifier = Modifier.size(24.dp)
    ) {
        SysMDTooltipArea("Open URL with explanation", Modifier) {
            Icon(Icons.Filled.Language, contentDescription = null)
        }
    }
}