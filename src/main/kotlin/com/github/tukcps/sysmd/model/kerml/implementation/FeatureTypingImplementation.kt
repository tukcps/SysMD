package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

/**
 * Feature typing relationship.
 */
class FeatureTypingImplementation(
    typedFeature: Feature = UnresolvedFeature("Base::things"),
    type: Type = UnresolvedType("Base::Anything"),
    elementType: String = "FeatureTyping"
): FeatureTyping, SpecializationImplementation(
    specific = typedFeature,
    general = type,
    elementType = elementType
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
            typedFeature = typedFeature,
            type = type,
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }
}