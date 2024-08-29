package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import com.github.tukcps.sysmd.services.session.Session
import java.io.File
import java.util.*

/**
 * Interface of all tab view models.
 */
interface TabModel {
    /** The title of the tab */
    var tabTitle: MutableState<String>

    /** The session with the KerML model */
    val kerMlModel: MutableState<Session>
    var doPostCommit: MutableState<Boolean>

    var owningProjectId: UUID?
    var commitId: UUID?
    var branchId:UUID?

    // Lambdas for actions by Menu
    var close: (() -> Unit)?
    fun save() { }
    fun compile() { }
    fun open(file: File, kerMlModel: Session) { }
    fun setUpCommit(commitName: String, commitDescription: String) { }
    fun validateCommit(name: String, description: String){ }
    fun checkBeforeCommit(editorTab: EditorTabModel){ }
    fun pushCommits(){}
}