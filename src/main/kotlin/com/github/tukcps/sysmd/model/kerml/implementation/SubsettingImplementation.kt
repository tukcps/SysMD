package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Subsetting
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Like a Specialization, but a relationship between Features that are a subset of a superset.
 */
@Suppress("UNCHECKED_CAST")
open class SubsettingImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    subsettingFeature: Feature = UnresolvedFeature(model, "Base::things"), // "that"
    subsettedFeature: Feature = UnresolvedFeature(model, "Base::things"),
): Subsetting, SpecializationImplementation(
    model,
    elementId = elementId,
    specific = subsettingFeature,
    general = subsettedFeature,
) {
    override var subsettedFeature: Feature
        get() = when (val gen = general) {
            is Feature -> gen
            else -> {
                target.firstOrNull() as? Feature
                    ?: throw IllegalStateException("SubsettingImplementation.subsettedFeature: general is not a Feature but ${gen.javaClass.simpleName ?: "null"}")
            }
        }
        set(value) { general = value }

    override var subsettingFeature: Feature
        get() = specific as Feature
        set(value) { specific = value }

    override fun clone(): Subsetting = SubsettingImplementation(
        model,
        subsettingFeature = subsettingFeature,
        subsettedFeature = subsettedFeature
    )
}