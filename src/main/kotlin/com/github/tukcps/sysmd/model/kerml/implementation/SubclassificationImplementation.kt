package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Subclassification
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedType

class SubclassificationImplementation(
    subclassification: Type = UnresolvedType(),
    superclassification: Type = UnresolvedType(),
    elementType: String = "Subclassification"
): Subclassification, SpecializationImplementation(
    specific = subclassification,
    general = superclassification,
    elementType = elementType
)