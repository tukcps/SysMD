package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Element

interface ViewUsage : PartUsage {
    val exposedElement: MutableList<Element>
    val satisfiedViewpoint: MutableList<ViewpointUsage>
    val viewCondition: MutableList<Expression>
    val viewDefinition: ViewDefinition?
    val viewRendering: RenderingUsage?

    fun includeAsExposed(element: Element): Boolean
}
