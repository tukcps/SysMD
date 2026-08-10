package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression

interface ViewDefinition : PartDefinition {

    val satisfiedViewpoint: MutableList<ViewpointUsage>
    val view: MutableList<ViewUsage>
    val viewCondition: MutableList<Expression>
    val viewRendering: RenderingUsage?

}
