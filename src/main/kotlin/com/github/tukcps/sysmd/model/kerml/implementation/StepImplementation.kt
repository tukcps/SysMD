package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*

open class StepImplementation(
    owner: Resolved<Element> = Resolved(),
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    isEnd: Boolean = false,
    elementType: String = "Step"
): Step, FeatureImplementation(
    owner = owner,
    elementId =elementId,
    declaredName =declaredName,
    declaredShortName =declaredShortName,
    ownedElement =ownedElement,
    direction =direction,
    isEnd =isEnd,
    elementType = elementType
)