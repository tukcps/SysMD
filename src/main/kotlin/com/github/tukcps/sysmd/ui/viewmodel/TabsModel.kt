package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.model.kerml.getOwnedByIndex
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.loadProjectSourceOnly
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.repositories.local.SysMDProjectService
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmlv2.entities.Branch
import com.github.tukcps.sysmlv2.entities.Commit
import com.github.tukcps.sysmlv2.entities.Project
import java.io.File

/**
 * The view model of the opened file-editor tabs.
 * A list of tabs, among which one is active for editing, or
 * null if none is yet open.
 * @param kerMlModelState the state of the internal KerML model, created by the SysMD compiler
 * @param refreshTrees lambda to be called when refresh of tree views is needed.
 */
class TabsModel(
    val kerMlModelState: MutableState<Session>,
    private var refreshTrees: () -> Unit,
) {
    val selectedIndex: MutableState<Int> = mutableStateOf(0)
    private val kerMlModel: Session by kerMlModelState
    val editorTabs = mutableStateListOf<TabModel>()
    val active: TabModel? get() = editorTabs.getOrNull(selectedIndex.value)

    /**
     * Functions to be called for resetting the model.
     * Reset will delete all generated models, but NOT the annotations in which the source code is saved.
     */
    fun reset() {
        try {
            if (SessionManager.projectService is SysMDProjectService) {
                (SessionManager.projectService as SysMDProjectService).reset()
                editorTabs.filterIsInstance<EditorTabModel>().forEach { tab ->
                    tab.reset()
                    tab.close = { close(tab, kerMlModel) }
                    tab.openFileInNewTab = ::open
                    kerMlModel.loadProjectSourceOnly(projectName = tab.file!!.name.dropLast(3)) // without .md
                    val annotation = kerMlModel.global.getOwned<AnnotatingElement>(tab.file!!.name)
                    if (annotation == null) {
                        kerMlModel.report(null, "File ${tab.file}.md not part of project; closing tab.")
                        close(tab, kerMlModel)
                    } else {
                        tab.fileAnnotation = annotation
                        var index = 0
                        tab.cells.forEach { cell ->
                            val existingTextualRepresentation = annotation.getOwnedByIndex<TextualRepresentation>(index)
                            index += 1
                            val language = cell.language.value.toString()
                            if (existingTextualRepresentation != null) {
                                cell.textualRepresentation = existingTextualRepresentation
                                existingTextualRepresentation.language = language
                                existingTextualRepresentation.body = cell.body.value.text
                            } else
                                cell.textualRepresentation = kerMlModel.create(
                                    TextualRepresentationImplementation(
                                        language = language,
                                        body = cell.body.value.text
                                    ),
                                    tab.fileAnnotation!!
                                )
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            kerMlModel.report(null, "Issue in reset routine: $exception", exception)
        }
    }

    /**
     * adding a new DisplayTabModel to display a graph
     */
    fun addDisplayTabModel(tabModel: DisplayTabModel) {
        var model = tabModel
        val existing: TabModel? =
            editorTabs.find { foo: TabModel -> foo is DisplayTabModel && foo.name == model.name }

        if (existing == null) {
            editorTabs.add(model)
            model.close = { close(model, kerMlModel) }
        } else {
            model = existing as DisplayTabModel
        }
    }

    /**
     * Loads a file into the view model.
     */
    fun open(fileTree: TreeViewNodeModel) {
        val file = (fileTree as AgilaFileTree).file
        open(file)
    }


    /**
     * Loads a file into the view models
     * @param file the file to be loaded
     */
    fun open(file: File) = open(file, true)
    fun open(file: File, addFiles: Boolean) {
        var editorTab = EditorTabModel(kerMlModelState, refreshTrees)
        editorTab.openFileInNewTab = ::open
        val existing: TabModel? = editorTabs.find {
                thisTab -> thisTab is EditorTabModel && thisTab.file == file
        }
        if (existing == null) {
            editorTab.open(file, kerMlModel)
            editorTab.close = { close(editorTab, kerMlModel) }
            editorTabs.add(editorTab)
        } else {
            editorTab = existing as EditorTabModel
        }
        if (addFiles) {
            kerMlModel.files.forEach {
                val fileOfIt = File(file.parent, it)
                open(fileOfIt, false)
            }
        }
    }

    /**
     * This function gets called if a Commit ( first Level Child of a project) is clicked in the GUI left tree view
     */
    fun openProjectCommit(commitTree: TreeViewNodeModel) {
        when (commitTree) {
            is AgilaOpenProjectNode ->
                commitTree.project.defaultBranch?.referencedCommit?.let {
                    openCommit(it, commitTree.project, commitTree.project.defaultBranch)
                }
            is AgilaBranchTree      -> commitTree.branch.referencedCommit?.let {
                openCommit(it, commitTree.project, commitTree.branch)
            }
            is AgilaCommitTree      ->
                //Todo Branch von commit erhalten?
                openCommit(commitTree.commit, commitTree.project)
            else -> kerMlModel.report(kerMlModel.global, "AGILA Backend not connected.")
        }
    }

    fun openCommit(commit: Commit, project: Project, branch: Branch?=null):EditorTabModel{
       var editorTab = EditorTabModel(kerMlModelState, refreshTrees)
       val existing: TabModel? = editorTabs.find { thisTab: TabModel ->
           thisTab is EditorTabModel && (thisTab.commitId == commit.id)
       }

       if (existing == null) {
           editorTab.tabTitle.value = project.name
           editorTab.commitId = commit.id

           if (branch != null) {
               editorTab.branchId = branch.id
           }
           editorTab.openSingleCommit(commit, project, kerMlModel)
           editorTab.close = { close(editorTab, kerMlModel) }
           editorTabs.add(editorTab)
       } else {
           editorTab = existing as EditorTabModel
       }
       return editorTab
   }


    /**
     * Creates a file and loads it into the view model; display should then show a new tab.
     */
    fun create(path: String, name: String, description: String = "") {
        val newFile = File(path, name)
        newFile.createNewFile()
        newFile.writeText("""
            ---
            title:
            name: ${name.removeSuffix(".md")}
            maintainer: 
            version: 
            website: 
            files: 
            usage: 
            description: $description
            ---
            [toc]
            # Cell with documentation
            Write the documentation in Markdown-Cells
            Text 
            ```SysMD
            // Write the model in SysMD-Cells
            package hello {
                attribute world: ScalarValues::Real = 1.0 + [2.0 .. 3.0]; 
            }
            ```
        """.trimIndent())
    }

    /** Closes an editor tab. */
    fun close(tab: TabModel, kerMlModel: Session) {
        try {
            editorTabs.remove(tab)
            if (tab is EditorTabModel && tab.fileAnnotation != null)
                kerMlModel.delete(tab.fileAnnotation!!)
            selectedIndex.value = selectedIndex.value.coerceAtMost(editorTabs.lastIndex)
        } catch (e: Exception) {
            selectedIndex.value = selectedIndex.value.coerceAtMost(editorTabs.lastIndex)
            println(e.message)
            println(e.stackTrace)
            println(" --> Resetting KerML model.")
        }
    }

}