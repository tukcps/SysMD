package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Succession
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class SuccessionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Succession",
) : Succession, ConnectorImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
)