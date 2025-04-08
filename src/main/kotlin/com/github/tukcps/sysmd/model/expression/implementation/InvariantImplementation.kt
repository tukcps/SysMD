package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.util.SimpleName

class InvariantImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Invariant",
): Invariant, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    textualRepresentation = textualRepresentation,
    typeConstraint = mutableListOf("true"),
    expression = expression,
    elementType = elementType,
) {
    override var isNegated: Boolean = false
}
