@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.exceptions.LexicalError
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName

fun KerML.Expression() : Expression = ConditionalExpression()

// FIXME: Result members not to standard yet

/** Grouped information on a set of binary operators that share a precedence
 * @param preserveTokens If true, the operator tokens should not be consumed before the next level of expression is parsed
 * @param operatorNameOverride If set, overrides all operators in this level to use that name instead of their parsed tokens
 * @param legacy If true, this operator is only expected in legacy code, and its right side contains raw names
 */
private data class PrecedenceLevel(
	val operatorInformation: Map<Token.Kind, BinaryOperatorInformation>,
	val rightAssociative: Boolean,
	val preserveTokens: Boolean = false,
	val operatorNameOverride: String? = null,
	val legacy : Boolean = false
) {
	init {
		require(operatorInformation.isNotEmpty())
	}

	/** The tokens corresponding to these operators */
	val tokens get() = operatorInformation.keys

	operator fun get(tk : Token.Kind) = operatorInformation[tk]
}

fun tokenOf(op : String) = Token.kerMLKeywords[op]
	?: op.singleOrNull()?.let { Token.charTokens[it] }
	?: Token.Kind.entries.firstOrNull { it.toString() == op }

/** Produces an operator table organized by precedence by applying some filter to binary operators.
 * Filters out all operators without corresponding tokens.
 */
private val binaryOperators : List<PrecedenceLevel> = BinaryOperatorInformation.bySymbol.entries.filter { (_,i) ->
	!i.special
}.groupBy { (_,i) ->
	i.precedence
}.entries.sortedBy {
	it.key
}.mapNotNull { (_,ops) ->
	val single = ops.singleOrNull()

	// special handling of space operator
	if(single?.key == " ")
	{
		return@mapNotNull PrecedenceLevel(
			listOf(INTEGER_LIT, FLOAT_LIT, STRING_LIT, NAME_LIT).associateWith { single.value },
			rightAssociative = false,
			preserveTokens = true,
			operatorNameOverride = " ",
			legacy  = true
		)
	}

	val tks = ops.mapNotNull { (tk,op) -> tokenOf(tk)?.to(op) }

	if(tks.isEmpty())
		return@mapNotNull null

	val ra = ops.any { it.value.rightAssociative }
	// associativity should be uniform within  one precedence level
	assert(!ra || ops.all { it.value.rightAssociative })

	PrecedenceLevel(tks.toMap(), rightAssociative = ra)
}

private val unaryOperators = UnaryOperatorInformation.bySymbol.entries.mapNotNull { (k,v) ->
	tokenOf(k)?.to(v)
}.toMap()

/* FIXME: We cannot use semantic actions because we don't know whether to insert a containing namespace
    until AFTER infix operator has been seen, i.e. left argument is finished
*/
private inline fun<T : Expression> KerML.expressionImplementation(ctor : () -> T, init : (T) -> Unit) = ctor().also {
	it.model = model
	it.direction = Feature.FeatureDirectionKind.IN

	init(it)
}

private fun KerML.invocationExpression(f : QualifiedName, operands : List<Expression>) : InvocationExpression
= expressionImplementation(::InvocationExpressionImplementation) {
	it.functionName = f

	for(op in operands)
		model.addOwnedMember(op, it)
}

private fun KerML.invocationExpression(f : QualifiedName, operands : Map<String, Expression>) : InvocationExpression
= expressionImplementation(::InvocationExpressionImplementation) {
	it.functionName = f

	for((name, op) in operands)
	{
		op.declaredName = name
		op.declaredShortName = name
		val membership = model.addOwnedMember(op, it).owningRelationship as ParameterMembership
		// signal that these need to be reordered
		membership.parameterIndex = -1
	}
}

private fun KerML.operatorExpression(op : String, vararg operands : Feature) : OperatorExpression
= expressionImplementation(::OperatorExpressionImplementation) {
	it.operator = op

	for(op in operands)
		model.addOwnedMember(op, it)
}

