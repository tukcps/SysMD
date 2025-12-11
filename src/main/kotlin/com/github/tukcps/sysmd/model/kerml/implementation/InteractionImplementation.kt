package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Interaction

class InteractionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Interaction"
): Interaction, AssociationImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): Interaction = InteractionImplementation(declaredName, declaredShortName).also {
        it.updateFrom(this)
    }
}