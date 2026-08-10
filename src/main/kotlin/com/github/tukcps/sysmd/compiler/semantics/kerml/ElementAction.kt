package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Import
import kotlin.uuid.Uuid

/**
 * We organize semantic actions as classes that re-use methods and fields.
 * SemanticAction is the base class of all semantic actions.
 * It
 * - Builds and sets its owning membership
 * - Sets owning membership's owned element to itself.
 * @param context the context of the compile-run, including, e.g., a stack of owning scopes.
 * @param type the type of the element created by the action.
 * @param owningMembershipType the type of the owning membership to be created.
 *                              Pass null to skip automatically generating a membership.
 */
open class ElementAction(
    val context: ActionsContext,
    type: ElementType,
    val owningMembershipType: ElementType? = ElementType.OwningMembership,
) {
    /** To save state of context. Stored/restored before/after production is executed. */
    val previousAction: ElementAction = context.action
    var isBuilt = false

    /**
     * The element of type 'type' that is built by the production.
     * Introduces a **preliminary** random id, might be replaced by UUID5 after
     * compile and before import to session.
     */
    val element: ElementData = ElementData(
        elementId = Uuid.random(), type = type
    )

    var type : ElementType
        get() = element.type
        set(value) {
            check(! isBuilt) { "Cannot change type after element is built" } // Really?
            element.type = value
        }

    /**
     * Actions that are done before parsing can be handled here.
     */
    open fun beforeProduction() {
        if(owningMembershipType !== null)
            context.addOwnedElement(element, owningMembershipType)
        context.action = this
        element.input = context.compiler.input
        element.indices = context.compiler.token.indices
    }

    /** Unwinds this action's frame from the action stack.
     * Safe to call twice (provided no other Action was ran in the meantime).
     */
    fun cleanUp() {
        context.prefixes.clear()                            // drop saved prefixes.
        context.visibility = Import.VisibilityKind.Public   // reset to default.
        context.action = previousAction                     // restore state from before.
    }

    /**
     * Method that is called after executing the lambda 'production'.
     * For cleanup and validation purposes.
     */
    open fun afterProduction() {
        element.indices = IntRange(element.indices?.first?:0, context.compiler.consumedToken.indices.last)
        cleanUp()
    }

    /**
     * Sets the name, shortname, and generates a UUID5 if needed.
     * Note that the UUID5 is based on the owner that is defined in the context!
     * @param identification, Identification data structure that includes short name and name
     */
    open fun setIdentification(identification: Identification): ElementAction {
        element.declaredShortName = identification.shortName
        element.declaredName = identification.name
        return this
    }

    override fun toString(): String = "${type}Action [${element.declaredName?:element.declaredShortName?:""}]"
}

/**
 * Core method that parses a production rule that is given as parameter.
 * Furthermore, it runs the methods
 *  - beforeProduction, that creates an element with UUID4, and adds it to the created elements.
 *  - afterProduction,
 *  - build
 * @param production Method that runs in scope of a SemanticAction.
 * The method MUST call the method buildAction() after name/shortName are known,
 * and before building the next element, typically in a new Body.
 * @return the created element of the created abstract representation
 */
inline fun<T : ElementAction> T.parse(production: T.() -> Unit ): ElementData {
    beforeProduction()
    production() // early return leaves dangling scopes (intentional)
    afterProduction()
    return element
}