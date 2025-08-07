@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.util.SimpleName


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
    var creator: (SimpleName?, SimpleName?) -> T,
) {
    var created: T

    /**
     * A semantic action creates always an element of type T.
     * This element can be added to the model by the method create.
     */
    init {
        created = creator(null, null)
    }

    /**
     * Actions that are done before parsing.
     */
    open fun init() {}

    fun parse( production: () -> Unit ): T {
        init()
        production()
        finish()
        return created
    }

     /**
     * Adds the element to the model.
     * @param identification name and short name.
     */
    open fun create(identification: Identification?=null) {
        created.declaredName = identification?.name
        created.declaredShortName = identification?.shortName
        created.input = context.compiler.input
        created.indices = context.compiler.consumedToken.indices
        if ( (STANDARD in context.prefixes) or (LIBRARY in context.prefixes) )
            created.isLibraryElement = true
        @Suppress("UNCHECKED_CAST")

        // determine the owner
        val whereToAdd = if (created == context.element()) context.owner() else context.element()

        created =
            if (created !is Namespace && created !is Annotation && created !is Dependency && created is Relationship)
                context.model.addOwnedRelationship(created as Relationship, whereToAdd) as T
            else
                context.model.addOwnedMember(created, whereToAdd)

        context.model.status.createdElements.add(
            context.ownerName() + "::${created.escapedName()}"
        )
    }

    /**
     * Method that is called after executing the lambda 'production'.
     */
    open fun finish() {
        context.prefixes.clear()
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

    override fun toString(): String = "[${created.escapedName()?:created.elementType}]"
}