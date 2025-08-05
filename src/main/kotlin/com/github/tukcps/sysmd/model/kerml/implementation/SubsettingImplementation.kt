package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Subsetting
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature

/**
 * Like a Specialization, but a relationship between Features that are a subset of a superset.
 */
@Suppress("UNCHECKED_CAST")
open class SubsettingImplementation(
    subsettingFeature: Feature = UnresolvedFeature("Base::things"), // "that"
    subsettedFeature: Feature = UnresolvedFeature("Base::things"),
    elementType: String = "Subsetting"
): Subsetting, SpecializationImplementation(
    specific = subsettingFeature,
    general = subsettedFeature,
    elementType = elementType
) {
    override var subsettedFeature: Feature
        get() = general as Feature
        set(value) { general = value }

    override var subsettingFeature: Feature
        get() = specific as Feature
        set(value) { specific = value }

    override fun clone(): Subsetting {
        return SubsettingImplementation(
            subsettingFeature = subsettingFeature,
            subsettedFeature = subsettedFeature
        )
    }
}