private fun KerML.ConditionalExpression() : Expression
= if(consumeIfTokenIs(IF)) {
	val c = ConditionalExpression()
	QUESTION.consume()
	val t = ConditionalExpression()
	ELSE.consume()
	val e = ConditionalExpression()

	operatorExpression("if", c, t, e)
}
else
	BinaryOperatorExpression(0)

private fun functionName(operator : Token.Kind) = operator.toString().lowercase()

/** handles all (non-primary) binary operators, including condition operators
 * @param step The precedence class of operator to parse, starting at 0
 */
private fun KerML.BinaryOperatorExpression(step : Int) : Expression
{
	if(step >= binaryOperators.size)
		return UnaryOperatorExpression()

	var left = BinaryOperatorExpression(step + 1)
	val lvl = binaryOperators[step]

	/** retrieves the token kind of the applied operator */
	fun tk() = (if(lvl.preserveTokens) token else consumedToken).kind
	/** retrieves the proper function name for an operator token*/
	fun op(tk : Token.Kind) = lvl.operatorNameOverride ?: functionName(tk)
	// FIXME: check which scopes legacy operators may appear in & skip them otherwise

	if(lvl.rightAssociative)
	{
		optional(lvl.tokens, consume = ! lvl.preserveTokens) {
			val tk = tk()
			// right-associating type operators make no sense
			assert(lvl[tk]?.typeOperand == false)
			val right = withUnresolvedNames(lvl.legacy) { BinaryOperatorExpression(step) }// parser can handle right-recursion here

			left = operatorExpression(op(tk), left, right)
		}
	}
	else
	{
		// FIXME: MetaclassificationExpression not quite right (`@@` and `meta`)
		noOrMore(lvl.tokens, consume = ! lvl.preserveTokens) {
			val tk = tk()
			val right = withUnresolvedNames(lvl.legacy) {
				if(lvl[tk]!!.typeOperand) TypeReferenceMember() else BinaryOperatorExpression(step + 1)
			}

			left = operatorExpression(op(tk), left, right)
		}
	}

	return left
}

private fun KerML.UnaryOperatorExpression() : Expression
{
	optional(unaryOperators.keys, consume = true) {
		val op = consumedToken.kind
		val right = if(unaryOperators[op]!!.typeOperand) TypeReferenceMember() else UnaryOperatorExpression()

		return operatorExpression(functionName(op), right)
	}

	return PrimaryExpression()
}

private fun KerML.PrimaryExpression() : Expression
{
	var left : Expression = SequenceExpression()
	val stopEarly = mutableSetOf<Token.Kind>() // hack to break out of noOrMore

	noOrMore(start = setOf(HASHTAG, ARROW, DOT, LBRACE, LCBRACE), stop = stopEarly) {
		alternatives {
			HASHTAG then { // IndexExpression
				val ixs = SequenceExpression()
				left = expressionImplementation(::IndexExpressionImplementation) {
					model.addOwnedMember(left, it)
					model.addOwnedMember(ixs, it)
				}
			}
			ARROW then { // FunctionOperationExpression; `x->f(y,z)` is syntax sugar for `f(x,y,z)`
				// FIXME: Standard uses "InvocationTypeMember" but lacks a definition
				consume(NAME_LIT)
				val f = consumedToken.string

				alternatives {
					NAME_LIT starts {
						val func = QualifiedName()
						left = invocationExpression(f, listOf(
							left,
							referenceTo(func)
						))
					}
					LCURBRACE starts {
						val lambda = BodyExpression()
						left = invocationExpression(f, listOf(left, lambda))
					}
					LBRACE starts {
						val args = ArgumentList().first ?: TODO("Named arguments not supported yet")
						left = invocationExpression(f, listOf(left) + args)
					}
				}
			}

			DOT starts {
				// break out of noOrMore
				stopEarly.add(DOT)
			}

			// FIXME: Replace with .?
			(DOT to QUESTION) starts { // SelectExpression
				consume(DOT)
				consume(QUESTION)
				val body = BodyExpression()

				left = expressionImplementation(::SelectExpressionImplementation) {
					model.addOwnedMember(left, it)
					model.addOwnedMember(body, it)
				}
			}

			(DOT then LCURBRACE) starts {// CollectExpression
				consume(DOT)
				val body = BodyExpression()

				left = expressionImplementation(::CollectExpressionImplementation) {
					model.addOwnedMember(left, it)
					model.addOwnedMember(body, it)
				}
			}

			(DOT then NAME_LIT) starts { // FeatureChainExpression

                if (nextNextToken.kind == HAS_A) {
                    stopEarly.add(DOT)
                } else {

                    consume(DOT)
                    val ch = FeatureChain()

                    left = expressionImplementation(::FeatureChainExpressionImplementation) {
                        it.targetFeature = ch
                        model.addOwnedMember(left, it)
                    }
                }
			}

			LCBRACE then { // BracketExpression
				val right : Expression = withUnresolvedNames { SequenceExpressionList() }
				consume(RCBRACE)

				left = operatorExpression("[", left, right)
			}
		}
	}


	return left
}

