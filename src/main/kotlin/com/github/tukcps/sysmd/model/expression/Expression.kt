package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Step

interface Expression: Step {
    var isModelLevelEvaluable: Boolean //true if not dependent on meta data

    fun modelLevelEvaluable(visited: Set<Feature>): Boolean //searches for circular dependencies. Redefined in FeatureReferenceExpression

    fun evaluate(target: Element): Set<Element> //recursively evaluates expression until literals are reached (if isModelLevelEvaluable == true)

    fun checkCondition(target: Element): Boolean //Modellevel evaluate this expression with the target. If result is LiteralBoolean, return it, else return false
}