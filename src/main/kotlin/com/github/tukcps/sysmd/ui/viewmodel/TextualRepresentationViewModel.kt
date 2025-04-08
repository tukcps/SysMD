package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.imports.ResultAnnotation
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.services.inheritance.getAllInheritedFeatures
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.inCompile
import io.github.tukcps.aadd.values.IntegerRange

/**
 * The view model of a textual representation that is rendered.
 * Each element consists of a description in MD or textual SysMD code.
 * The view model represents the state of the element:
 *  - the code/description lines
 *  - which of both is editable
 *  - a list of annotations to the lines
 *  - a list of updated properties from the constraint propagation
 *  @param sessionState The internal kerMl model; does not refresh, it is independent of UI
 *  @param refreshTrees lambda that refreshes the tree-views in the left panel
 */
open class TextualRepresentationViewModel(
    val sessionState: MutableState<Session>,
    val refreshTrees: () -> Unit,

    // Code or description in Markdown?
    var language: MutableState<Language> = mutableStateOf(Language.MARKDOWN),
    var namespace: MutableState<String> = mutableStateOf("Global"),

    // The editable description as text field.
    val bodyState: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue()),

    // Per line, an annotation and corresponding line number as a hashmap.
    val annotations: SnapshotStateMap<Int, String> = mutableStateMapOf(),

    /**
     * Additional text to be displayed as "info", e.g., analysis results by "show".
     */
    val displayItems: SnapshotStateList<TextFieldValue> = mutableStateListOf(),

    /**
     * The element with show relationship
     */
    private var displayElement: Element? = null
) {
    val session: Session by sessionState
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
     * Compiles the element and updates the display of this element.
     * @param propagate If true (default), constraint propagation is computed.
     */
    fun compile(propagate: Boolean = true) {
        clearView()
        if (language.value in compilableLanguages) {
            try {
                // Parse model & compute propagation
                when (language.value) {
                    Language.KerML -> KerML(session).parse(body.text, namespace.value)
                    Language.SYS_MD -> SysMD(session).parse(body.text, namespace.value)
                    Language.SYS_ML -> SysMLv2(session).parse(body.text, namespace.value)
                    else -> {}
                }

                if (propagate) {
                    session.propagate()
                    display()
                    refreshTrees()
                }
            } catch (error: Exception) {
                if (error is SysMDException) {
                    session.status.fatal(error.message, cause = error)
                } else
                    session.status.fatal(message = error.message?:"(unknown error)")
            }
        }
    }


    /**
     * Generates the model for displaying computed results.
     */
    fun display() {
        // Update annotations (error messages in the shape of a bell near line no.).
        session.status.issues.forEach {
            if (it.input == body.text && it.token?.lineNo != null)
                annotations[it.token!!.lineNo - 1] = it.message
        }

        // Update displayed items, part's errors and properties of Display class
        displayItems.clear()
        try {
            session.get()
                .filter { it.input == body.text }
                .forEach { element ->
                    when (element) {
                    is Classifier -> {
                        if (element !is CalculationDefinitionImplementation){
                            displayItems.add(TextFieldValue("Definition ${element.path()} created or updated "))
                            session.getAllInheritedFeatures(element).forEach {
                                when (it) {
                                    is Classifier -> displayItems.add(TextFieldValue("   Type: ${it.escapedName()}"))
                                    else    -> {
                                        if ( !(( it is Multiplicity) && it.variable!!.intSpecs.first() == IntegerRange(1,1))) {
                                            var string = "    Feature: ${it.escapedName()} "
                                            if (it.variable != null)
                                                string += " = ${it.variable!!.vectorQuantity}"
                                            displayItems.add(TextFieldValue(string))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is Feature -> {
                        if ((element.variable != null) && !(element is Multiplicity && element.variable!!.vectorQuantity.idd().getRange() == IntegerRange(1, 1))) {
                            if (element.variable!!.isVectorQuantityInitialized )
                                displayItems.add(TextFieldValue("    ${element.path()} = ${element.variable!!.vectorQuantity}"))
                            else
                                displayItems.add(TextFieldValue("    ${element.path()} = (not computed/reset?)"))
                        }
                    }
                }
            }

            // Errors to be displayed.
            session.status.issues.forEach {
                if (it.input == body.text && it.kind.ordinal >= Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("ERROR: ${it.message}"))
            }

            // Other infos ...
            session.status.issues.forEach {
                if (it.input == body.text && it.kind.ordinal < Issue.Kind.ERROR.ordinal)
                    displayItems.add(TextFieldValue("INFO: ${it.message}"))
            }
        } catch (ignore: Exception) {
            displayItems.add(TextFieldValue("ERROR in display reporting ($ignore). "))
        }
    }


    companion object {
        /** The languages handled in SysMD Notebook. */
        enum class Language {
            MARKDOWN { override fun toString() = "Markdown" },
            KerML    { override fun toString() = "KerML" },
            SYS_MD   { override fun toString() = "SysMD" },
            SYS_ML   { override fun toString() = "SysML" },
            YAML     { override fun toString() = "YAML" },
        }
        val compilableLanguages = setOf(Language.KerML, Language.SYS_MD, Language.SYS_ML)
        val allLanguages = Language.entries
        val language: Map<String, Language> = allLanguages.associate { (it.toString() to it) }
    }
}
