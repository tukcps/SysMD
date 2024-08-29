package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.parser.dropFirstName
import com.github.tukcps.sysmd.compiler.loadProjectSourceOnly
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommits
import com.github.tukcps.sysmd.rest.AgilaRepository.getElementById
import com.github.tukcps.sysmd.rest.AgilaRepository.getElements
import com.github.tukcps.sysmd.rest.AgilaRepository.getElementsForUi2
import com.github.tukcps.sysmd.rest.AgilaRepository.postCommit
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.toElement
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.MoveRequest
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.language
import com.github.tukcps.sysmlv2.entities.Commit
import com.github.tukcps.sysmlv2.entities.ElementDAO
import com.github.tukcps.sysmlv2.entities.Identified
import com.github.tukcps.sysmlv2.entities.Project
import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import java.io.File
import java.util.*


/**
 * The view model of an editor tab.
 * As of now, we just use a simple list of Elements for a package.
 * This might change to a tree in line with the SysMLv2 Metamodel.
 *
 * For the tab, we save
 *  - a list of elements that can be edited; an element is a Package or an Element.
 *  - the agila model (must be shared, if several tabs are active, not yet done)
 *  - the selected index of an element, and its section (description, code)
 * @param kerMlModel internal model that is the result of the compilation and analysis;
 *        does not trigger any updates of any views.
 * @param refreshTrees lambda that can be called when a refresh of the tree-views is needed
 */
