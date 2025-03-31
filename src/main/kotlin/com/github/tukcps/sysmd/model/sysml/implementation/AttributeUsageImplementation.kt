package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import java.util.UUID

class AttributeUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.IN,
    isEnd: Boolean = false,
    isComposite: Boolean = true,
    isPortion: Boolean = false,
    isSufficient: Boolean = false,
    isUnique: Boolean = false,
    isOrdered: Boolean = false,
    isRedefined: Boolean = false,
    isDerived: Boolean = false,
    isReadOnly: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "AttributeUsage",
): AttributeUsage, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    isComposite = isComposite,
    isPortion = isPortion,
    isSufficient = isSufficient,
    isUnique = isUnique,
    isOrdered = isOrdered,
    isRedefined = isRedefined,
    isDerived = isDerived,
    isReadOnly = isReadOnly,
    textualRepresentation = textualRepresentation,
    elementType = elementType
) {
    override fun clone(): AttributeUsage {
        return AttributeUsageImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
            direction = direction,
            isEnd = isEnd,
            isComposite = isComposite,
            isPortion = isPortion,
            isSufficient = isSufficient,
            isUnique = isUnique,
            isOrdered = isOrdered,
            isRedefined = isRedefined,
            isDerived = isDerived,
            isReadOnly = isReadOnly,
            textualRepresentation = textualRepresentation,
        ).also {
            it.model = model
            it.unitConstraint = unitConstraint
            it.typeConstraint = typeConstraint
            it.expression = expression
        }
    }
}