/** An OPTIONALLY parenthesized sequence expression (not exactly to standard) */
private fun KerML.SequenceExpression() : Expression
{
	if(consumeIfTokenIs(LBRACE))
	{
		if(consumeIfTokenIs(RBRACE))
			return NullExpressionImplementation()

		return SequenceExpressionList().also { RBRACE.consume() }
	}

	return BaseExpression()
}

/** WITHOUT parens */
private fun KerML.SequenceExpressionList() : Expression
{
	var left = Expression()

	noOrMore(COMMA, consume = true) {
		if(tokenIs(RBRACE))
			return left

		val right = Expression()
		left = operatorExpression(",", left, right)
	}

	return left
}

/** WITH parens
 * @return A positional or named argument list. Both populated when the argument list is ambiguous (i.e. empty)
 * */
private fun KerML.ArgumentList() : Pair<List<Expression>?, Map<QualifiedName, Expression>?>
{
	// TODO: NamedArgumentMembers
	LBRACE.consume()

	if(consumeIfTokenIs(RBRACE))
		return Pair(emptyList(), emptyMap())

	var list : MutableList<Expression>? = null
	var map : MutableMap<QualifiedName, Expression>? = null

	val (headName, head) = SingleArgument(null)

	if(headName === null)
		list = mutableListOf(head)
	else
		map = mutableMapOf(headName to head)

	noOrMore(COMMA, consume = true) {
		// trailing comma
		if(consumeIfTokenIs(RBRACE))
			return Pair(list, map)

		val (n,v) = SingleArgument(headName !== null)

		list?.add(v)
		map?.put(n!!, v)?.also { TODO("Name conflict") }
	}

	RBRACE.consume()
	return Pair(list, map)
}

/**
 * @param named Whether to expect a name for the argument. `null` to accept either.
 * */
private fun KerML.SingleArgument(named : Boolean?) : Pair<QualifiedName?, Expression>
{
	val name = if(named == true || (named == null && token.kind == NAME_LIT && nextToken.kind == EQ)) {
		NAME_LIT.consume()
		val name = consumedToken.string
		EQ.consume()
		name
	} else
		null

	return name to Expression()
}

/** A set of special, legacy SysMD functions.
 * Names within their operands are not resolved statically.
 */
private val specialLegacyFunctions = setOf(
	"owns",
	"sumOverParts", "productOverParts",
	"sumOverSubclasses", "productOverSubclasses",
	"sumOverPartsNotTransitive", "productOverPartsNotTransitive",
	"sumOverSubclassesNotTransitive", "productOverSubclassesNotTransitive"
)

private fun KerML.referenceTo(name : QualifiedName)
= if(unresolvedNamesMode) expressionImplementation(::RawNameExpressionImplementation) {
	it.rawName = name
} else expressionImplementation(::FeatureReferenceExpressionImplementation) {
	it.referent = UnresolvedFeature(name)
}

