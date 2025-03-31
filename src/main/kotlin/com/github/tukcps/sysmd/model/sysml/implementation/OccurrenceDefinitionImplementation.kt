package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import java.util.UUID

class OccurrenceDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "OccurrenceDefinition"
) : OccurrenceDefinition, ClassImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
)