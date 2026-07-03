package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.ui.paneleft.projectlist.ProjectListViewModel
import kotlin.uuid.Uuid

/**
 * The view model of the opened file-editor tabs.
 * A list of tabs, among which one is active for editing, or
 * null if none is yet open.
 * @param projectListViewModel the respective project list with the project.
 * @param refreshTrees lambda to be called when refresh of tree views is needed.
 */
class EditorTabsViewModel(
    val projectListViewModel: () -> ProjectListViewModel,
    val refreshTrees: () -> Unit,
) {
    val sessionIdState: MutableState<Uuid> get() = projectListViewModel().sessionIdState
    val editorTabs = mutableStateListOf<CellListViewModel>()
    val selectedIndex: MutableState<Int> = mutableStateOf(0)
    val selectedCellList: CellListViewModel? get() = editorTabs.getOrNull(selectedIndex.value)


    /**
     * Unclear what to do here ...
     */
    fun onShowTab() : (String) -> Unit = {}

    /**
     * Functions to be called for resetting the model.
     * Reset will delete all generated models, but NOT the annotations in which the source code is saved.
     */
    fun reset() {
        try {
            editorTabs.forEach { tab ->
                tab.reset()
                tab.close = { hideTab(tab) }
            }
        } catch (exception: Exception) {
            logger.error( "Issue in reset of editor tabs: $exception", exception)
        }
    }

    /**
     * Gets a file from the project into the view model and opens a suitable tab.
     * @param name the file or tab to be shown and activated
     */
    fun showTab(name: String) {
        val existing: CellListViewModel? = editorTabs.find { tab -> tab.nameState.value.equals(name, ignoreCase = true) }
        if (existing == null) {
            val editorTab = CellListViewModel(sessionIdState, this, mutableStateOf(name))
            val project = projectListViewModel().selectedProjectState.value
            val cellDataKey = project?.fileData?.cellData?.keys?.firstOrNull { it.equals(name, ignoreCase = true) }
            if (cellDataKey != null && project.fileData.cellData[cellDataKey] != null) {
                editorTab.addTabAndCellList(sessionIdState, project.fileData.cellData[cellDataKey]!!)
            }
            editorTab.close = { hideTab(editorTab) }
            editorTabs.add(editorTab)
        }
        // Select the shown tab
        selectedIndex.value = editorTabs
            .indexOfFirst { it.nameState.value.equals(name, ignoreCase = true) }
            .takeIf { it >= 0 } ?: 0
    }

    /** Closes an editor tab. */
    fun hideTab(tab: CellListViewModel) {
        try {
            editorTabs.remove(tab)
            selectedIndex.value = selectedIndex.value.coerceAtMost(editorTabs.lastIndex)
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }

    /**
     * Changes the name of a file/tab.
     * @param oldName the old name of the tab.
     * @param newName new name of the tab.
     */
    fun updateTabTitle(oldName: String, newName: String) {
        val tab = findTabByName(oldName)
        if (tab == null) { logger.error("$newName could not be updated due to internal error.") }
        tab?.nameState?.value = newName
    }

    /**
     * Saves all open tabs/files and marks tab as not edited
     */
    fun save() = editorTabs.forEach { tab ->
        tab.save()
        tab.hasChangesState.value = false
    }

    /**
     * Searches a tab by its name and returns its index in the list of tabs; -1 if not there
     */
    fun findTabIndexByName(name: String): Int {
        return editorTabs.indexOfFirst { it.nameState.value.equals(name, ignoreCase = true) }
    }

    fun findTabByName(name: String): CellListViewModel? = editorTabs.find { it.nameState.value.equals(name, ignoreCase = true) }
}