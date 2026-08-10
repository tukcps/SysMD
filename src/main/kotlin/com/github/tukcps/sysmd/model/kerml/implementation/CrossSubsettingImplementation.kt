
package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.CrossSubsetting
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class CrossSubsettingImplementation(model : Session,elementId : Uuid = Uuid.random())
    : CrossSubsetting, SubsettingImplementation(model,elementId = elementId)
{
    var crossedFeature: Feature = TODO()
    val crossingFeature: Feature = TODO()
}
