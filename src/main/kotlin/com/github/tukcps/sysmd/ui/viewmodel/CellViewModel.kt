package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.imports.ResultAnnotation
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.inCompile
import io.github.tukcps.aadd.values.IntegerRange
import kotlin.uuid.Uuid

/**
 * The view model of a textual representation that is rendered.
 * Each element consists of a description in MD or textual SysMD code.
 * The view model represents the state of the element:
 *  - the code/description lines
 *  - which of both is editable
 *  - a list of annotations to the lines
 *  - a list of updated properties from the constraint propagation
 *  @param sessionIdState The internal model; does not refresh, it is independent of UI
 *  @param refreshTrees lambda that refreshes the tree-views in the left panel
 */
open class CellViewModel(
    val sessionIdState: MutableState<Uuid>,
    val cellListViewModel: CellListViewModel,
    val refreshTrees: () -> Unit,

    // Code or description in Markdown?
    var language: MutableState<Language> = mutableStateOf(Language.MARKDOWN),
    var namespace: MutableState<String> = mutableStateOf("Global"),

    // The editable description as a text field.
    val bodyState: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue()),

    // Per line, an annotation and corresponding line number as a hashmap.
    val annotations: SnapshotStateMap<Int, String> = mutableStateMapOf(),

    /**
     * Additional text to be displayed as "info", e.g., analysis results by "show".
     */
    val displayItems: SnapshotStateList<TextFieldValue> = mutableStateListOf(),

    /** The element with show relationship */
    private var displayElement: Element? = null
) {
    constructor(
        sessionIdState: MutableState<Uuid>,
        cellListViewModel: CellListViewModel,
        refreshTrees: () -> Unit,
        elementData: ElementData
    ): this(
        sessionIdState = sessionIdState,
        cellListViewModel = cellListViewModel,
        refreshTrees = refreshTrees,
        language = mutableStateOf(Language.toLanguage(elementData.language?:"")?: Language.MARKDOWN),
        namespace = mutableStateOf(Language.toNamespace(elementData.language?:"")?:""),
        bodyState = mutableStateOf(TextFieldValue(elementData.body?:"")),
    )

    val session: Session? get() = sessionService.getSession(sessionIdState.value)
    var body: TextFieldValue by bodyState

    //Simulation results annotations; for displaying simulation results right to the Editor field
    val resultsAnnotations : MutableList<ResultAnnotation> = mutableListOf()

    /** Clears annotations on features, but not the lines. */
    fun clearView() {
        displayElement = null
        annotations.clear()
        displayItems.clear()
        inCompile = false   // who knows ... the semaphore to prevent starting the compiler twice.
    }

    /**
     * Lambda for the selection of a compile run of ONLY this cell
     */
    val onCompile = { compile() }

    /**
     * Compiles the element and updates the display of this element.
     * @param propagate If true (default), constraint propagation is computed.
     */
    fun compile(propagate: Boolean = true) {
        clearView()
        if (session == null) return
        try {
            val language = language.value
            val namespace = namespace.value
            sessionService.updateModel(sessionIdState.value, code = body.text, language, namespace, Runlevel.MODEL )

            if (propagate) {
                sessionService.updateModel(sessionIdState.value, code = "", language, namespace, Runlevel.ALL )
                collectVariablesToDisplay()
                refreshTrees()
            }
        } catch (error: Exception) {
            if (error is SysMDException) {
                session!!.status.fatal(error.message, cause = error)
            } else
                session!!.status.fatal(message = error.message?:"(unknown error)")
        }
    }


    /**
     * Generates the model for displaying computed results.
     */
    fun collectVariablesToDisplay() {
        // Update annotations (error messages in the shape of a bell near line no.).
        if (session == null) return
        session!!.status.issues.forEach { issue ->
            if (issue.input == body.text && issue.line() != null)
                annotations[issue.line()!!-1] = issue.message
        }

        // Update displayed items, part's errors and properties of Display class
        displayItems.clear()
        try {
            val elements = session!!.get().filter { it.input == body.text }
            val displayedVariables = mutableSetOf<String>()

            // 1. Process Classifiers first
            elements.filterIsInstance<Classifier>().forEach { element ->
                if (element !is CalculationDefinitionImplementation){
                    displayItems.add(TextFieldValue("${element.elementType} ${element.path()} created or updated "))

                    val hasLocalFeatures = element.visibleMemberships().any {
                        val member = it.memberElement
                        member is Feature && member !is Multiplicity
                    }
                    val membershipsToIterate = if (!hasLocalFeatures) {
                        element.allSupertypes(transitive = true).flatMap { supertype ->
                            supertype.visibleMemberships()
                        }.distinctBy { it.memberElement.escapedName() }
                    } else {
                        element.visibleMemberships()
                    }

                    membershipsToIterate.forEach { membership ->
                        val member = membership.memberElement
                        val varPath = if (member.owner === element) {
                            member.path()
                        } else {
                            member.escapedName()?.let { "${element.path()}::$it" } ?: member.path()
                        }
                        val variable = session!!.solver.getVariable(varPath)
                        when (member) {
                            is Classifier -> displayItems.add(TextFieldValue("   Classifier: ${member.escapedName()}"))
                            else -> {
                                if ( !(( member is Multiplicity) && member.variable!!.intSpecs.first() == IntegerRange(1,1))) {
                                    var string = "    Feature: ${member.escapedName()} "
                                    if (variable != null && variable.vectorQuantity.isConstrained()) {
                                        string += " = ${variable.vectorQuantity}"
                                        displayItems.add(TextFieldValue(string))
                                        displayedVariables.add(variable.path)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Process Features second
            elements.filterIsInstance<Feature>().forEach { element ->
                val path = element.path()
                if (path !in displayedVariables) {
                    if ((element.variable != null) && !(element is Multiplicity && element.variable!!.vectorQuantity.idd().getRange() == IntegerRange(1, 1))) {
                        if (element.variable!!.isVectorQuantityInitialized && element.variable!!.vectorQuantity.isConstrained()) {
                            displayItems.add(TextFieldValue("    ${element.path()} = ${element.variable!!.vectorQuantity}"))
                            displayedVariables.add(path)
                        }
                        // else
                        //    displayItems.add(TextFieldValue("    ${element.path()} = (not computed/reset?)"))
                    }
                }
            }

            // Errors to be displayed.
            session!!.status.issues.forEach {
                if (it.input == body.text && it.kind.ordinal >= Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("ERROR: ${it.message}"))
            }

            // Other infos ...
            session!!.status.issues.forEach {
                if (it.input == body.text && it.kind.ordinal < Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("INFO: ${it.message}"))
            }
        } catch (ignore: Exception) {
            displayItems.add(TextFieldValue("ERROR in display reporting ($ignore). "))
        }
    }
}
