package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.FeatureTyping
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Feature typing relationship.
 */
open class FeatureTypingImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    typedFeature: Feature = UnresolvedFeature(model, "Base::things"),
    type: Type = UnresolvedType(model, "Base::Anything"),
): FeatureTyping, SpecializationImplementation(
    model,
    elementId = elementId,
    specific = typedFeature,
    general = type,
) {
    @Suppress("UNCHECKED_CAST")
    override val owningFeature: Feature
        get() = owningRelatedElement as Feature

    override val type: Type
        get() = general

    @Suppress("UNCHECKED_CAST")
    override val typedFeature: Feature
        get() = owner as Feature

    @Suppress("UNCHECKED_CAST")
    override fun clone(): FeatureTyping {
        return FeatureTypingImplementation(
            model,
            typedFeature = typedFeature,
            type = type,
        ).also { klon ->
            klon.isTransient = isTransient
        }
    }
}