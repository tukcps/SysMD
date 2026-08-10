@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.*
import com.github.tukcps.sysmd.compiler.scanner.*
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.expression.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.datamodel.*
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.*

/** An expression WITHOUT implicit owning membership */
fun KerML.OwnedExpression() : ElementData = ConditionalExpression()

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

private fun KerML.ConditionalExpression() : ElementData = when {
	tokenIs(IF) -> OperatorExpressionAction(semantics, "if").parse {
		consume(IF)// consume here so that indices are right
		addArgument(ConditionalExpression())
		QUESTION.consume()
		addArgument(ConditionalExpression())
		ELSE.consume()
		addArgument(ConditionalExpression())
	}
	else -> BinaryOperatorExpression(0)
}

private fun operator(operator : Token.Kind) = operator.toString().lowercase()

private fun ElementAction.setIndices(left : ElementData?)
{
	left?.indices?.let {
		element.indices = it
	}
}
private fun ElementAction.setIndices(tk : Token)
{
	element.indices = tk.indices
}


/** handles all (non-primary) binary operators
 * @param step The precedence class of operator to parse, starting at 0
 */
private fun KerML.BinaryOperatorExpression(step : Int, left : ElementData? = null) : ElementData
{
	if(step >= binaryOperators.size)
		return left ?: UnaryOperatorExpression()

	// if true, need to re-visit to handle left-associating operator
	var revisit = false

	val left = left ?: BinaryOperatorExpression(step + 1)
	val lvl = binaryOperators[step]
	val opTk = token.kind
	val i = lvl[opTk]

	if(i === null)
		return left

	val expr = OperatorExpressionAction(
		semantics, lvl.operatorNameOverride ?: operator(opTk), i
	).parse {
		// copy index from LHS
		setIndices(left)

		if(! lvl.preserveTokens)
			nextToken()

		addArgument(left)
		assert(!(lvl.rightAssociative && i.typeOperand)) { "right-associating type operators make no sense" }

		val right = withUnresolvedNames(lvl.legacy) {
			when {
				lvl.rightAssociative -> BinaryOperatorExpression(step) // Right recursion is OK here
				i.typeOperand -> TypeReference()
				else -> {
					revisit = true
					// if another operator from this level is used, this leaves it suffix to be parsed next
					BinaryOperatorExpression(step + 1)
				}
			}
		}

		addArgument(right)
	}

	return if(revisit) BinaryOperatorExpression(step, left = expr) else expr
}

private fun KerML.UnaryOperatorExpression() : ElementData
{
	val uopTk = token.kind
	val uop = unaryOperators[uopTk]

	return when {
		uop !== null -> OperatorExpressionAction(semantics, operator(uopTk)).parse {
			uopTk.consume() // consume here s.t. indices are right
			addArgument(if(uop.typeOperand) TypeReference() else UnaryOperatorExpression())
		}
		else -> PrimaryExpression()
	}
}

private fun KerML.PrimaryExpression(left : ElementData? = null) : ElementData
{
	var left = left ?: SequenceExpression()
	val stopEarly = mutableSetOf<Token.Kind>() // hack to break out of noOrMore

	noOrMore(start = setOf(HASHTAG, ARROW, DOT, LBRACE, LCBRACE), stop = stopEarly) {
		alternatives {
			HASHTAG then { // IndexExpression
				left = OperatorExpressionAction(semantics, "#", type = ElementType.IndexExpression).parse {
					setIndices(left)
					addArgument(left)
					addArgument(SequenceExpression()) // indices
				}
			}
			ARROW then { // FunctionOperationExpression; `x->f(y,z)` is syntax sugar for `f(x,y,z)`
				left = InvocationExpressionAction(semantics).parse {
					setIndices(left)
					// FIXME: Standard uses "InvocationTypeMember" but lacks a definition
					consume(NAME_LIT)
					function = consumedToken.string
					addArgument(left)

					alternatives {
						NAME_LIT starts {
							addArgument(FunctionReferenceArgument())
						}
						LCURBRACE starts {
							addArgument( FeatureAction(semantics, owningMembershipType = null).parse {
								// BodyArgument : Feature
								semantics.addOwnedElement(BodyExpression(), ElementType.FeatureValue)
							})
						}
						LBRACE starts {
							ArgumentList()
						}
					}
				}
			}

			DOT starts {
				// break out of noOrMore
				stopEarly.add(DOT)
			}

			// FIXME: Replace with .?
			(DOT then QUESTION) starts { // SelectExpression
				left = OperatorExpressionAction(semantics, "select", type = ElementType.SelectExpression).parse {
					consume(DOT)
					consume(QUESTION)
					setIndices(left)
					addArgument(left)
					addArgument(BodyExpression()) // body
				}
			}

			(DOT then LCURBRACE) starts {// CollectExpression
				left = OperatorExpressionAction(semantics, "collect", type = ElementType.CollectExpression).parse {
					consume(DOT)
					setIndices(left)
					addArgument(left)
					addArgument(BodyExpression())
				}
			}

			(DOT then NAME_LIT) starts { // FeatureChainExpression
				when {
					nextNextToken.kind == HAS_A -> stopEarly.add(DOT)
					unresolvedNamesMode && left.isNameLiteral == true -> {
						// treat an entire path as name literal
						consume(DOT)
						left.literalStringValue += "." + FeatureChain()
						left.indices?.let { left.indices = it.first .. consumedToken.indices.last }
					}
					else -> {
						left = OperatorExpressionAction(semantics, ".", type = ElementType.FeatureChainExpression).parse {
							setIndices(left)
							consume(DOT)
							addArgument(left)
							// fixme: not standard compliant
							element.targetFeature = FeatureChain()
						}
					}
				}
			}

			LCBRACE then { // BracketExpression
				left = OperatorExpressionAction(semantics, "[").parse {
					setIndices(left)
					addArgument(left)
					addArgument(withUnresolvedNames { SequenceExpressionList() })
					consume(RCBRACE)
				}
			}
		}
	}

	return left
}

