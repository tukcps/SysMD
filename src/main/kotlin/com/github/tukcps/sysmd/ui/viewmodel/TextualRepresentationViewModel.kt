package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.imports.ResultAnnotation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.services.inheritance.getAllInheritedFeatures
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.ui.inCompile
import java.util.*

/**
 * The view model of an element.
 * Each element consists of a description in MD or textual SysMD code.
 * The view model represents the state of the element:
 *  - the code/description lines
 *  - which of both is editable
 *  - a list of annotations to the lines
 *  - a list of updated properties from the constraint propagation
 *  @param kerMlModel The internal kerMl model; does not refresh, is independent of UI
 *  @param refreshTrees lambda that refreshes the tree-views in the left panel
 */
open class TextualRepresentationViewModel(
    val kerMlModel: MutableState<Session>,
    val refreshTrees: () -> Unit,
    // id for easy identification
    var id: UUID? = null,

    // Code or description in Markdown?
    var language: MutableState<Language> = mutableStateOf(Language.MARKDOWN),
    var namespace: MutableState<String> = mutableStateOf("Global"),

    // The editable description as text field.
    val body: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue()),

    // Per line, an annotation and corresponding line number as a hashmap.
    val annotations: SnapshotStateMap<Int, String> = mutableStateMapOf(),

    /**
     * The annotations to be shown on mouseover in code parts
     * (after "Analyze", yellow background)
     */
    var textualRepresentation: TextualRepresentation,

    /**
     * Additional text to be displayed as "info", e.g., analysis results by "show".
     */
    val displayItems: SnapshotStateList<TextFieldValue> = mutableStateListOf(),

    /**
     * The element with show relationship
     */
    private var displayElement: Element? = null
) {

    //Simulation results annotations; for displaying simulation results right to the Editor field
    val resultsAnnotations : MutableList<ResultAnnotation> = mutableListOf()

    init {
        language.value = TextualRepresentationViewModel.language[textualRepresentation.language.split("::").firstOrNull()] ?:Language.MARKDOWN
    }


    /** Clears annotations on properties, but not the lines. */
    fun reset() {
        displayElement = null
        annotations.clear()
        displayItems.clear()
        inCompile = false   // who knows ... the semaphor to prevent starting the compiler twice.
    }

    /**
     * Compiles the element and updates the display of this element.
     * @param propagate If true (default), constraint propagation is computed.
     */
    fun compile(propagate: Boolean = true) {
        // Clean old status
        require(kerMlModel.value == textualRepresentation.model)
        reset()
        textualRepresentation.body = this.body.value.text
        if (language.value in compilableLanguages) {
            // search for Element named Display and show hasA relations; first clean it.
            try {
                // Parse model & compute propagation
                textualRepresentation.language = "SysMD"+"::"+namespace.value
                textualRepresentation.compile()
                if (propagate) {
                    kerMlModel.value.initialize()
                    kerMlModel.value.propagate()
                    refreshTrees()
                    display()
                }
            } catch (ignore: Exception) {
                kerMlModel.value.report(SysMDError(message = ignore.message?:"(unknown)"))
            }
            // Update and show the display part with results
        }

        // refresh the tree views & the agenda
        refreshTrees()
    }


    /**
     * Generates the model for displaying computed results.
     */
    fun display() {
        // Update annotations (error messages in the shape of a bell near line no.).
        kerMlModel.value.status.exceptions.forEach {
            if (it.textualRepresentation == this.textualRepresentation) {
                if (it.token?.lineNo != null )
                    annotations[it.token!!.lineNo - 1] = it.message
            }
        }

        // Update displayed items, part's errors and properties of Display class
        displayItems.clear()
        try {
            val annotations = textualRepresentation.getOwnedElementsOfType<Annotation>()
            annotations.forEach { annotation ->
                when (val target = annotation.annotatedElement.ref) {
                    is Classifier -> {
                        if (target !is CalculationDefinitionImplementation){
                            displayItems.add(TextFieldValue("Definition ${target.qualifiedName} created or updated "))
                            kerMlModel.value.getAllInheritedFeatures(target).forEach {
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
                        // kerMlModel.value.getAllInheritedFeatures(target).forEach {
                           if ((target.variable != null) && !(target is Multiplicity && target.variable!!.vectorQuantity.idd().getRange() == IntegerRange(1, 1)))
                                displayItems.add (TextFieldValue("    ${target.qualifiedName} = ${target.variable!!.vectorQuantity}"))
                        //}
                    }
                }
            }

            // Errors to be displayed.
            kerMlModel.value.status.exceptions.forEach {
                if (it.textualRepresentation == this.textualRepresentation && it.priority > 1)
                    displayItems.add(TextFieldValue("ERROR: $it"))
            }

            // Other infos ...
            kerMlModel.value.status.exceptions.forEach {
                if (it.textualRepresentation == this.textualRepresentation && it.priority <= 1)
                    displayItems.add(TextFieldValue("INFO: $it"))
            }
        } catch (ignore: Exception) {
            displayItems.add(TextFieldValue("ERROR in display reporting ($ignore). "))
        }
    }


    companion object {
        /** The languages handled in SysMD Notebook. */
        enum class Language {
            MARKDOWN { override fun toString() = "Markdown" },
            SYS_MD   { override fun toString() = "SysMD" },
            SYS_ML   { override fun toString() = "SysML" },
            FORM     { override fun toString() = "Form" },
            YAML     { override fun toString() = "YaML" },
            VIEW     { override fun toString() = "View" },
        }
        val compilableLanguages = setOf(Language.SYS_MD, Language.SYS_ML)
        val allLanguages = Language.entries
        val language: Map<String, Language> = allLanguages.associate { (it.toString() to it) }
    }
}
