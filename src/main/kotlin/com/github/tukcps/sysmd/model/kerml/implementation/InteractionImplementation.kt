package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Interaction
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class InteractionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): Interaction, AssociationImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): Interaction = InteractionImplementation(model).also { it.updateFrom(this) }
}