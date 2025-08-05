package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Subclassifier
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedType

class SubclassifierImplementation(
    subclassification: Type = UnresolvedType(),
    superclassification: Type = UnresolvedType(),
    elementType: String = "Subclassification"
): Subclassifier, SpecializationImplementation(
    specific = subclassification,
    general = superclassification,
    elementType = elementType
)