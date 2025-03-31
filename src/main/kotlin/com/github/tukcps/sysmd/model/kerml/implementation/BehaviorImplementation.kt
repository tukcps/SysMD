package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Behavior
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class BehaviorImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Behavior",
): Behavior, ClassImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
)
