package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.MetaclassImplementation
import com.github.tukcps.sysmd.model.sysml.MetadataDefinition
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ItemDefinitionImplementation. */
class MetadataDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    MetadataDefinition, MetaclassImplementation(model,elementId = elementId)