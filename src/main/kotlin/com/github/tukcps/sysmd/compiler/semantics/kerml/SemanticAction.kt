@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Import

/**
 * We organize semantic actions as classes that re-use methods and fields.
 * SemanticAction is the base class of all semantic actions.
 * @param T the type of the created element in the abstract representation (model).
 * @param context the context of the compile-run, including, e.g., a stack of owning scopes.
 * @param creator a lambda that is called when the creation process is finished.
 * The result is stored in the field created.
 */
open class SemanticAction<T: Element>(
    val context: ActionsContext,
    var creator: () -> T,
) {
    var created: T

    /**
     * A semantic action creates always an element of type T.
     * This element can be added to the model by the method create.
     */
    init {
        created = creator()
    }

    /**
     * Actions that are done before parsing can be handled here.
     * This includes
     * - handling visibility of ownership (Namespace), etc.
     */
    open fun init() {}

    /**
     * Method that parses a production rule given as parameter.
     * @param production Method that runs in scope of a SemanticAction
     * @return the created element of the created abstract representation
     */
    fun parse( production: SemanticAction<T>.() -> Unit ): T = created

     /**
     * Adds the element to the model.
     * @param identification name and short name.
     */
    open fun create(identification: Identification?=null) {}

    /**
     * Method that is called after executing the lambda 'production'.
     */
    open fun finish() {
        context.prefixes.clear()
        context.visibility = Import.VisibilityKind.Public
    }

    /**
     * Sets the name, shortname, and generates a UUID5 if needed.
     * Note that the UUID5 is based on the owner that is defined in the context!
     * @param identification, Identification data structure that includes short name and name
     */
    open fun setIdentification(identification: Identification) {
        created.declaredShortName = identification.shortName
        created.declaredName = identification.name
    }

    override fun toString(): String = "[${created.escapedName()?:""}]"
}