package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Subclassifier
import com.github.tukcps.sysmd.model.kerml.Type
import java.util.*

class SubclassifierImplementation(
    elementId: UUID = UUID.randomUUID(),
    owner: Resolved<Element> = Resolved(),
    subclassification: Resolved<Type>? = null,
    superclassification: Resolved<Type>? = null,
    elementType: String = "Subclassification"
): Subclassifier, SpecializationImplementation(
    elementId = elementId,
    owner = owner,
    specific = subclassification,
    general = superclassification,
    elementType
)