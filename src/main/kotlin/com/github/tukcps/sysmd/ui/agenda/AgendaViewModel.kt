package com.github.tukcps.sysmd.ui.agenda

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionStatus

/**
 * The object Agenda stores and provides access to the agenda storing errors
 * which occurred on elements defined in SysMD cells
 */
class AgendaViewModel(
    private var sessionState: MutableState<Session>
) {
    /**
     * The status of the overall model; includes among others a mutable set of infos, errors, etc. .
     */
    val status: SessionStatus
        get() = sessionState.value.status

    /**
     * Stores qualified names of all undefined elements
     */
    private val agenda: MutableList<AgendaElement> = mutableStateListOf()

    /**
     * Adds elements to [agenda]
     */
    private fun addElement(
        qualifiedName: QualifiedName,
        exceptionClass: SysMDException, // TODO why?
        errorMessage: String = "",
        textualRepresentation: TextualRepresentation?,
        line: Int = -1
    ) {
        if (!contains(name = qualifiedName))
            agenda.add(
                AgendaElement(
                    qualifiedName = qualifiedName,
                    exceptionClass = exceptionClass,
                    errorMessage = errorMessage,
                    textualRepresentation = textualRepresentation,
                    line = line
                )
            )
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
    fun contains(element: AgendaElement): Boolean {
        return agenda.contains(element)
    }

    /**
     * Returns true if element is found
     */
    fun contains(name: QualifiedName): Boolean {
        return agenda.any { it.qualifiedName == name }
    }

    /**
     * Returns all elements from [agenda][com.github.tukcps.sysmd.ui.agenda.AgendaViewModel.agenda]
     */
    fun issues(): List<AgendaElement> {
        return agenda
    }

    /**
     * Returns true if the agenda is empty
     */
    fun isEmpty(): Boolean {
        return agenda.isEmpty()
    }

    /**
     * Removes elements from [agenda]
     * IMPORTANT:
     * 1) Lines always refer to a TextualRepresentation ... I would also add textual representation in Agenda.
     * 2) I wonder if the agenda might be positioned best as a "View Model".
     */
    fun removeElement(
        qualifiedName: QualifiedName,
        textualRepresentation: TextualRepresentation?,
        line: Int
    ) {
        agenda.removeIf { it.qualifiedName == qualifiedName && it.textualRepresentation == textualRepresentation && it.line == line }
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
     * Analyzes, sorts, manages error and status message.
     */
    fun update() {
        status.exceptions.forEach {
            addElement(
                qualifiedName = it.element?.qualifiedName ?: "",
                errorMessage = it.message,
                exceptionClass = it,
                textualRepresentation = it.textualRepresentation,
                line = it.token?.lineNo ?: -1
            )
        }
    }
}