package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DD

sealed class ExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "Expression",
): Expression, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType,
) {
	/** Quantity produced by upwards evaluation.
	 * The current "value" of this expression
	 */
	override lateinit var upQuantity: VectorQuantity
	/** Quantity produced by downwards evaluation */
	override lateinit var downQuantity: VectorQuantity

    override val isModelLevelEvaluable: Boolean = false //When in doubt set it to false first

	override val astString by lazy {
		StringBuilder().also {
			toAstString(it, 0)
		}.toString()
	}

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

    override val function: Function? = null

	final override val behavior : List<Function>
		get() = super<Expression>.behavior

	protected abstract fun learnType() : List<Type>

	final override fun initType()
	{
		if(this.type.isNotEmpty())
			return

		for(o in membership.flatMap { it.ownedElement }.filterIsInstance<Expression>())
			o.initType()

		learnType().ifEmpty {
			// TODO: report error
			listOf(model!!.global.resolve("Base::Anything")!!.member<Type>()!!)
			// TODO: figure out why initialize won't resolve this type:
			// listOf(UnresolvedType("Base::Anything"))
		}.forEach {
			// FIXME: attach these to results, not the expressions themselves
			model!!.addOwnedRelationship(FeatureTypingImplementation(
				this,
				it
			).apply {
				isTransient = true
			})
		}
	}

    /** Non Standard **/
    open var internalValue: AstNode? = null

    //For solvers
    open var domain: DD<*>? = null //One DD should be enough to encode lb,ub for ints/reals and t,f,x for booleans

	override fun updateFrom(template: Element) {
		super.updateFrom(template)

		// copy type information
		template.ownedRelationship.filterIsInstance<FeatureTyping>().forEach { typing ->
			model!!.addOwnedRelationship(FeatureTypingImplementation(
				this,
				typing.type
			).apply {
				isTransient = true
			})

		}
	}

	protected fun localIdentifier(x : Element?)
		= if(x is Unresolved) x.relativeName else x?.qualifiedName ?: "'missing feature'"

	abstract override fun clone() : ExpressionImplementation

	override fun toString(): String = "[$elementType] $astString"
}