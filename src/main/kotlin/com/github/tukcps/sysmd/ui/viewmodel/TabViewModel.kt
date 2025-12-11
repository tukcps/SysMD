package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.MetadataFeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.model.util.dropFirstName
import com.github.tukcps.sysmd.rest.RESTRepository.getCellsForUi
import com.github.tukcps.sysmd.rest.RESTRepository.getCommit
import com.github.tukcps.sysmd.rest.RESTRepository.getElementById
import com.github.tukcps.sysmd.services.repositories.local.SysMDElementNavigationService.getElements
import com.github.tukcps.sysmd.services.repositories.local.toElement
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.MoveRequest
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.language
import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.Project
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import java.io.File


/**
 * The view model of an editor tab.
 * As of now, we just use a simple list of Elements for a package.
 * This might change to a tree in line with the SysMLv2 Metamodel.
 *
 * For the tab, we save
 *  - a list of elements that can be edited; an element is a Package or an Element.
 *  - the model (must be shared, if several tabs are active, not yet done)
 *  - the selected index of an element, and its section (description, code)
 * @param sessionState the internal model that is the result of the compilation and analysis;
 *        does not trigger any updates of any views.
 * @param refreshTrees lambda that can be called when a refresh of the tree-views is needed
 */
class TabViewModel(
    val tabsViewModel: TabsViewModel,
    val sessionState: MutableState<Session>,
    val refreshTrees: () -> Unit
) {
    var file: File? = null
    val session by sessionState
    val tabTitle: MutableState<String> = mutableStateOf("")

    lateinit var openFileInNewTab: (File) -> Unit

    /** The collapsed element ids in the tab */
    val collapsedCellIndices: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    /** The hidden element ids in the tab */
    val hiddenElementIds: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    // Reference into the SysMD model.
    var fileAnnotation: Namespace? = null

    /** The states of the view model. */
    val cells = mutableStateListOf<TextualRepresentationViewModel>()

    var scrollState = LazyListState()
    val editState =  mutableStateOf(false)

    private var commitList = mutableListOf<Commit>()
    private var oldCommitElementsDAOList = mutableListOf<ElementDAO>()
    private var newCommitElementsDaoList = mutableListOf<ElementDAO>()
    private var doPostCommit = mutableStateOf(false)

    val elementEdited: MutableState<Boolean> = mutableStateOf(false)
    val references = InternalRefReference(editorTabModel = this) { generateTableOfContents() }

    /**
     * Transform the current editor tab to a single string in Markdown
     */
    private fun toMarkdownString(): String {
        var str = ""
        for (e in cells) {
            // The lines of the description section.
            val languageStr = if (e.language.value !in setOf(Language.MARKDOWN, Language.YAML)) {
                e.language.value.toString()+if (e.namespace.value.isNotBlank()) "::"+e.namespace.value else ""
            } else ""
            if (e.language.value !in setOf(Language.MARKDOWN, Language.YAML))
                str += "```$languageStr\n"
            str += e.body.text.trimEnd('\n') + "\n"
            if (e.language.value !in setOf(Language.MARKDOWN, Language.YAML))
                str += "```\n"
        }
        return str
    }

    /** the MD string of the currently active tab */
    override fun toString() = toMarkdownString()
    val selectedIndex = mutableStateOf(0)

    /**
     * Reads a file directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation.
     * The textual models are, however, not compiled.
     * @param file The file that is read into the view-model.
     */
    fun open(file: File, sessionState: MutableState<Session>) {
        /**
         * Adds a TextualRepresentation view model to the model elements.
         */
        fun buildViewModelFromModel(element: Element) {
            element.ownedElement.forEach { e ->
                if (e is TextualRepresentation) {
                    val elementModel = TextualRepresentationViewModel(
                        tabViewModel = this,
                        sessionState = sessionState,
                        bodyState = mutableStateOf(TextFieldValue(e.body)),
                        refreshTrees = refreshTrees
                    )
                    e.language.let { string ->
                        elementModel.language.value = language[string.split("::").first()]
                            ?:Language.MARKDOWN }
                    elementModel.namespace.value = e.language.dropFirstName()
                    cells.add(elementModel)
                }
            }
            generateTableOfContents()
        }

        if (file.isFile) {
            try {
                this.tabTitle.value = file.name
                this.file = file
                fileAnnotation = session.global.getOwned<Namespace>(name = file.name)
                buildViewModelFromModel(fileAnnotation!!)
                refreshTrees()
            } catch (error: Exception) {
                logger.error(error.message, error)
            }
        }
    }


    /**
     * Move the selected cell-> up or down
     */
    fun onMoveRequest(index: Int, moveRequest: MoveRequest) {
        val currentCell = cells[index]
        if (moveRequest == MoveRequest.Up) {
            if (index == 0) {
                return
            }
            val aboveCell = cells[index - 1]
            cells[index - 1] = currentCell
            cells[index] = aboveCell
        } else if (moveRequest == MoveRequest.Down) {
            if (index == cells.size - 1) {
                return
            }
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
        val textualRepresentationViewModel =
            TextualRepresentationViewModel(
                tabViewModel = this,
                sessionState = sessionState,
                refreshTrees = refreshTrees,
                bodyState = mutableStateOf(TextFieldValue()),
            )
        cells.add(index, textualRepresentationViewModel)
    }

    /**
     * Reads Commit directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation. The textual models are, however,
     * not compiled.
     * @param commit The commit that is read into the view-model.
     * @param project The Project the commit is opened
     * @param kerMlModel the Session Model that is required
     */
    fun openSingleCommit(commit: Commit, project: Project, kerMlModel: Session) {
        fileAnnotation = MetadataFeatureImplementation()
        fileAnnotation = kerMlModel.addOwnedMember(fileAnnotation!!, kerMlModel.global)
        // fileName.value = commitTree.commit.name.toString()

        /**
         *  TODO(Order so that the first commit in the ThreeView has the last State (last commit)
         *          for(i in commitList.size-1 downTo  0)
         */
        commitList = getCommit(project).toMutableList()
        // get all textual Representation Elements of the last Commit to know the initial State of the model Elements later

        var elementModel: TextualRepresentationViewModel
        oldCommitElementsDAOList = getElements(project, commit).toMutableList()
        val oldCommitElementsIds = getCellsForUi(project, commit)
        println("====> Old Number of Ids: " + oldCommitElementsIds.size)

        for (elemID in oldCommitElementsIds) {
            val e = getElementById(project, commit, elemID)
            val element = e.toElement()

            if (element is TextualRepresentation) {
                element.model = kerMlModel
                elementModel = TextualRepresentationViewModel(
                    tabViewModel = this,
                    sessionState = mutableStateOf(kerMlModel),
                    language = mutableStateOf(Language.SYS_MD),
                    bodyState = mutableStateOf(TextFieldValue(text = element.body)),
                    refreshTrees = refreshTrees
                )
                when (e.language) {
                    "SysMD"      -> elementModel.language.value = Language.SYS_MD
                    "SysML"      -> elementModel.language.value = Language.SYS_ML
                    "YAML"       -> elementModel.language.value = Language.YAML
                    else         -> elementModel.language.value = Language.MARKDOWN
                }
                cells.add(elementModel)
            }
        }
        generateTableOfContents()
    }

    var close: (() -> Unit)? = null

    /**
     * Compile all cells; first, only translation, after all are compiled, calls
     * solver and updates display lines with errors/infos.
     */
    fun compile() {
        sessionState.value.loadUsages()
        cells.forEach { cell ->
            sessionState.value.status.reset()
            cell.compile(propagate = false)
        }
        cells.forEach { it.collectVariablesToDisplay() }
    }


    /**
     * Saves the currently open tab into its file.
     * Only for local Files
     */
    fun save() {
        val str = toMarkdownString()
        file?.writeText(str)
    }

    /**
     * Resets the elements with its error messages.
     * This also affects the error messages and results.
     */
    fun reset() {
        for (cell in cells) {
            cell.clearView()
        }
    }

    /**
     * Sets Up the environment for a Commit
     * Checks all the conditions before a Commit
     */
    fun checkBeforeCommit(editorTab: TabViewModel) {
        val bodyEdited = editorTab.elementEdited.value
        val languageChanged = mutableStateOf(false)

        for (oeDAO in oldCommitElementsDAOList) {
            for (neDAO in newCommitElementsDaoList) {
                if (oeDAO.elementId == neDAO.elementId) {
                    if (oeDAO.language != neDAO.language) {
                        languageChanged.value = true
                    }
                }
            }
        }
        doPostCommit.value = (bodyEdited || languageChanged.value)
    }


    /**
     * Commits to the DataBase
     * TODO (This should also commit a  local project as new project in the Database?)
     */
    fun validateCommit(name: String, description: String) {
        // postCommit("${name}.md", description, kerMlModel.value.project!!.id, newCommitElementsDaoList, branchId = branchId)
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
