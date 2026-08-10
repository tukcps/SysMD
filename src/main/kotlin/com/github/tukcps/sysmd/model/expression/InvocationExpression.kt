package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature

interface InvocationExpression: InstantiationExpression
{
    override fun evaluate(target: Element): Set<Element>
    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean
    override fun clone(): InvocationExpression
}