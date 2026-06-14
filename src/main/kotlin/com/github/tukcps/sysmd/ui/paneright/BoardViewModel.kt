package com.github.tukcps.sysmd.ui.paneright

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionStatus
import kotlin.uuid.Uuid

/**
 * The class BoardViewModel stores and provides access to the agenda storing errors
 * which occurred on elements defined in SysMD cells
 */
class BoardViewModel(
    private val sessionIdState: MutableState<Uuid>
) {
    /**
     * The status of the overall model; includes among others a mutable set of infos, errors, etc.
     */
    val status: SessionStatus
        get() = SessionManager.sessionService.getSession(sessionIdState.value)?.status
            ?: SessionStatus()

    /**
     * Stores qualified names of all undefined elements
     */
    private val agenda: MutableList<IssueViewModel> = mutableStateListOf()

    /**
     * Adds elements to [agenda]
     */
    private fun addElement(issue: Issue) {
        if (!contains(issue)) agenda.add(IssueViewModel(issue))
    }

    /**
     * Clears [agenda]
     */
    fun clear() {
        agenda.clear()
    }

    /**
     * Returns true if agenda element found
     */
    fun contains(element: IssueViewModel): Boolean {
        return agenda.contains(element)
    }

    /**
     * Returns true if element is found
     */
    fun contains(issue: Issue): Boolean {
        return agenda.any { it.issue == issue }
    }

    /**
     * Returns all elements from [agenda][com.github.tukcps.sysmd.ui.paneright.BoardViewModel.agenda]
     */
    fun issues(): List<IssueViewModel> {
        return agenda
    }

    /**
     * Returns true if the agenda is empty
     */
    fun isEmpty(): Boolean {
        return agenda.isEmpty()
    }


    fun removeElement(qualifiedName: QualifiedName) {
        agenda.removeIf { it.qualifiedName == qualifiedName }
    }

    /**
     * Returns the number of all agenda elements of all types
     */
    fun size(): Int {
        return agenda.size
    }

    /**
     * Analyzes, sorts, manages the error and status message.
     */
    fun update() {
        status.issues.forEach {
            addElement(it)
        }
    }
}