package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function

interface Expression: Step {
    var isModelLevelEvaluable: Boolean //true if not dependent on meta data

    fun modelLevelEvaluable(visited: Set<Feature>): Boolean //searches for circular dependencies. Redefined in FeatureReferenceExpression

    fun evaluate(target: Element): Set<Element> //recursively evaluates expression until literals are reached (if isModelLevelEvaluable == true)

    fun checkCondition(target: Element): Boolean //Modellevel evaluate this expression with the target. If result is LiteralBoolean, return it, else return false

    val function: Function? //function that types the expression

    val result: Feature?
		// TODO: function should have return type as fallback
	    get() = ownedRelationship.filterIsInstance<ResultExpressionMembership>().firstOrNull()?.ownedResultExpression
}