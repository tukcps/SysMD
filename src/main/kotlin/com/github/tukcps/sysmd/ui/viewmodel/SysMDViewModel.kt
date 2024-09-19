package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.*
import com.github.tukcps.sysmd.compiler.*
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.rest.AgilaRepository.deleteProject
import com.github.tukcps.sysmd.rest.AgilaRepository.getBranchById
import com.github.tukcps.sysmd.rest.AgilaRepository.getBranches
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommitById
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommits
import com.github.tukcps.sysmd.rest.AgilaRepository.getElements
import com.github.tukcps.sysmd.rest.AgilaRepository.postCommit
import com.github.tukcps.sysmd.rest.AgilaRepository.postProject
import com.github.tukcps.sysmd.rest.entities.ProjectImplementation
import com.github.tukcps.sysmd.services.*
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.composables.*
import com.github.tukcps.sysmd.ui.openDummy
import com.github.tukcps.sysmlv2.entities.*
import com.github.tukcps.sysmlv2.entities.requestModels.DigitalTwinRequest
import java.io.File
import java.util.UUID


/**
 * View model for overall application
 */
class SysMDViewModel(
    var showDialogFilename: MutableState<Boolean> = mutableStateOf(false),
    var chosenFileName: MutableState<String> = mutableStateOf(""),
    var chosenFilePath: MutableState<String> = mutableStateOf(""),
    var session: Session   // Session in which the KerML model is edited, computed, ...
) {
    // The main window with editable files
    var kerMlModel = mutableStateOf(session)
    val tabsModel = TabsModel(kerMlModel, ::refreshTrees)
    val agenda = Agenda(kerMlModel)
    val outputDisplay = OutputDisplay(kerMlModel)
    var agendaIsEmpty = mutableStateOf(agenda.isEmpty())

    // The selectable tree views
    val files = mutableStateOf(TreeViewModel(AgilaFileTree(ModelsFolder), tabsModel::open,  ::createFile,::onDelete,::uploadProjectFromFile,true))
    val composition = mutableStateOf(TreeViewModel(AgilaCompositionTree(kerMlModel.value.global), ::openDummy, ::createOwnedElement,::onDelete, { _: TreeViewNodeModel, _:String ->  }, false))
    val inheritance = mutableStateOf(TreeViewModel(AgilaInheritanceTree(kerMlModel.value.any), ::openDummy, ::createSubclass, ::onDelete,{ _: TreeViewNodeModel, _:String ->  }, false))
    val projectsTree = mutableStateOf(TreeViewModel(AgilaProjectsTree(AgilaRepository.getProjects().toMutableList()), tabsModel::openProjectCommit, ::onCreateProject, ::onDelete,{ _: TreeViewNodeModel, _:String ->  }, false))

    val showSettingsDialog: MutableState<Boolean> = mutableStateOf(false)
    val reconnectionRequired:MutableState<Boolean> = mutableStateOf(false)
    val stateDiagramImageFile : MutableState<File>? = null

    //Manage the commit process
    val showDialogProjectName: MutableState<Boolean> = mutableStateOf(false)
    val chosenProjectDescription: MutableState<String> = mutableStateOf("")
    var chosenBranchName: MutableState<String> = mutableStateOf("")
    var chosenProjectName: MutableState<String> = mutableStateOf("")
    var chosenCommitName: MutableState<String> = mutableStateOf("")
    var chosenCommitDescription: MutableState<String> = mutableStateOf("")
    var showDialogBranchName:MutableState<Boolean> = mutableStateOf(false)
    var showDialogBranchDeletion:MutableState<Boolean> = mutableStateOf(false)
    var showDialogProjectAlreadyExits:MutableState<Boolean> = mutableStateOf(false)
    var chosenNameDigitalTwin:MutableState<String> = mutableStateOf("")
    var chosenModelsForDigitalTwin:MutableMap<UUID,MutableState<Boolean>> = mutableMapOf()
    var activeProject: ProjectImplementation?=null

    private lateinit var fileToDelete: File
    private lateinit var projectID: String

    init { refreshProjectChilds() }

    /**
     * Opens a file dialog and eventually creates a file.
     */
    fun createFile(n: TreeViewNodeModel) {
        chosenFileName.value = ""
        val file = n as AgilaFileTree
        chosenFilePath.value = n.file.path
        println("    Added file: ${file.file.path}")
        showDialogFilename.value = true
    }

    fun createProject(name: String, description: String) : Project {
        showDialogProjectName.value=false
        val postedProject = postProject(name, description)
        if (postedProject != null){
            val elementsDAOList = mutableListOf<ElementDAO>()
            elementsDAOList.add(
                ElementData(
                    elementId = UUID.randomUUID(),
                    body = "Initialization successful",
                    language = "Markdown",
                    name="Initialization",
                    type="TextualRepresentation"
                )
            )
            postCommit("InitialCommit.md","Initial Commit", postedProject.id, elementsDAOList, null)

            postedProject.branches = getBranches(postedProject.id).toMutableList()
            postedProject.commits = getCommits(postedProject.id).toMutableList()

            postedProject.defaultBranch = postedProject.defaultBranch?.let { getBranchById(postedProject.id, it.id) }
            postedProject.defaultBranch?.referencedCommit = getCommitById(postedProject.id, postedProject.defaultBranch?.referencedCommit?.id)

            (projectsTree.value.root as AgilaProjectsTree).projects.add(postedProject)
            refreshTrees()
            return postedProject
        }else{
            throw Exception("The project could not be created")
        }
    }

    fun createCommit(commitName: String, commitDescription: String){
        val activeEditorTab = tabsModel.active
        activeEditorTab?.validateCommit(commitName, commitDescription)
    }

    fun createBranch(projectBranch: Project, branchBasedOn: Branch, branchName: String) {
        AgilaRepository.postBranch(projectBranch.id,branchName, (branchBasedOn.referencedCommitId() ?: UUID::randomUUID) as UUID)
    }

    fun deleteBranch(project:Project,branch:Branch){
        AgilaRepository.deleteBranch(project.id,branch.id)
        reconnectToBackend()
    }

    fun onDelete(model: TreeViewNodeModel, fileNameOrProjectID: String)
    {
        if(model is AgilaProjectsTree){
            menuState.deleteProjectClicked.value=true
            projectID=fileNameOrProjectID
        }
        else if (model is AgilaFileTree) {
            menuState.deleteFileClicked.value=true
            fileToDelete = File(model.file.path +"/"+fileNameOrProjectID)
        }
    }

    fun deleteFile(){
        fileToDelete.delete()
        modelToUpdate.value.refresh()
    }

    fun deleteProject(){
        deleteProject(UUID.fromString(projectID))
        reconnectToBackend()
    }

    @Suppress("UNUSED_PARAMETER")
    fun createSubclass(n: TreeViewNodeModel) {}

    @Suppress("UNUSED_PARAMETER")
    fun createOwnedElement(n: TreeViewNodeModel) {}

    @Suppress("UNUSED_PARAMETER")
    fun onCreateProject(n: TreeViewNodeModel) {
        kerMlModel.value.checkConsistencyOfBuilders()
        showDialogProjectName.value = true
        kerMlModel.value.checkConsistencyOfBuilders()
    }

    /**
     * Resets the KerML model and re-loads the default libraries.
     */
    fun reset() {
        kerMlModel.value.reset()
        agenda.clear()
        tabsModel.reset()
        kerMlModel.value.loadDefaultProjects()
        refreshTrees()
    }

    /**
     * Redraws all tree-views by opening/closing them and also updates the agenda.
     * This function should be called after each change in the KerML model of a session.
     */
    fun refreshTrees() {
        composition.value.items[0].item.toggleExpanded()
        composition.value.items[0].item.toggleExpanded()
        inheritance.value.items[0].item.toggleExpanded()
        inheritance.value.items[0].item.toggleExpanded()
        projectsTree.value.items[0].item.toggleExpanded()
        projectsTree.value.items[0].item.toggleExpanded()
        agenda.clear()
        agenda.update()
        agendaIsEmpty.value = agenda.isEmpty()
        outputDisplay.update()
    }

    /**
     * Compiles all open tabs.
     */
    fun compile() {
        agenda.clear()
        kerMlModel.value.status.exceptions.clear()
        tabsModel.editorTabs.forEach {
            if (it is EditorTabModel) {
                it.cells.forEach { cell ->
                    if (cell.language.value == TextualRepresentationViewModel.Companion.Language.YAML) {
                        kerMlModel.value.importMD(cell.body.value.text, null)
                    }
                    if (cell.language.value == TextualRepresentationViewModel.Companion.Language.TABLE) {
                        cell.tableViewModel.value.toText()
                    }
                }
            }
        }
        kerMlModel.value.loadUsage()
        tabsModel.editorTabs.forEach {
            if (it is EditorTabModel) {
                it.cells.forEach { cell ->
                    cell.compile(propagate = false)
                }
            }
        }
        kerMlModel.value.initialize()
        kerMlModel.value.propagate()
        tabsModel.editorTabs.forEach { editorTabModel ->
            if (editorTabModel is EditorTabModel)
                editorTabModel.cells.forEach { cell -> cell.display() }
        }
        refreshTrees()  // refreshes tree-views and agenda
    }

    /**
     * Creates a file and opens it in a tab.
     */
    fun createFile() {
        kerMlModel.value.checkConsistencyOfBuilders()
        tabsModel.create(chosenFilePath.value, chosenFileName.value)
        showDialogFilename.value = false
        kerMlModel.value.checkConsistencyOfBuilders()
        files.value.refresh()
    }

    fun refreshProjectChilds() {
        AgilaRepository.getProjects()
        if(AgilaRepository.onlineState.value) {
            for (project in AgilaRepository.projectsState) {
                project.branches = getBranches(project.id).toMutableList()
                project.commits = getCommits(project.id).toMutableList()

                project.defaultBranch = project.defaultBranch?.let { getBranchById(project.id, it.id) }
                if (project.commits.isNotEmpty())
                    project.defaultBranch?.referencedCommit =
                        getCommitById(project.id, project.defaultBranch?.referencedCommit?.id)
            }
        }

        (projectsTree.value.root as AgilaProjectsTree).projects = AgilaRepository.projectsState.toMutableList()
        projectsTree.value.refresh()
    }

    fun reconnectToBackend() {
        AgilaRepository.getProjects()

        refreshProjectChilds()

        AgilaRepository.onlineState.value = (AgilaRepository.projectsState.isNotEmpty())

        (projectsTree.value.root as AgilaProjectsTree).projects = AgilaRepository.projectsState.toMutableList()
        projectsTree.value.refresh()

    }

    fun uploadProjectFromFile(model:TreeViewNodeModel, filename:String){
        val name = filename.split(".").first()

        if(checkIfProjectAlreadyExits(name))
        {
            showDialogProjectAlreadyExits.value=true
            return
        }

        val project = createProject(name,name)

        for(child in model.children()){
            if (child.name == filename)
            {
                tabsModel.open(child)
                tabsModel.active?.owningProjectId = project.id
                val commitName = "Uploaded Data from Client"
                val commitDescription = "Uploaded Data from Client"
                tabsModel.active?.setUpCommit(commitName, commitDescription)
                tabsModel.active?.validateCommit(commitName, commitDescription)
                tabsModel.reset()
            }
        }
        reconnectToBackend()
    }

    fun checkIfProjectAlreadyExits(name:String) : Boolean{
        for (project in AgilaRepository.projectsState)
            if(project.name==name)
                return true

        return false
    }

    fun pull() {
        try {
            var activeTab = tabsModel.active
            val idList = arrayListOf<UUID>()
            while (activeTab!=null) {
                idList.add(activeTab.owningProjectId !!)
                tabsModel.close(activeTab,kerMlModel.value)
                activeTab = tabsModel.active
            }
            reconnectToBackend()
            for (element in idList)
            {
                AgilaRepository.projectsState.forEach {
                    if (it.id == element) {
                        val branch = getBranchById(it.id, it.defaultBranch?.id ?: UUID.randomUUID())
                        tabsModel.openCommit(getCommitById(it.id, branch.referencedCommit?.id ?: UUID.randomUUID()), it)
                    }
                }
            }
        }
        catch (error: Exception) {
            println("Error in pull: ${error.message}")
        }
    }

    fun getAllElementsProxy(projectName:String): MutableList<ElementDAO> {
        for(proj in AgilaRepository.getProjects()){
            if(proj.name==projectName){
                activeProject=proj
            }
        }
        if (activeProject != null) {
            return getElements(activeProject!!.id, tabsModel.active!!.commitId!!)
        }
        return mutableListOf()
    }

    fun createDigitalTwin()
    {
        val selectedModels = mutableListOf<UUID>()
        for(key in chosenModelsForDigitalTwin.keys)
        {
            if(chosenModelsForDigitalTwin[key]?.value == true)
            {
                selectedModels.add(key)
            }
        }

        activeProject?.let { AgilaRepository.postDigitalTwin(it.id, DigitalTwinRequest(name=chosenNameDigitalTwin.value, commitId= tabsModel.active?.commitId!!, connectedElements = selectedModels)) }
    }
}
