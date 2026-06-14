@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.paneright

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
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel


/**
 * Top Composable defining the agenda GUI
 */
@Composable
fun Board(boardViewModel: BoardViewModel, editorTabsViewModel: EditorTabsViewModel) {
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
                    append(boardViewModel.issues().size.toString())
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
                items(boardViewModel.issues().sortedByDescending { it.issue?.kind?.ordinal }) { item: IssueViewModel ->
                    Issue(issueViewModel = item, editorTabsModel = editorTabsViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}