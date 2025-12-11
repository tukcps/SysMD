package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DD

sealed class ExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "Expression",
): Expression, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType,
)
{
	/** Quantity produced by upwards evaluation.
	 * The current "value" of this expression
	 */
	override lateinit var upQuantity: VectorQuantity
	/** Quantity produced by downwards evaluation */
	override lateinit var downQuantity: VectorQuantity

    override var isModelLevelEvaluable: Boolean = false //When in doubt set it to false first

    override fun checkCondition(target: Element): Boolean {
        TODO("Not yet implemented")
    }

    override fun evaluate(target: Element): Set<Element> {
        TODO("Not yet implemented")
    }

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        TODO("Not yet implemented")
    }

	override fun evalDownRec()
	{
		val result = this.result

		if(result !== null)
		{
			result.evalDownRec()
			downQuantity = result.downQuantity
		}
		else
			evalDown()
	}

	override fun evalUpRec()
	{
		val result = this.result

		if(result !== null)
		{
			result.evalUpRec()
			upQuantity = result.upQuantity
		}
		else
			evalUp()
	}

	override val type : List<Type> get() = TODO("placeholder")

    override val function: Function? = null

    /** Non Standard **/
    open var internalValue: AstNode? = null

    //For solvers
    open var domain: DD<*>? = null //One DD should be enough to encode lb,ub for ints/reals and t,f,x for booleans
}