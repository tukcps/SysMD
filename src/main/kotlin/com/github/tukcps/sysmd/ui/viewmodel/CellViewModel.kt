package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.imports.ResultAnnotation
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.expression.BooleanExpression
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import io.github.tukcps.aadd.values.integer.IntegerRange
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
            val lang = language.value
            val ns = if (namespace.value in setOf("Global", "")) "" else namespace.value
            sessionService.updateModel(sessionIdState.value, code = body.text, lang, ns, Runlevel.MODEL )

            if (propagate) {
                sessionService.updateModel(sessionIdState.value, code = "", lang, ns, Runlevel.ALL )
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
     * @param elementsByInput optionally, all elements of the session grouped by their input (the source text of
     * the cell that defines them). Pass it when the results of many cells are collected, so that the elements of
     * the session are not searched once per cell.
     */
    fun collectVariablesToDisplay(elementsByInput: Map<CharSequence?, List<Element>>? = null) {
        // Update annotations (error messages in the shape of a bell near line no.).
        if (session == null) return
        val text = body.text
        session!!.status.issues.forEach { issue ->
            if (issue.input == text && issue.line() != null)
                annotations[issue.line()!!-1] = issue.message
        }

        // Update displayed items, part's errors and properties of Display class
        displayItems.clear()
        try {
            val elements = if (elementsByInput != null) elementsByInput[text] ?: emptyList()
                           else session!!.get().filter { it.input == text }
            val displayedVariables = mutableSetOf<String>()

            fun shouldExclude(name: String?, path: String, isConstraint: Boolean): Boolean {
                if (isConstraint) return true
                if (name == "unit" || name == "range") return true
                if (path.endsWith("::unit") || path.endsWith("::range") || path.endsWith(".unit") || path.endsWith(".range") || path == "unit" || path == "range") return true
                if (name == null || path.contains("/2/1") || path.endsWith("/2/1") || path.endsWith("/1")) return true
                return false
            }

            // Attributes (parameters, locals) of calculation definitions have no values; hide them.
            fun isInCalculationDefinition(element: Element): Boolean {
                var current: Element? = element
                while (current != null) {
                    if (current is CalculationDefinitionImplementation) return true
                    current = current.owner
                }
                return false
            }

            // 1. Process Classifiers first
            elements.filterIsInstance<Classifier>()
                .sortedByKey { it.escapedName()?.lowercase() ?: it.path().lowercase() }
                .forEach { element ->
                if (element !is CalculationDefinitionImplementation){
                    displayItems.add(TextFieldValue("${element.elementType().name} ${element.path()} created or updated "))

                    val membershipsToIterate = element.visibleMemberships().filter { membership ->
                        val member = membership.memberElement
                        // Include properties defined in cell OR inherited properties (owned by element or supertype)
                        (member.input == text || member.isImpliedIncluded || (member.owner !== element && member.owner != null))
                    }.sortedByKey {
                        val m = it.memberElement
                        m.escapedName()?.lowercase() ?: (m as? Feature)?.expression?.lowercase() ?: m.name?.lowercase() ?: ""
                    }

                    membershipsToIterate.forEach { membership ->
                        val member = membership.memberElement
                        val memberName = member.escapedName()
                        val isConstraint = member is Invariant ||
                                member is ConstraintUsage ||
                                member is BooleanExpression ||
                                member.elementType() == ElementType.ConstraintUsage ||
                                member.elementType() == ElementType.Invariant ||
                                (member as? Feature)?.type?.any { it.escapedName() == "ConstraintUsage" || it.escapedName() == "Constraint" } == true
                        val varPath = if (member.owner === element) {
                            member.path()
                        } else {
                            member.escapedName()?.let { "${element.path()}::$it" } ?: member.path()
                        }
                        if (!shouldExclude(memberName, varPath, isConstraint)) {
                            val memberVar = (member as? Feature)?.variable
                            val variable = session!!.solver.getVariable(varPath) ?: memberVar
                            when (member) {
                                is Classifier -> displayItems.add(TextFieldValue("   Classifier: $memberName"))
                                else -> {
                                    if (member !is Multiplicity && !(memberVar?.intSpecs?.firstOrNull() == IntegerRange(1, 1))) {
                                        val isConstrained = variable != null && variable.isVectorQuantityInitialized && variable.vectorQuantity.isConstrained()
                                        if (isConstrained) {
                                            val valueStr = if (variable.unitSpec.isNotEmpty() && variable.vectorQuantity.unitSpec.isEmpty()) {
                                                variable.vectorQuantity = variable.vectorQuantity.copy(unitSpec = variable.unitSpec)
                                                variable.vectorQuantity.toString()
                                            } else {
                                                variable.vectorQuantity.toString()
                                            }
                                            val displayName = memberName ?: (member as? Feature)?.expression?.let { "{ $it }" } ?: member.name ?: "feature"
                                            val string = "    Feature: $displayName = $valueStr"
                                            displayItems.add(TextFieldValue(string))
                                            displayedVariables.add(variable.path)
                                            displayedVariables.add(varPath)
                                            displayedVariables.add(member.path())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Process Features second
            elements.filterIsInstance<Feature>()
                .sortedByKey { it.escapedName()?.lowercase() ?: it.expression?.lowercase() ?: it.path().lowercase() }
                .forEach { element ->
                val path = element.path()
                val name = element.escapedName()
                val isConstraint = element is Invariant ||
                        element is ConstraintUsage ||
                        element is BooleanExpression ||
                        element.elementType() == ElementType.ConstraintUsage ||
                        element.elementType() == ElementType.Invariant ||
                        element.type.any { it.escapedName() == "ConstraintUsage" || it.escapedName() == "Constraint" }
                if (element !is Multiplicity &&
                    element.input == text &&
                    !element.isImpliedIncluded &&
                    !isInCalculationDefinition(element) &&
                    !shouldExclude(name, path, isConstraint) &&
                    path !in displayedVariables
                ) {
                    val variable = element.variable ?: session!!.solver.getVariable(path)
                    val isConstrained = variable != null && variable.isVectorQuantityInitialized && variable.vectorQuantity.isConstrained()
                    if (isConstrained) {
                        val valueStr = if (variable.unitSpec.isNotEmpty() && variable.vectorQuantity.unitSpec.isEmpty()) {
                            variable.vectorQuantity = variable.vectorQuantity.copy(unitSpec = variable.unitSpec)
                            variable.vectorQuantity.toString()
                        } else {
                            variable.vectorQuantity.toString()
                        }
                        val displayName = name ?: element.expression?.let { "{ $it }" } ?: path
                        displayItems.add(TextFieldValue("    $displayName = $valueStr"))
                        displayedVariables.add(path)
                        displayedVariables.add(variable.path)
                    }
                }
            }

            // Errors to be displayed.
            session!!.status.issues.forEach {
                if (it.input == text && it.kind.ordinal >= Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("ERROR: ${it.message}"))
            }

            // Other infos ...
            session!!.status.issues.forEach {
                if (it.input == text && it.kind.ordinal < Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("INFO: ${it.message}"))
            }
        } catch (ignore: Exception) {
            displayItems.add(TextFieldValue("ERROR in display reporting ($ignore). "))
        }
    }
}

/** Like sortedBy, but computes the (here expensive) key once per element instead of once per comparison. */
private inline fun <T> Iterable<T>.sortedByKey(key: (T) -> String): List<T> =
    map { key(it) to it }.sortedBy { it.first }.map { it.second }
