package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.quantities.VectorQuantity

/** ref. 8.3.4.7.3 */
interface Expression: Step {
    var isModelLevelEvaluable: Boolean //true if not dependent on meta data

	var upQuantity : VectorQuantity
	var downQuantity : VectorQuantity

    fun modelLevelEvaluable(visited: Set<Feature>): Boolean //searches for circular dependencies. Redefined in FeatureReferenceExpression

    fun evaluate(target: Element): Set<Element> //recursively evaluates expression until literals are reached (if isModelLevelEvaluable == true)

    fun checkCondition(target: Element): Boolean //Modellevel evaluate this expression with the target. If result is LiteralBoolean, return it, else return false

	/** The Function that types this Expression */
    val function: Function?
	    get() = behavior.let {
			if(it.isEmpty()) null else it.single()
	    }

	// function redefines behaviour
	override val behavior : List<Function>
		get() = super.behavior.map { it as Function }

    val result: Expression?
		// FIXME: function should have return type as fallback (?)
	    get() = ownedRelationship.filterIsInstance<ResultExpressionMembership>().let {
			if(it.isEmpty()) null else it.single()
		}?.ownedResultExpression

	/** Initializes this expression subtree recursively.
	 * Assigns default domains to `upQuantity` and `downQuantity` of the correct types for this AST.
	 * Does not search for properties etc. in the symbol table as these might not be declared.
	 */
	fun initialize()
	fun evalUp()
	fun evalDown()
	fun evalUpRec()
	fun evalDownRec()

	fun toAstString(): String = StringBuilder().also {
		toAstString(it, 0)
	}.toString()

	fun toAstString(b : StringBuilder, precedence : Int)
}