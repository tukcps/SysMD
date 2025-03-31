package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

/**
 * Like a Specialization, but a relationship between Features that are a subset of a superset.
 */
@Suppress("UNCHECKED_CAST")
open class SubsettingImplementation(
    subsettingFeature: Resolved<Feature>? = null,
    subsettedFeature: Resolved<Feature>? = null,
    elementType: String = "Subsetting"
): Subsetting, SpecializationImplementation(
    specific = subsettingFeature,
    general = subsettedFeature,
    elementType = elementType
) {
    override var subsettedFeature: Resolved<Feature>
        get() = general as Resolved<Feature>
        set(value) { general = value }

    override var subsettingFeature: Resolved<Feature>
        get() = specific as Resolved<Feature>
        set(value) { specific = value }

    override fun clone(): Subsetting {
        return SubsettingImplementation(
            subsettingFeature = subsettingFeature,
            subsettedFeature = subsettedFeature
        )
    }
}