package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

open class StepImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    isEnd: Boolean = false,
    elementType: String = "Step"
): Step, FeatureImplementation(
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    direction =direction,
    isEnd =isEnd,
    elementType = elementType
)