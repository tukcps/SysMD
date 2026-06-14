package com.github.tukcps.sysmd.ui.syntaxhighlighting

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.logging.log4j.LogManager

/**
 * The global index of components.
 * Note: The component name is stored together with the name of the package it belongs to as one single string.
 * The format looks like this: *component_name::package_name*
 * When using SysMD the name of the package must be in front of the component name.
 */
var globalComponentsIndex : MutableSet<String> = mutableSetOf("Anything::Base") //As "Anything" is a build in class, its declaration cannot be found in the files and therefore is initially added here

/**The global index of Packages*/
var globalPackagesIndex : MutableSet<String> = mutableSetOf("ISQ")


val indexerScope = CoroutineScope(Dispatchers.Default)


/**
 * The Indexer is used to create Indexes for Packages and Components defined in the projects.
 * The Indexes can then be used to provide suggestions to the user when writing or highlight the text.
 */
object Indexer {

    var indexerSession: Session? = null
    var editorTabsViewModel: EditorTabsViewModel? = null
    var updatedTextFields = mutableSetOf<TextFieldValue>()
    
    fun indexAllTabs() {
        indexerSession?.status?.reset()
        editorTabsViewModel?.editorTabs?.forEach {
            it.cells.forEach { cell ->
                editorTabsViewModel?.sessionIdState?.value?.let { sessionId ->
                sessionService.updateModel(
                    session = sessionId,
                    code = cell.body.text,
                    language = cell.language.value,
                    namespace = cell.namespace.value,
                    runlevel = Runlevel.NAMES_RESOLVED
                )
                }
            }
        }
    }

    fun addChangedEditorCell(text: TextFieldValue) {
        updatedTextFields.add(text)
    }

    /**
     * Initializes the Index lists by scanning all Markdown files of the SysMD data Folder
     */
    fun initializeIndexes(editorTabsViewModel: EditorTabsViewModel) {
        try {
            Indexer.editorTabsViewModel = editorTabsViewModel
            indexerSession = SessionImplementation()
            indexAllTabs()
            logger.info("Indexed project $editorTabsViewModel with ${indexerSession?.status?.issues?.size} issues")
        } catch (e: Exception) {
            logger.info(e.message)
        }
    }

    private fun indexElements(
        ele: Element,
        tempPackages: MutableState<MutableSet<String>>,
        tempComponents: MutableState<MutableSet<String>>
    ){

        when(ele.javaClass.simpleName.toString()){
            "PackageImplementation" -> tempPackages.value.add(ele.declaredName.toString())
            "ClassImplementation","PartUsageImplementation","PartDefinitionImplementation" -> tempComponents.value.add(ele.declaredName.toString())
        }

        //Recursively call this function on element children
        ele.ownedElement.forEach { child -> indexElements(child, tempPackages, tempComponents) }
    }


    /**Updates the global indexes by comparing and old Index to a new Index.
     * New entries will be added to the global index while deleted entries will be removed.
     */
    private fun MutableSet<String>.applyLocalChangesToGlobal(oldIndex : MutableSet<String>, newIndex : MutableSet<String>){
        //What exists in the OLD but NOT in the NEW index has to be REMOVED
        this.removeAll(oldIndex.subtract(newIndex))

        //What exists in the NEW but NOT in the OLD index has to be ADDED
        this.addAll(newIndex.subtract(oldIndex))

    }

    private val logger = LogManager.getLogger(Indexer::class.java)
}