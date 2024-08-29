package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.rendering.TreeNodeViewModel
import java.util.*


/**
 * Tab that displays a graph.
 */
class DisplayTabModel(
    override val kerMlModel: MutableState<Session>,
    val treeModel: TreeNodeViewModel,
    val isA: Boolean = false,
): TabModel {
    override var tabTitle: MutableState<String> = mutableStateOf("")

    var name = treeModel.name
    override var doPostCommit = mutableStateOf(false)
    override var owningProjectId: UUID? = null
    override var commitId:UUID? = null
    override var branchId:UUID? = null

    override var close: (() -> Unit)? = null

    fun isIsA(): Boolean{
        return isA
    }

    /** Compiling a display tab model does nothing */
    override fun compile() {}

    /** Saving a display tab model does nothing */
    override fun save() { }
}