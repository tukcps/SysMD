package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.LiteralBoolean
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.util.SimpleName

class LiteralBooleanImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "LiteralBoolean"
) : LiteralBoolean, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    textualRepresentation = textualRepresentation,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)  {
    var value: Boolean? = null
}