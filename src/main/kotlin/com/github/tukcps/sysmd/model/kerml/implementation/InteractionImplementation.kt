package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Interaction
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.UUID

class InteractionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Interaction"
): Interaction,
    AssociationImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        elementType = elementType
    )