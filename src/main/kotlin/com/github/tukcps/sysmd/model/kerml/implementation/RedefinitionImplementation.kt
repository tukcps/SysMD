package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

@Suppress("UNCHECKED_CAST")
class RedefinitionImplementation(
    redefiningFeature: Resolved<Feature>? = null,
    redefinedFeature: Resolved<Feature>? = null,
    elementType: String = "Redefinition"
): Redefinition, SubsettingImplementation(
    subsettedFeature = redefinedFeature,
    subsettingFeature = redefiningFeature,
    elementType = elementType
) {
    override var redefinedFeature: Resolved<Feature>
        get() = if (target.firstOrNull() != null) target.first() as Resolved<Feature> else Resolved(str="KerML::")
        set(value) { target[0] = value }

    override var redefiningFeature: Resolved<Feature>
        get() = if (source.firstOrNull() != null) source.first() as Resolved<Feature> else Resolved(str="KerML::")
        set(value) { source[0] = value }

    override fun clone(): Redefinition {
        return RedefinitionImplementation(
            redefiningFeature = redefiningFeature,
            redefinedFeature = redefinedFeature,
        ).also {
            it.model = model
            it.isStandard = isStandard
            it.isLibraryElement = isLibraryElement
        }
    }

    override fun toString(): String = "Redefinition { of ${redefinedFeature.ref?.qualifiedName} to ${redefiningFeature.ref?.qualifiedName} }"

    override fun updateFrom(template: Element) {
        require(model != null)
        require(template is Redefinition)
        super.updateFrom(template)
    }
}