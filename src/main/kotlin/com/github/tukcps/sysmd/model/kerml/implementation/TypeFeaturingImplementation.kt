package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.TypeFeaturing
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class TypeFeaturingImplementation(model : Session,elementId : Uuid = Uuid.random())
    : TypeFeaturing, RelationshipImplementation(model,elementId = elementId)
{
    override var featureOfType: Feature = TODO()
    override var featuringType: Type = TODO()
    override val owningFeatureOfType: Feature? = TODO()
}
