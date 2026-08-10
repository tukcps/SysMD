package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Redefinition
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
class RedefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    redefiningFeature: Feature = UnresolvedFeature(model, "Base::things"),
    redefinedFeature: Feature = UnresolvedFeature(model, "Base::things"),
): Redefinition, SubsettingImplementation(
    model,
    elementId = elementId,
    subsettedFeature = redefinedFeature,
    subsettingFeature = redefiningFeature
) {
    override var redefinedFeature: Feature
        get() = subsettedFeature
        set(value) {
            target = mutableListOf(value)
            general = value
        }

    override var redefiningFeature: Feature
        get() = source.first() as Feature
        set(value) { source = mutableListOf(value) }

    override fun clone(): Redefinition = RedefinitionImplementation(
        model,
        redefiningFeature = redefiningFeature,
        redefinedFeature = redefinedFeature,
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        require(template is Redefinition)
        super.updateFrom(template)
    }
}