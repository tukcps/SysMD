@file:Suppress("unused")

package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.viewmodel.CellListViewModel
import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.Project

class WebProject {
    private var commitList = mutableListOf<Commit>()
    private var oldCommitElementsDAOList = mutableListOf<ElementDAO>()
    private var newCommitElementsDaoList = mutableListOf<ElementDAO>()
    private var doPostCommit = mutableStateOf(false)
    /**
     * Reads Commit directly into the view-model. It creates annotation elements that
     * hold the textual models and the comments for documentation. The textual models are, however,
     * not compiled.
     * @param commit The commit that is read into the view-model.
     * @param project The Project the commit is opened
     * @param kerMlModel the Session Model that is required
     */
    fun openSingleCommit(commit: Commit, project: Project, kerMlModel: Session) {}


    /**
     * Sets Up the environment for a Commit
     * Checks all the conditions before a Commit
     */
    fun checkBeforeCommit(editorTab: CellListViewModel) {
        val bodyEdited = editorTab.hasChangesState.value
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

}