/** An OPTIONALLY parenthesized sequence expression (not exactly to standard) */
private fun KerML.SequenceExpression() : ElementData
{
	val p0 = token.indices

	if(consumeIfTokenIs(LBRACE))
	{
		if(consumeIfTokenIs(RBRACE))
		{
			return ExpressionAction(semantics, ElementType.NullExpression).parse {
				element.indices = p0
			}
		}

		return SequenceExpressionList().also { RBRACE.consume() }
	}

	return BaseExpression()
}

/** A sequence expression WITHOUT parenthesis.
 * Cannot be parsed as a binary operator because trailing commas are allowed.
 */
private fun KerML.SequenceExpressionList() : ElementData
{
	var left = OwnedExpression()

	if(tokenIsNot(COMMA))
		return left

	noOrMore(COMMA, consume = true) {
		// allow trailing
		if(token.kind in setOf(RBRACE, RCBRACE, RCURBRACE))
			return left

		left = OperatorExpressionAction(semantics, ",", type = ElementType.OperatorExpression).parse {
			setIndices(left)
			addArgument(left)
			addArgument(OwnedExpression())
		}
	}

	return left
}

/** WITH parens, mutates surrounding element */
private fun InvocationExpressionAction.ArgumentList() : Unit = with(context.compiler) {
	LBRACE.consume()

	var isNamed : Boolean? = null
	var wantComma = false

	while(true) when {
		consumeIfTokenIs(COMMA) -> when {
			wantComma -> wantComma = false
			else -> throw SyntaxError(this, "Too many commas")
		}
		consumeIfTokenIs(RBRACE) -> return // standard forbids trailing comma here...
		token.kind == NAME_LIT && nextToken.kind == EQ -> { // NamedArgument
			when {
				isNamed == false -> throw SyntaxError(this, "Named- and positional arguments may not be mixed")
				wantComma -> throw SyntaxError(this, "Missing comma")
			}

			isNamed = true
			wantComma = true

			semantics.addOwnedElement( // NamedArgumentMember
				FeatureAction(semantics, owningMembershipType = null).parse { // NamedArgument
					semantics.addRedefinition(token.string) // ParameterRedefinition
					NAME_LIT.consume()
					EQ.consume()
					semantics.addOwnedElement( // ArgumentValue
						OwnedExpression(), // OwnedExpression
						ElementType.FeatureValue
					)
				},
				ElementType.FeatureMembership // NOT parameterMembership
			).first?.also {
				it.parameterIndex = -1 // indicate reordering is required
			}
		}
		else -> { // positional argument
			when {
				isNamed == true -> throw SyntaxError(this, "Named- and positional arguments may not be mixed")
				wantComma -> throw SyntaxError(this, "Missing comma")
			}
			isNamed = false
			wantComma = true

			addArgument(OwnedExpression())
		}
	}
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

private fun KerML.FunctionReferenceArgument() = FeatureAction(semantics, owningMembershipType = null).parse {
	// FunctionReferenceArgumentValue
	semantics.addOwnedElement(ExpressionAction(semantics, ElementType.FeatureReferenceExpression).parse {
		// FunctionReferenceExpression
		// FunctionReferenceMember
		semantics.addOwnedElement(ExpressionAction(semantics, ElementType.Expression).parse {
			// FunctionReference
			// ReferenceTyping
			semantics.addTyping(QualifiedName())
		}, ElementType.FeatureMembership)
	}, ElementType.FeatureValue)
}

private fun KerML.BaseExpression() : ElementData
{
	lateinit var expr : ElementData

	alternatives {
		NULL starts { /* Type already set */
			expr = ExpressionAction(semantics, ElementType.NullExpression).parse {
				NULL.consume()
			}
		}
		NAME_LIT starts { // FeatureReferenceExpression | InvocationExpression
			expr = InvocationExpressionAction(semantics).parse {
				val f = QualifiedName()

				alternatives {
					LBRACE starts {
						// FIXME: Standard uses "InstatiatedTypeMember" here which lacks a definition
						function = f
						withUnresolvedNames(function in specialLegacyFunctions) {
							ArgumentList()
						}
					}
					(DOT then METADATA) starts {
						DOT.consume()
						METADATA.consume()
						type = ElementType.MetadataAccessExpression
						addElementReference(f)
					}
					others {
						type = ElementType.FeatureReferenceExpression
						makeFeatureReference(f)
					}
				}
			}
		}
		// TODO: ConstructorExpression, missing "new" keyword
		LCURBRACE starts { // BodyExpression
			expr = BodyExpression()
		}

		TRUE then {
			expr = ExpressionAction(semantics, ElementType.LiteralBoolean).parse {
				setIndices(consumedToken)
				element.literalBooleanValue = true
			}
		}
		FALSE then {
			expr = ExpressionAction(semantics, ElementType.LiteralBoolean).parse {
				setIndices(consumedToken)
				element.literalBooleanValue = false
			}
		}

		// SysML extension: bracketed ranges
		LCBRACE starts {
			// fixme: LiteralRange type?
			expr = OperatorExpressionAction(semantics, "..").parse {
				// we could also just parse an arbitrary expression here...
				fun bound() = ExpressionAction(semantics, ElementType.LiteralRational).parse {
					var sign = 1
					alternatives {
						PLUS then {}
						MINUS then { sign = -1 }
						others { }
					}

					alternatives {
						INTEGER_LIT then {
							type = ElementType.LiteralInteger
							element.literalIntegerValue = consumedToken.number.toLong() * sign // really? manual conversion?
							element.literalRationalValue = consumedToken.number * sign // as fallback
						}
						FLOAT_LIT then {
							element.literalRationalValue = consumedToken.number * sign
						}
						TIMES then {
							type = ElementType.LiteralInfinity
						}
					}
				}

				consume(LCBRACE)
				val low = bound()
				consume(DOTDOT)
				val high = bound()
				consume(RCBRACE)

				val int = ElementType.LiteralInteger
				val float = ElementType.LiteralRational

				// perform a non-standard implicit cast so that strict type equality holds for SysMD's range implementation
				when {
					low.type == int && high.type != int -> low.type = float
					low.type != int && high.type == int -> high.type = float
				}

				addArgument(low)
				addArgument(high)
			}
		}

		FLOAT_LIT starts {
			expr = ExpressionAction(semantics, ElementType.LiteralRational).parse {
				FLOAT_LIT.consume()
				element.literalRationalValue = consumedToken.number
			}
		}

		INTEGER_LIT starts {
			expr = ExpressionAction(semantics, ElementType.LiteralInteger).parse {
				INTEGER_LIT.consume()
				element.literalIntegerValue = consumedToken.number.toLong()
			}
		}

		STRING_LIT starts {
			expr = ExpressionAction(semantics, ElementType.LiteralString).parse {
				STRING_LIT.consume()
				element.literalStringValue = consumedToken.string
			}
		}

		TIMES starts {
			expr = ExpressionAction(semantics, ElementType.LiteralInfinity).parse {
				TIMES.consume()
			}
		}

		IF starts {
			expr = ConditionalExpression()
		}

		// hack to implement the percentage unit
		PERCENT then {
			expr = FeatureReferenceExpression("%")
		}
	}

	return expr
}

/** A function body enclosed in {}
	```
		BodyExpression : FeatureReferenceExpression = ownedRelationship += ExpressionBodyMember
		ExpressionBodyMember : FeatureMembership = ownedMemberFeature = ExpressionBody
		ExpressionBody : Expression = '{' FunctionBodyPart '}'
	```
 */
private fun KerML.BodyExpression() : ElementData = ExpressionAction(semantics, ElementType.FeatureReferenceExpression).parse {
	// acts like consume() without consuming
	if(! tokenIs(LCURBRACE))
		throw LexicalError(this@BodyExpression, "after '$consumedToken': expected '{' but read '$token' ")

	semantics.addOwnedElement( // ExpressionBodyMember
		ExpressionAction(semantics, ElementType.Expression).parse { // ExpressionBody
			FunctionBody() // attaches elements directly
		},
		ElementType.FeatureMembership
	)
}

private fun KerML.TypeReference() = FeatureAction(semantics, owningMembershipType = null).parse {
	val typeName = QualifiedName()
	semantics.addOwnedRelationship( // ReferenceTyping
		IdentifiedByName(typeName, IdentificationKind.Type),
		ElementType.FeatureTyping
	)
}

private fun KerML.FeatureReferenceExpression(name : QualifiedName) = ExpressionAction(
	semantics,
	ElementType.FeatureReferenceExpression
).parse {
	setIndices(consumedToken)
	makeFeatureReference(name)
}

private fun ExpressionAction.makeFeatureReference(name : String) {
	if(context.compiler.unresolvedNamesMode) {
		type = ElementType.LiteralString
		element.isNameLiteral = true
		element.literalStringValue = name
		element.functionName = name

		return
	}

	context.compiler.semantics.addOwnedRelationship( // FeatureReferenceMember
		IdentifiedByName(name, IdentificationKind.Feature),
		ElementType.Membership // really?
	)
}

private fun ExpressionAction.addElementReference(name : QualifiedName) {
	context.compiler.semantics.addOwnedRelationship(
		IdentifiedByName(name, IdentificationKind.Element),
		ElementType.Membership // really?
	)
}