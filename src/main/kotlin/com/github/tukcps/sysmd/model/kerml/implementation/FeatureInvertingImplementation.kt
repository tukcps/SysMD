package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.FeatureInverting
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class FeatureInvertingImplementation(
    model : Session,
    elementId : Uuid,
) : FeatureInverting, RelationshipImplementation(
    model,
    elementId = elementId
) {
    var featureInverted: Feature = TODO()
    var invertingFeature: Feature = TODO()
    val owningFeature: Feature = TODO()
}