private fun KerML.BaseExpression() : Expression
{
	var expr : Expression? = null

	alternatives {
		NULL then {
			expr = NullExpressionImplementation()
		}
		NAME_LIT starts { // FeatureReferenceExpression | InvocationExpression
			val qn = QualifiedName()

			expr = when {
				tokenIs(LBRACE) -> { // InvocationExpression
					val (positional,named) = if(qn in specialLegacyFunctions) withUnresolvedNames { ArgumentList() }
											 else ArgumentList()

					// FIXME: Standard uses "InstatiatedTypeMember" here which lacks a definition
					positional?.let { invocationExpression(qn, it) }
						?: named?.let { invocationExpression(qn, it) }
				}
				match(DOT, METADATA) -> {
					DOT.consume()
					METADATA.consume()

					expressionImplementation(::MetadataAccessExpressionImplementation) {
						it.referencedElement = UnresolvedElement(qn)
					}
				}
				// FeatureReferenceExpression
				else -> referenceTo(qn)
			}
		}
		// TODO: ConstructorExpression, missing "new" keyword
		LCURBRACE starts { // BodyExpression
			expr = BodyExpression()
		}

		TRUE then {
			expr = expressionImplementation(::LiteralBooleanImplementation) {
				it.value = true
			}
		}
		FALSE then {
			expr = expressionImplementation(::LiteralBooleanImplementation) {
				it.value = false
			}
		}

		LCBRACE then { // SysML extension: bracketed ranges
			// we could also just parse an arbitrary expression here...
			fun bound() : Expression
			{
				var sign = 1
				alternatives {
					PLUS then {}
					MINUS then { sign = -1 }
					others { }
				}
				var x = Double.NaN
				alternatives {
					INTEGER_LIT then { x = consumedToken.number }
					FLOAT_LIT then { x = consumedToken.number }
				}
				// negative literals might cause problems elsewhere
				return expressionImplementation(::LiteralRationalImplementation) {
					it.value = sign * x
				}
			}

			val l = bound()
			consume(DOTDOT)
			val r = bound()
			consume(RCBRACE)

			expr = operatorExpression("..", l, r)
		}

		FLOAT_LIT then {
			expr = expressionImplementation(::LiteralRationalImplementation) {
				it.value = consumedToken.number
			}
		}

		INTEGER_LIT then {
			expr = expressionImplementation(::LiteralIntegerImplementation) {
				it.value = consumedToken.number.toLong()
			}
		}

		STRING_LIT then {
			expr = expressionImplementation(::LiteralStringImplementation) {
				it.value = consumedToken.string
			}
		}

		TIMES then {
			expr = expressionImplementation(::LiteralInfinityImplementation) {}
		}

		IF starts {
			expr = ConditionalExpression()
		}

		// hack to implement the percentage unit
		PERCENT then {
			expr = referenceTo("%")
		}
	}

	// fixme: is this the right fallback?
	return expr ?: expressionImplementation(::NullExpressionImplementation) {}
}

/** A function body enclosed in {}
	```
		ExpressionBody : Expression = '{' FunctionBodyPart '}'
	```
	 FIXME: Standard says this should be a FeatureReferenceExpression, but never assigns a FeatureReferenceMember
            Also, it references multiple features, and owns those features
 */
private fun KerML.BodyExpression() : Expression
{
	if(! tokenIs(LCURBRACE))
		throw LexicalError(this, "after '$consumedToken': expected '{' but read '$token' ")

	// TODO: Hack! What about owner?
	return FeatureActions<Expression>(semantics, ::BodyExpressionImplementation).parse {
		FunctionBody()
	}
}

private fun KerML.TypeReferenceMember() : Feature
{
	val typeName = QualifiedName()
	return FeatureImplementation().also {
		model.addOwnedRelationship(FeatureTypingImplementation(
				type = UnresolvedType(typeName)
			), it)
	}
}
