package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Step
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class StepImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    Step,
    FeatureImplementation(model,elementId = elementId)
{
    override fun clone() = StepImplementation(model).also { it.updateFrom(this) }
}