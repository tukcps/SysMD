package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition

class OccurrenceDefinitionImplementation(
    elementType: String = "OccurrenceDefinition",
    ) : OccurrenceDefinition, ClassImplementation(
        elementType = elementType,
    )