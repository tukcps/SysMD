package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * The view model of the opened file-editor tabs.
 * A list of tabs, among which one is active for editing, or
 * null if none is yet open.
 * @param sessionState the state of the internal KerML model, created by the SysMD compiler
 * @param refreshTrees lambda to be called when refresh of tree views is needed.
 */
class TabsViewModel(
    val sessionState: MutableState<Session>,
    private val refreshTrees: () -> Unit,
) {
    private val session: Session by sessionState
    val editorTabs = mutableStateListOf<TabViewModel>()
    val selectedIndex: MutableState<Int> = mutableStateOf(0)
    val active: TabViewModel? get() = editorTabs.getOrNull(selectedIndex.value)
    val removeFileDialog =  mutableStateOf(false)
    val removeFile =  mutableStateOf<Int?>(null)

    /**
     * Adds a new file to the view model and the local files.
     */
    fun addNewFile() {
        val maxIndex = editorTabs.size+1
        val file = sessionState.value.project?.addIndex("${sessionState.value.project!!.getIndex().size+1}", "Filename-$maxIndex.md") ?: return
        val newTab = TabViewModel(this, sessionState, refreshTrees)
        newTab.file = file
        newTab.tabTitle.value = " " + file.name + " "
        newTab.editState.value = true
        sessionState.value.loadSysMDFromFile(file, compile = false, 0)
        open(file, false)
        selectedIndex.value = editorTabs.size-1
    }

    fun removeFile() {
        if (removeFile.value != null) {
            val tab = editorTabs.getOrNull(removeFile.value!!)
            close(tab!!)
            if (tab.file != null)
                sessionState.value.project?.removeFromIndex(tab.file!!.name)
            if (selectedIndex.value > 1) selectedIndex.value -= 1
        }
    }

    /**
     * Functions to be called for resetting the model.
     * Reset will delete all generated models, but NOT the annotations in which the source code is saved.
     */
    fun reset() {
        try {
            editorTabs.forEach { tab ->
                tab.reset()
                tab.close = { close(tab) }
                tab.openFileInNewTab = ::open
            }
        } catch (exception: Exception) {
            logger.error( "Issue in reset of editor tabs: $exception", exception)
        }
    }


    /**
     * Loads a file into the view models
     * @param file the file to be loaded
     */
    fun open(file: File) = open(file, true)
    fun open(file: File, createFiles: Boolean) {
        var editorTab = TabViewModel(this, sessionState, refreshTrees)
        editorTab.openFileInNewTab = ::open
        val existing: TabViewModel? = editorTabs.find { tab -> tab.file == file }
        if (existing == null) {
            editorTab.open(file, sessionState)
            editorTab.close = { close(editorTab) }
            editorTabs.add(editorTab)
        } else {
            editorTab = existing
        }
        if (createFiles) {
            session.project?.getIndex()?.forEach {
                open(it, false)
            }
        }
    }

    /** Closes an editor tab. */
    fun close(tab: TabViewModel) {
        try {
            editorTabs.remove(tab)
            if (tab.fileAnnotation != null)
                tab.session.delete(tab.fileAnnotation!!)
            selectedIndex.value = selectedIndex.value.coerceAtMost(editorTabs.lastIndex)
        } catch (e: Exception) {
            selectedIndex.value = selectedIndex.value.coerceAtMost(editorTabs.lastIndex)
            logger.error(e.message)
            logger.info(" --> Resetting KerML model.")
        }
    }

    /**
     * Changes the name of a file/tab.
     * @param index the index in the list of tabs.
     * @param name new name of the tab.
     */
    fun rename(index: Int, name: String) {
        val tab = editorTabs[index]
        tab.fileAnnotation?.declaredName = "$name.md"
        sessionState.value.project!!.updateIndexFilename(tab.file!!.name, "$name.md")
        tab.tabTitle.value = " $name "
        tab.save()
        val newFile = File(tab.file!!.parent, "$name.md")
        tab.file!!.renameTo(newFile)
    }

    /**
     * Saves all open tabs/files and marks tab as saved
     */
    fun save() = editorTabs.forEach { tab ->
        tab.save()
        tab.elementEdited.value = false
    }

    /**
     * Searches a tab by its name and returns its index in the list of tabs; -1 if not there
     */
    fun findTabIndexByName(name: String): Int {
        var index = 0
        editorTabs.forEach {
            tab -> if (tab.file!!.name == name) return index
            index++
        }
        return -1
    }

    companion object {
        private val logger = LogManager.getLogger(TabsViewModel::class.java)
    }
}