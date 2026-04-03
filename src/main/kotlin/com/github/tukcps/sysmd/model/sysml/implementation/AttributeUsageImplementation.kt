package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeUsage

class AttributeUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "AttributeUsage",
): AttributeUsage, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): AttributeUsage = AttributeUsageImplementation(
            declaredName = this.declaredName,
            declaredShortName = this.declaredShortName,
        ).also {
            super.updateFrom(this)
            it.model = model
            it.typeConstraint = typeConstraint
            it.expression = expression
        }
}