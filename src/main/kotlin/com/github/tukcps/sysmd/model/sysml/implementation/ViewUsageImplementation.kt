package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.sysml.RenderingUsage
import com.github.tukcps.sysmd.model.sysml.ViewDefinition
import com.github.tukcps.sysmd.model.sysml.ViewUsage
import com.github.tukcps.sysmd.model.sysml.ViewpointUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ViewUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ViewUsage,
    PartUsageImplementation(model,elementId = elementId)
{
    
    override val exposedElement: MutableList<Element> = TODO()
    override val satisfiedViewpoint: MutableList<ViewpointUsage> = TODO()
    override val viewCondition: MutableList<Expression> = TODO()
    override val viewDefinition: ViewDefinition? = TODO()
    override val viewRendering: RenderingUsage? = TODO()
    
    override fun includeAsExposed(element: Element): Boolean = TODO()
    
}