class EditorTabModel(
    override val kerMlModel: MutableState<Session>,
    val refreshTrees: () -> Unit
) : TabModel {
    var file: File? = null
    override var tabTitle: MutableState<String> = mutableStateOf("")

    lateinit var openFileInNewTab: (File) -> Unit

    /** The collapsed element ids in the tab */
    val collapsedElementIds: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    /** The hidden element ids in the tab */
    val hiddenElementIds: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()

    // Reference into the SysMD model.
    var fileAnnotation: AnnotatingElement? = null

    /** The states of the view model. */
    val cells = mutableStateListOf<TextualRepresentationViewModel>()

    var scrollState = LazyListState()
    val editState =  mutableStateOf(false)
    val showInfo = cells.indices.associateWith {  mutableStateOf(true) }

    private var commitList = mutableListOf<Commit>()
    private var oldCommitElementsDAOList = mutableListOf<ElementDAO>()
    private var newCommitElementsDaoList = mutableListOf<ElementDAO>()
    override var doPostCommit = mutableStateOf(false)
    override var commitId:UUID? = null
    override var branchId:UUID? = null
    override var owningProjectId: UUID? = null

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
            str += e.body.value.text.trimEnd('\n') + "\n"
            if (e.language.value !in setOf(Language.MARKDOWN, Language.YAML))
                str += "```\n"
        }
        return str
    }

    /** the MD string of the currently active tab */
    override fun toString() = toMarkdownString()
    val selectedIndex = mutableStateOf(0)

    /**
     * Reads file directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation.
     * The textual models are, however, not compiled.
     * @param file The file that is read into the view-model.
     */
    override fun open(file: File, kerMlModel: Session) {
        /**
         * Adds a TextualRepresentation view model to the model elements.
         */
        fun buildViewModelFromModel(
            element: Element,
            modelElements: SnapshotStateList<TextualRepresentationViewModel>
        ) {
            element.ownedElement.forEach {
                val e = it.ref!!
                if (e is TextualRepresentation) {
                    val elementModel = TextualRepresentationViewModel(
                        kerMlModel = mutableStateOf(kerMlModel),
                        body = mutableStateOf(TextFieldValue(e.body)),
                        textualRepresentation = e,
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
                this.tabTitle.value = file.name.dropLast(3)
                this.file = file
                kerMlModel.loadProjectSourceOnly(projectName = file.name.dropLast(3).trim('/', '\\') )
                fileAnnotation = kerMlModel.global.getOwned<AnnotatingElement>(name = file.name)
                buildViewModelFromModel(fileAnnotation!!, cells)
                refreshTrees()
            } catch (error: Exception) {
                kerMlModel.report(exception = error)
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
                kerMlModel = kerMlModel,
                refreshTrees = refreshTrees,
                textualRepresentation = TextualRepresentationImplementation(language = "Markdown", body = ""),
                body = mutableStateOf(TextFieldValue()),
            )
        kerMlModel.value.create(
            textualRepresentationViewModel.textualRepresentation,
            kerMlModel.value.global
        )
        cells.add(index, textualRepresentationViewModel)
    }

    /**
     * Reads Commit directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation. The textual models are however
     * not compiled.
     * @param commit The commit that is read into the view-model.
     * @param project The Project the commit is opened
     * @param kerMlModel the Agila Session Model, that is required
     */
    fun openSingleCommit(commit: Commit, project: Project, kerMlModel: Session) {
        fileAnnotation = AnnotatingElementImplementation(declaredName = commit.description, body = "")
        fileAnnotation = kerMlModel.create(fileAnnotation!!, kerMlModel.global)
        // fileName.value = commitTree.commit.name.toString()
        owningProjectId = project.id

        /**
         *  TODO(Order so that the first commit in the ThreeView has the last State (last commit)
         *          for(i in commitList.size-1 downTo  0)
         */
        commitList = getCommits(project.id).toMutableList()
        // get all textual Representation Elements of the last Commit to know the initial State of the model Elements later

        var elementModel: TextualRepresentationViewModel
        oldCommitElementsDAOList = getElements(project.id, commit.id)
        val oldCommitElementsIds = getElementsForUi2(project.id, commit.id)
        println("====> Old Number of Ids: " + oldCommitElementsIds.size)

        for (elemID in oldCommitElementsIds) {
            val e = getElementById(project.id, commit.id, elemID)
            val element = e.toElement()

            if (element is TextualRepresentation) {
                element.model = kerMlModel
                elementModel = TextualRepresentationViewModel(
                    kerMlModel = mutableStateOf(kerMlModel),
                    language = mutableStateOf(Language.SYS_MD),
                    body = mutableStateOf(TextFieldValue(text = element.body)),
                    textualRepresentation = element,
                    refreshTrees = refreshTrees
                )
                when (e.language) {
                    "SysMD" -> elementModel.language.value = Language.SYS_MD
                    "SysML" -> elementModel.language.value = Language.SYS_ML
                    "FormSysMd" -> elementModel.language.value = Language.FORM
                    else -> elementModel.language.value = Language.MARKDOWN
                }
                cells.add(elementModel)
            }
        }
        generateTableOfContents()
    }

    override var close: (() -> Unit)? = null

    /**
     * Compile all cells; first, only translation, after all are compiled, calls
     * solver and updates display lines with errors/infos.
     */
    override fun compile() {
        kerMlModel.value.loadUsage()
        cells.forEach { cell ->
            kerMlModel.value.status.exceptions.clear()
            cell.compile(propagate = false)
        }
        cells.forEach { it.display() }
    }


    /**
     * Saves the currently open tab into its file.
     * Only for local Files
     */
    override fun save() {
        val str = toMarkdownString()
        file?.writeText(str)
    }

    /**
     * Resets the elements with its error messages.
     * This also affects the error messages and results.
     */
    fun reset() {
        for (element in cells)
            element.reset()
    }

    /**
     * Sets Up the environment for a Commit
     * Checks all the conditions before a Commit
     */
    override fun checkBeforeCommit(editorTab: EditorTabModel) {
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

    override fun setUpCommit(commitName: String, commitDescription: String) {
        val ownedElements = mutableListOf<Identified>()

        // Create the AE
        val annotatedElement = ElementData(
            elementId = UUID.randomUUID(),
            name = "$commitName.md",
            type = "AnnotatingElement",
            ownedElements = ownedElements,
            body = ""
        )
        newCommitElementsDaoList.add(annotatedElement)

        // TextualRepresentation information
        var id: UUID?
        var name: String?
        var shortName: String?
        val type = "TextualRepresentation"
        var owner: UUID?
        var language: String
        var body: String

        var elementDAO: ElementDAO
        // Create ElementDAOList from Textual Representation View Model
        for (elementViewModel in cells) {
            /**
             *  Comparing bodies to identify Updated elements that should keep their ID requires high time execution
             *  Even with the implementation of versions for each Element ( cells ) . It will not make sense, because, we won't be able to
             *  identify edited cells without comparing Bodies ( which is what we want to avoid.)
             */

            /**
            preserves the same ID over different Commits
             */

            //id = elementViewModel.textualRepresentation.id
            /**
             * Always creates new IDs also for the same elements in the new Commit  ->  avoids override
             */
            id = UUID.randomUUID() // if not, edits or delete will overwrite previous Versions.
            name = elementViewModel.textualRepresentation.declaredName
            shortName = elementViewModel.textualRepresentation.declaredShortName
            owner = annotatedElement.elementId
            language = elementViewModel.language.value.toString()
            body = elementViewModel.body.value.text

            // new ElementDAO
            elementDAO = ElementData(
                elementId = id,
                name = name,
                shortName = shortName,
                type = type,
                owner = Identified(owner),
                language = language,
                body = body
            )

            // Elements UUID for
            ownedElements.add(Identified(id!!))
            newCommitElementsDaoList.add(elementDAO)
        }
        println("====> New Number of ElementDAOs: " + newCommitElementsDaoList.size)
    }

    /**
     * Commits to the DataBase
     * TODO (This should also commit a  local project as new project in the Database?)
     */
    override fun validateCommit(name: String, description: String) {
        println("BranchId: $branchId")
        postCommit("${name}.md", description, owningProjectId!!, newCommitElementsDaoList, branchId = branchId)
    }

    fun generateTableOfContents() {
        references.reset()
        cells.filter { it.language.value == Language.MARKDOWN }.forEach {
            val extensions: List<Extension> = listOf(TablesExtension.create(), YamlFrontMatterExtension.create())
            val parser = Parser.builder().extensions(extensions).build()
            val string = it.textualRepresentation.body.ifEmpty { it.body.value.annotatedString.text }
            val document = parser.parse(string)
            references.generateRefReferenceOfElements(it, document)
        }
        references.generateHeadingNumbering()
    }
}
