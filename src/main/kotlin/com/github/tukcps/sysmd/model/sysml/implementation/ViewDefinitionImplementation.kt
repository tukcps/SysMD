package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.RenderingUsage
import com.github.tukcps.sysmd.model.sysml.ViewDefinition
import com.github.tukcps.sysmd.model.sysml.ViewUsage
import com.github.tukcps.sysmd.model.sysml.ViewpointUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: PartDefinitionImplementation. */
class ViewDefinitionImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ViewDefinition,
    PartDefinitionImplementation(model,elementId = elementId)
{
    
    override val satisfiedViewpoint: MutableList<ViewpointUsage> = TODO()
    override val view: MutableList<ViewUsage> = TODO()
    override val viewCondition: MutableList<Expression> = TODO()
    override val viewRendering: RenderingUsage? = TODO()
    
}
