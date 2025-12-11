package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImportImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import kotlin.properties.Delegates.observable

abstract class InstantiationExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "InstantiationExpression"
) : InstantiationExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)
{
	/** Cache for `function` (implicit backing fields may not be mutable)  */
	private var _function : Function? = null
	/** Hack to avoid infinite resolution loop between `resolveLocal` and `getFunction` */
	private var _functionBusy : Boolean = false

    override val argument: List<Expression>
	    get() = membership.filterIsInstance<ParameterMembership>()
		    .filter { it.parameterDirection == Feature.FeatureDirectionKind.IN } // ?
			.map { it.ownedMemberParameter }
		    // TODO: What about return parameters & result expressions?
			// TODO: Standard says we need to take FeatureValue
			.filterIsInstance<Expression>()

	override var functionName : QualifiedName? by observable(null) { _, _, _ ->
		_function = null
	}

	/** If set, use this definition as the semantics for propagation */
	var builtinFunction : BuiltinFunction? = null

	override val behavior : List<Function>
		get() = listOfNotNull(function)

	/** The most specific overload of the applied function */
	override val function : Function? get() {
		_function?.let { return it }
		val model = this.model ?: throw IllegalStateException("Model not initialized")

		try
		{
			_functionBusy = true

			var f = functionName?.let {
				(owningNamespace ?: model.global).resolve(it)?.member<Function>()
			} ?: return null
			val args = argument

			if(! f.accepts(args))
				TODO("type or arity error")

			while(true)
			{
				val candidates = f.subtypes
					.filterIsInstance<Function>()
					.filter { it.accepts(args) }

				when(candidates.size) {
					0 -> break
					1 -> f = candidates.first()
					else -> TODO("Application of ${f.qualifiedName} is ambiguous between ${candidates.joinToString { it.qualifiedName!! }}")
				}
			}

			if(f.isAbstract)
				TODO("No concrete overload")

			_function = f

			// We need this context to evaluate the function's result as a member of this expression
			f.owningNamespace?.let { ns ->
				model.addOwnedRelationship(NamespaceImportImplementation(
					importedNamespace = ns,
					importingNamespace = this
				))
			}

			return f
		}
		finally {
			_functionBusy = false
		}
	}

	override val type : List<Type> get() = function?.result?.type ?: emptyList()

	var instantiatedType: Type = this //FIXME: Hack

    open fun instantiatedType(): Type? = this //FIXME: Should be self?

    //FIXME: Necessary to also implement TypeImplementation via composition or is this covered by the fact that expression already implements type?
    var internalType: TypeImplementation = this as TypeImplementation

	/** Called after clone to clone relevant owned elements recursively */
	protected fun postClone(to : InstantiationExpressionImplementation)
	{
		val m = to.model ?: model

		if(m === null)
			throw IllegalArgumentException("No session instance associated with cloned expression")

		to.model = m

		for(rel in ownedRelationship)
		{
			if(rel is ParameterMembership)
			{
				m.addOwnedMember(rel.ownedMemberParameter.clone(), to)
				/*val rel2 = rel.clone() as ParameterMembership
				rel2.owningStep = to
				rel2.ownedMemberParameter = rel.ownedMemberParameter.clone()

				m.addOwnedRelationship(rel2)*/
			}
		}
	}

	final override fun initialize()
	{
		val model = this.model ?: throw IllegalStateException("model not initialized")
		val function = this.function ?: throw IllegalStateException("function '${functionName}' not found")

		for(a in argument)
			a.initialize()

		val res = function.result

		val q = when {
			res === null -> TODO("Initialization without known function doesn't make sense")
			res.specializes(model.repo.booleanType!!) -> VectorQuantity(listOf(model.builder.Bool))
			res.specializes(model.repo.stringType!!) -> VectorQuantity(listOf(model.builder.Strings))
			// check integer before real because real :> integer
			res.specializes(model.repo.integerType!!) -> VectorQuantity(listOf(model.builder.Integers))
			res.specializes(model.repo.realType!!) -> VectorQuantity(listOf(model.builder.Reals), "?")
			else -> TODO("Expression is not of simple type")
		}

		upQuantity = q
		evalUp()
		downQuantity = q
	}

	final override fun evalUp()
	{
		result?.let { r ->
			r.evalUp()
			this.upQuantity = r.upQuantity
			return
		}

		builtinFunction?.let { f ->
			this.upQuantity = f.evalUp(argument.map { it.upQuantity })

			return
		}

		val function = this.function ?: throw IllegalStateException("function not initialized")

		function.builtin?.let {
			this.builtinFunction = it
			return evalUp() // re-run
		}

		TODO("Function result doesn't expose an expression yet")
		// this.result = function.result.???()
	}

	final override fun evalDown()
	{
		result?.let { r ->
			r.evalDown()
			this.downQuantity = r.downQuantity
			return
		}

		builtinFunction?.let { f ->
			val argument = this.argument
			val dq = f.evalDown(downQuantity, argument.map { it.upQuantity })

			for((a,q) in argument zip dq)
				a.downQuantity = q

			return
		}

		val function = this.function ?: throw IllegalStateException("function not initialized")

		function.builtin?.let {
			this.builtinFunction = it
			return evalDown() // re-run
		}

		TODO("Function doesn't expose a result expression yet")
	}

	final override fun evalUpRec()
	{
		for(a in argument)
			a.evalUpRec()
		evalUp()
	}

	final override fun evalDownRec()
	{
		evalDown()

		for(a in argument)
			a.evalDownRec()
	}

	/** Resolves argument expressions using function parameter names */
	override fun resolveLocal(name : SimpleName) : Membership?
	{
		if(_functionBusy)
			return null

		val function = function ?: return null

		return (function.parameterMembership zip this.parameterMembership).firstOrNull { (fm,_) ->
			fm.memberName == name || fm.memberShortName == name
		}?.second
	}
}