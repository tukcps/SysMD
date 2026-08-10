package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.PayloadFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class PayloadFeatureImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    PayloadFeature,
    FeatureImplementation(model,elementId = elementId)
{
}
