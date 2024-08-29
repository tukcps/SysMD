@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.tukcps.sysmd.ui.composables.Tabs
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.TabsModel


/**
 * Shows a row of file-names on top of the SysMD2 window (=tabs).
 * The file-names are the editor tabs in the EditorTabsModel.
 * The concrete rendering of each name is done in EditorTabView.
 */
@Composable
fun EditorTabsView(model: TabsModel) {

    // on close must set openDialog
    val openDialog = remember { mutableStateOf(false) }
    if (openDialog.value && (model.active is EditorTabModel)){
        SaveDialog(openDialog, model.active as EditorTabModel)
    }

    val titles = model.editorTabs.map { it.tabTitle.value }.toList()
    Tabs(titles, model.selectedIndex,
        onSelection = { model.selectedIndex.value = it },
        onClose = {
            if (model.selectedIndex.value > it)
                model.selectedIndex.value -= 1
            val tab = model.editorTabs.getOrNull(it)
            if ( (tab is EditorTabModel) && tab.elementEdited.value)
                openDialog.value = true
            else
                tab?.close?.let { it() }        }
    )
}