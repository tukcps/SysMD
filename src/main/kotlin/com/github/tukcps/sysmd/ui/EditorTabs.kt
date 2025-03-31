@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.dialogs.DeleteFileDialog
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel


/**
 * Shows a row of file-names on top of the SysMD2 window (=tabs).
 * The file-names are the editor tabs in the EditorTabsModel.
 * The concrete rendering of each name is done in EditorTabView.
 */
@Composable
fun EditorTabs(editorTabsViewModel: EditorTabsViewModel) {

    // on close, set openDialog
    if (editorTabsViewModel.removeFileDialog.value) {
        DeleteFileDialog(editorTabsViewModel.removeFileDialog, editorTabsViewModel::removeFile)
    }

    val titles = editorTabsViewModel.editorTabs.map { it.tabTitle }.toList()
    if (titles.isNotEmpty()) {
        Tabs(
            titles, editorTabsViewModel.selectedIndex,
            onSelection = { editorTabsViewModel.selectedIndex.value = it; },
            onClose = { editorTabsViewModel.removeFile.value = it; editorTabsViewModel.removeFileDialog.value = true },
            onAdd = editorTabsViewModel::addNewFile,
            onRename = editorTabsViewModel::rename
        )
    } else
        EditorEmptyView()
}