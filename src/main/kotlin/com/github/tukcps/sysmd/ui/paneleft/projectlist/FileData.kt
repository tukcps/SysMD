package com.github.tukcps.sysmd.ui.paneleft.projectlist

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.ui.viewmodel.CellListViewModel
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel


/**
 * The data that is maintained for a tab/file
 */
data class CellData(
    var namespace: QualifiedName,
    var language: Language,
    var body: String
)


/**
 * Data class for maintaining all changes, including those that are not displayed.
 * Manages the transfer between repository, local data, and view model of UI.
 * - repository is some kind of backend, e.g., file system or database.
 * - file data is the locally persisted data.
 * - view model is the data as (partially) presented and modified by the UI.
 * @param cellData: List of cell's data per file/tabname.
 */
data class FileData(
    var cellData: LinkedHashMap<String, List<ElementData>> = LinkedHashMap()
) {

    /**
     * Copies the data into a given tab given as parameter.
     * @param cellListViewModel The viewModel to which the data is copied.
     */
    fun copyToViewModel(cellListViewModel: CellListViewModel) {
        cellListViewModel.cells.forEach { cell ->

        }
    }

    fun copyFromViewModel(tabs: EditorTabsViewModel) {

    }

    fun saveToRepository() {

    }

    fun saveToRepository(name: String) {

    }

    fun loadFromRepository() {

    }
}