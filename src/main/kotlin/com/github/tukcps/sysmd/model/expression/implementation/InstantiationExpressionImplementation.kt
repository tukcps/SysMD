package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import kotlin.properties.Delegates.observable

abstract class InstantiationExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "InstantiationExpression"
) : InstantiationExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)
{
	/** The specific function actually evaluated for this expression, i.e. the proper overload */
	final override var function : Function? = null
		private set

	val argumentMembership : List<ParameterMembership> get() = membership
			.filterIsInstance<ParameterMembership>()
			.filter { it.parameterDirection == Feature.FeatureDirectionKind.IN } // TODO
			.sortedBy { it.parameterIndex }

    override val argument: List<Expression> get() = argumentMembership
			.map { it.ownedMemberParameter }
		    .filterIsInstance<Expression>()

	override var functionName : QualifiedName? by observable(null) { _, _, _ ->
		function = null
	}

	/** If set, use this definition as the semantics for propagation */
	var builtinFunction : BuiltinFunction? = null

	/** The most general overload of the instantiated function, i.e. exactly the function named by `functionName` */
	val rootFunction : Function? get() = functionName?.let {
		(owningNamespace ?: model?.global)?.resolve(it)?.member<Function>()
	}

	var instantiatedType: Type = this //FIXME: Hack

	/** reorders and renames the argument list to conform with the given function */
	private fun fixArguments(f : Function)
	{
		val args = argumentMembership
		val wanted = f.parameterMembership

		if(args.isEmpty())
			return

		if(args.first().parameterIndex < 0)
		{ // named args, have to populate indices
			val visited = HashSet<ParameterMembership>()

			for(a in args)
			{
				val id = Identification(a.memberShortName, a.memberName)
				val w = wanted.firstOrNull { Identification(it.memberShortName, it.memberName) == id }

				if(w === null)
					TODO("No argument named '${a.name}' in '${f.qualifiedName}'")
				if(! visited.add(w))
					TODO("Duplicate named argument '${a.name}'")

				a.parameterIndex = w.parameterIndex
			}
		}
	}

	/** Also performs overload resolution
	 * FIXME: perhaps too early?
	 */
	override fun learnType() : List<Type>
	{
		val model = model ?: throw IllegalStateException("model not set")
		assert(function === null)

		var f = rootFunction ?: run {
			// TODO: report error
			// throw IllegalStateException("Cannot resolve function name '$functionName'")
			return emptyList()
		}
		try { fixArguments(f) } catch(_ : Exception) { return emptyList() }
		val args = argument

		if(! f.accepts(args))
		{
			// TODO: report type or arity error
			return emptyList()
		}

		while(true)
		{
			val candidates = f.subtypes
				.filterIsInstance<Function>()
				.filter { it.accepts(args) }

			when(candidates.size) {
				0 -> break
				1 -> f = candidates.first()
				else -> return emptyList() // TODO("Application of ${f.qualifiedName} is ambiguous between ${candidates.joinToString { it.qualifiedName!! }}")
			}
		}

		/* if(f.isAbstract)
			 TODO("No concrete overload") */

		function = f

		return f.result?.type ?: emptyList() // TODO("Calling function without result?")

		// TODO: fix resolution
		// We need this context to evaluate the function's result as a member of this expression
		/*f.owningNamespace?.let { ns ->
			model.addOwnedRelationship(NamespaceImportImplementation(
				importedNamespace = ns,
				importingNamespace = this
			))
		}*/
	}

	open fun instantiatedType(): Type? = this //FIXME: Should be self?

    //FIXME: Necessary to also implement TypeImplementation via composition or is this covered by the fact that expression already implements type?
    var internalType: TypeImplementation = this as TypeImplementation

	override fun updateFrom(template : Element)
	{
		super.updateFrom(template)

		if(template is InstantiationExpressionImplementation)
		{
			functionName = template.functionName

			for(rel in template.ownedRelationship.filterIsInstance<ParameterMembership>())
				model!!.addOwnedMember(rel.ownedMemberParameter.clone(), this)
		}
	}

	final override fun initialize()
	{
		initType()
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

	// TODO: fix resolution
	/** Resolves argument expressions using function parameter names */
	/*override fun resolveLocal(name : SimpleName) : Membership?
	{
		val function = function ?: return null

		return (function.parameterMembership zip this.parameterMembership).firstOrNull { (fm,_) ->
			fm.memberName == name || fm.memberShortName == name
		}?.second
	}*/

	abstract override fun clone(): InstantiationExpressionImplementation
}