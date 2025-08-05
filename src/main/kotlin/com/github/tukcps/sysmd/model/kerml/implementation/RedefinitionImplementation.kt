package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

@Suppress("UNCHECKED_CAST")
class RedefinitionImplementation(
    redefiningFeature: Feature = UnresolvedFeature("Base::things"),
    redefinedFeature: Feature = UnresolvedFeature("Base::things"),
    elementType: String = "Redefinition"
): Redefinition, SubsettingImplementation(
    subsettedFeature = redefinedFeature,
    subsettingFeature = redefiningFeature,
    elementType = elementType
) {
    override var redefinedFeature: Feature
        get() = subsettedFeature
        set(value) { target = mutableListOf(value) }

    override var redefiningFeature: Feature
        get() = source.first() as Feature
        set(value) { source = mutableListOf(value) }

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

    override fun updateFrom(template: Element) {
        require(model != null)
        require(template is Redefinition)
        super.updateFrom(template)
    }
}