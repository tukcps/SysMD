package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.toMarkdownString
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.MoveRequest
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import kotlin.uuid.Uuid


/**
 * The view model of an editor tab.
 * As of now, we just use a simple list of Elements for a package.
 * This might change to a tree in line with the SysML v2 Metamodel.
 *
 * For the tab, we save
 *  - a list of elements that can be edited; an element is a Package or an Element.
 *  - the model (must be shared, if several tabs are active, not yet done)
 *  - the selected index of an element, and its section (description, code)
 * @param sessionIdState the internal model that is the result of the compilation and analysis;
 *        does not trigger any updates of any views.
 */
class CellListViewModel(
    val sessionIdState: MutableState<Uuid>,
    val editorTabsViewModel: EditorTabsViewModel,
    val nameState: MutableState<String>
) {
    val refreshTrees
        get() = editorTabsViewModel.refreshTrees

    /** The collapsed element ids in the tab */
    val collapsedCellIndices: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    /** The hidden element ids in the tab */
    val hiddenElementIds: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    /** The states of the view model. */
    val cells = mutableStateListOf<CellViewModel>()
    var scrollState = LazyListState()
    val editState =  mutableStateOf(false)

    val hasChangesState: MutableState<Boolean> = mutableStateOf(false)
    val references = InternalRefReference(editorTabModel = this) { generateTableOfContents() }

    /**
     * Creates a new element data model from the view model of a cell.
     * @param cellViewModel The view model that is used for building the data model.
     * @return A data model that has language, namespace, body.
     */
    private fun toElementData(cellViewModel: CellViewModel): ElementData {
        val language = cellViewModel.language.value.toString() +
                if (cellViewModel.namespace.value.isNotBlank()) "::"+cellViewModel.namespace.value
                else ""

        val body = cellViewModel.body.text
        return ElementData(
            elementId = Uuid.random(),
            type = ElementType.TextualRepresentation,
            language = language,
            body = body
        )
    }

    /** the MD string of the currently active tab */
    override fun toString() = toMarkdownString(cells.map { toElementData(it) })
    val selectedIndex = mutableStateOf(0)

    /**
     * Reads a file directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation.
     * The textual models are, however, not compiled.
     */
    fun addTabAndCellList(sessionIdState: MutableState<Uuid>, fileCells: List<ElementData>) {
        this.cells.clear()
        fileCells.forEach { fileCell ->
            val model = CellViewModel(sessionIdState, this, refreshTrees, fileCell)
            this.cells.add(model)
        }
        refreshTrees()
    }


    /**
     * Move the selected cell-> up or down
     */
    fun onMoveRequest(index: Int, moveRequest: MoveRequest) {
        val currentCell = cells[index]
        if (moveRequest == MoveRequest.Up) {
            if (index == 0)
                return
            val aboveCell = cells[index - 1]
            cells[index - 1] = currentCell
            cells[index] = aboveCell
        } else if (moveRequest == MoveRequest.Down) {
            if (index == cells.size - 1)
                return
            val belowCell = cells[index + 1]
            cells[index + 1] = currentCell
            cells[index] = belowCell
        }
        reset()
        generateTableOfContents()
    }

    /**
     * Add an empty Cell at the given index
     */
    fun onAddRequest(index: Int) {
        val cellViewModel =
            CellViewModel(
                cellListViewModel = this,
                sessionIdState = sessionIdState,
                refreshTrees = editorTabsViewModel.refreshTrees,
                bodyState = mutableStateOf(TextFieldValue()),
            )
        cells.add(index, cellViewModel)
    }

    var close: (() -> Unit)? = null

    /**
     * Saves the currently open tab into its file.
     * Only for local Files
     */
    fun save() {
        sessionService.putCells(sessionIdState.value, nameState.value, cells.map { toElementData(it) })
    }

    /**
     * Resets the cells with its error messages.
     * This also affects the error messages and results.
     */
    fun reset() {
        for (cell in cells) {
            cell.clearView()
        }
    }



    fun generateTableOfContents() {
        references.reset()
        cells.filter { it.language.value == Language.MARKDOWN }.forEach {
            val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())
            val parser = Parser.builder().extensions(extensions).build()
            val string = it.body.text.ifEmpty { it.body.annotatedString.text }
            val document = parser.parse(string)
            references.generateRefReferenceOfElements(it, document)
        }
        references.generateHeadingNumbering()
    }
}
