@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.dialogs.DeleteFileDialog
import com.github.tukcps.sysmd.ui.viewmodel.TabsViewModel


/**
 * Shows a row of file-names on top of the SysMD2 window (=tabs).
 * The file-names are the editor tabs in the EditorTabsModel.
 * The concrete rendering of each name is done in EditorTabView.
 */
@Composable
fun EditorTabs(tabsViewModel: TabsViewModel) {

    // on close, set openDialog
    if (tabsViewModel.removeFileDialog.value) {
        DeleteFileDialog(tabsViewModel.removeFileDialog, tabsViewModel::removeFile)
    }

    val titles = tabsViewModel.editorTabs.map { it.tabTitle }.toList()
    if (titles.isNotEmpty()) {
        Tabs(
            titles,
            tabsViewModel.selectedIndex,
            onSelection = { tabsViewModel.selectedIndex.value = it; },
            onClose = { tabsViewModel.removeFile.value = it; tabsViewModel.removeFileDialog.value = true },
            onAdd = tabsViewModel::addNewFile,
            onRename = tabsViewModel::rename
        )
    } else
        EditorEmptyView()
}