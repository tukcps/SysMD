package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

@Suppress("UNCHECKED_CAST")
class ReferenceSubsettingImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    referencingFeature: Feature = UnresolvedFeature(model),
    referencedFeature: Feature = UnresolvedFeature(model),
) : ReferenceSubsetting, SubsettingImplementation(
    model,
    elementId = elementId,
    subsettingFeature = referencingFeature,
    subsettedFeature = referencedFeature,
) {
    override var referencedFeature: Feature
        get() = general as? Feature ?: throw SysMDException(kind = Issue.Kind.FATAL, message = "Expecting feature, but got $general", element = general)

        set(value) { general = value }

    override var referencingFeature: Feature
        get() = specific as? Feature ?: throw SysMDException(kind = Issue.Kind.FATAL, message = "Expecting feature, but got $general", element = general)
        set(value) { specific = value}

    override fun clone(): ReferenceSubsetting = ReferenceSubsettingImplementation(
        model,
        referencingFeature = source.firstOrNull() as Feature,
        referencedFeature = target.firstOrNull() as Feature,
    ).also { klon ->
        klon.isTransient = isTransient
    }

    override fun updateFrom(template: Element) {
        require(template is ReferenceSubsetting)
        super.updateFrom(template)
    }
}