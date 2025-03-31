package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Subclassifier
import com.github.tukcps.sysmd.model.kerml.Type

class SubclassifierImplementation(
    subclassification: Resolved<Type>? = null,
    superclassification: Resolved<Type>? = null,
    elementType: String = "Subclassification"
): Subclassifier, SpecializationImplementation(
    specific = subclassification,
    general = superclassification,
    elementType
)