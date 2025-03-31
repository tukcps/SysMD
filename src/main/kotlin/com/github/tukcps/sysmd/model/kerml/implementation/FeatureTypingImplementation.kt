package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

/**
 * Feature typing relationship.
 */
class FeatureTypingImplementation(
    typedFeature: Resolved<Feature> = Resolved(),
    type: Resolved<Type> = Resolved(),
    elementType: String = "FeatureTyping"
): FeatureTyping, SpecializationImplementation(
    specific = typedFeature,
    general = type,
    elementType = elementType
) {
    @Suppress("UNCHECKED_CAST")
    override val owningFeature: Resolved<Feature>
        get() = owner as Resolved<Feature>

    override val type: Resolved<Type>
        get() = general

    @Suppress("UNCHECKED_CAST")
    override val typedFeature: Resolved<Feature>
        get() = owner as Resolved<Feature>

    @Suppress("UNCHECKED_CAST")
    override fun clone(): FeatureTyping {
        return FeatureTypingImplementation(
            typedFeature = Resolved(source[0] as Resolved<Feature>),
            type = if (target.firstOrNull() == null) Resolved() else Resolved(target.firstOrNull() as Resolved<Type>)
        ).also { klon ->
            klon.isTransient = isTransient
            klon.model = model
        }
    }
}