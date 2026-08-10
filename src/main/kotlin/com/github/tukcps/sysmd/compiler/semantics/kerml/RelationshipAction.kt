package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * Creates a Relationship ElementData, and
 * - sets owningElement, owner
 * - adds Relationship to ownedRelationship of owner.
 * - sets source, target.
 * @param context Information on the context of the parse run
 * @param type
 */
open class RelationshipAction(
    context: ActionsContext,
    type: ElementType
): ElementAction(context, type) {

    /** Just an alias - name for the element ... */
    val relationship
        get() = element

    /**
     * Explicitly no call of super.init() that would create an owning relationship.
     */
    override fun beforeProduction() {
        // super.beforeProduction() // NOT, is for connecting Elements.
        context.addOwnedRelationship(relationship)
        context.action = this
        relationship.input = context.compiler.input
        relationship.indices = IntRange(element.indices?.first?:0, context.compiler.consumedToken.indices.last)
    }
}
