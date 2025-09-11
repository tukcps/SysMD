package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.LiteralRational
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.util.SimpleName
import io.github.tukcps.aadd.Real

class LiteralRationalImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "LiteralRational"
) : LiteralRational, LiteralExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    textualRepresentation = textualRepresentation,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    var value: Real? = null //Real? Or Float/double?
}