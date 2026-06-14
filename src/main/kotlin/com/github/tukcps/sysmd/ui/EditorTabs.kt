@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel


/**
 * Shows a row of file-names on top of the SysMD window (=tabs).
 * The file-names are the editor tabs in the EditorTabsModel.
 * The concrete rendering of each name is done in EditorTabView.
 */
@Composable
fun EditorTabs(
    editorTabsViewModel: EditorTabsViewModel
) {
    val titles = editorTabsViewModel.editorTabs.map { it.nameState }.toList()
    if (titles.isNotEmpty()) {
        Tabs(
            titles,
            editorTabsViewModel.selectedIndex,
            onSelection = { editorTabsViewModel.selectedIndex.value = it; },
            onHide = { editorTabsViewModel.selectedCellList?.close?.let { it() } },
            onShow = editorTabsViewModel::onShowTab,
        )
    } else
        EditorEmptyView()
}