@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.kerml.legacy

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.kerml.parseIntegerRange
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConditionalExpressionActions
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.functions.AstHasType
import com.github.tukcps.sysmd.model.expression.functions.AstNot
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.unaryMinus

/**
 * parseExpression parses an expression and returns the AST as the result.
 * Expression :- Comparison
 * @return an AstNode with the abstract syntax tree
 */
@Deprecated("Will be replaced with KerML-expression compliant parser")
fun KerML.Expression(): AstNode
{
    return Comparison()
}

/**
 *      BooleanExpression =
 *          FeaturePrefix 'bool' FeatureDeclaration ValuePart? FunctionBody
 */
fun KerML.BooleanExpression(): AstNode
{
    TODO()
}


/**
 * conditionalExpression :- IF expression ? expression ELSE expression
 */
fun KerML.ConditionalExpression(): AstNode
{
    val action = ConditionalExpressionActions(semantics)
    Token.Kind.IF.consume()
    Expression().also { action.condExpr = it }
    Token.Kind.QUESTION.consume()
    Expression().also { action.thenExpr = it }
    Token.Kind.ELSE.consume()
    Expression().also { action.elseExpr = it; return action.run() }
}

/**
 * parseComparison computes an expression and returns the AST as the result.
 *
 * Expression :- Sum [ relOp Sum]
 */
fun KerML.Comparison(): AstNode
{
    var result = Sum()
    optional(Token.Kind.GT or Token.Kind.LT or Token.Kind.EQ or Token.Kind.GE or Token.Kind.LE or Token.Kind.EE or Token.Kind.NEQ) {
        consume()
        val op = consumedToken.kind
        val t2 = Sum()
        result = AstBinOp(result, op, t2)
    }
    return result
}

/** Sum :- Product ( ("+"|"-"|"|") Product )*  */
fun KerML.Sum(): AstNode
{
    var s1 = Product()
    while (consumeIfTokenIs(Token.Kind.PLUS, Token.Kind.MINUS, Token.Kind.OR)) {
        val op = consumedToken.kind
        val s2 = Product()
        s1 = AstBinOp(s1, op, s2)
    }
    return s1
}

/** Product :- Exponent ( ("*"|"/"|"&") Exponent )*     */
fun KerML.Product(): AstNode
{
    var f1 = Exponent()
    noOrMore (Token.Kind.TIMES or Token.Kind.DIV or Token.Kind.AND or Token.Kind.CROSS or Token.Kind.DOTProduct, consume = true) {
        val op = consumedToken.kind
        val f2 = Exponent()
        f1 = AstBinOp(f1, op, f2)
    }
    return f1
}

/**
 * Exponent :- UnaryOperatorExpression ( "^" Exponent )*
 * Note: right associative via recursion
 */
fun KerML.Exponent(): AstNode
{
    val exponent = UnaryOperatorExpression()
    if (tokenIs(Token.Kind.EXP)) {
        consume(Token.Kind.EXP)
        Exponent() .also {
            return semantics.handleFunctionCall(function = "power", param = arrayListOf(exponent, it), semantics = semantics)
        }
    }
    return exponent
}

/**
 * UnaryOperatorExpression :- ["+" Value | "-" Value | "not" Value | Value|
 */
fun KerML.UnaryOperatorExpression(): AstNode
{
    var result: AstNode? = null
    alternatives {
        Token.Kind.PLUS then { Value().also { result = it } }
        Token.Kind.MINUS then { Value().also { result = AstUnaryOp(Token.Kind.MINUS, it) }}
        Token.Kind.NOT then { Value().also { result = AstNot(model, arrayListOf(it)) }}
        others     { Value().also { result = it }}
    }.also { return result!!  }
}

/**
 *  Parameters :-
 *      [ "(" [ Expression ("," Expression)*] ")" ]
 */
fun KerML.Parameters(): ArrayList<AstNode>? =
    optional(Token.Kind.LBRACE, noMatch = null, consume = true) {
        val parameters = ArrayList<AstNode>()
        noOrMore ({token.kind != Token.Kind.RBRACE }) {
            Expression().also { parameters.add(it)}
            while (consumeIfTokenIs(Token.Kind.COMMA)) {
                Expression().also { parameters.add(it)}
            }
        }
        Token.Kind.RBRACE.consume()
        parameters
    }


/**
 *  Unit :-> "%" // Percent as a unit
 *          | ["1"] (NAME_LIT ["^" INTEGER_LIT])* ["/" (NAME_LIT [^INTEGER_LIT] )+]
 **/
fun KerML.Unit(): String {
    var unit = ""

    alternatives {
        Token.Kind.PERCENT then  { unit = "%" }
        others {
            optional(Token.Kind.INTEGER_LIT, consume = true) {
                if (consumedToken.integer != LongBound.Finite(1))
                    throw SyntaxError(this, "Unit must not start with number not equal to 1")

                unit = "1 "
            }

            noOrMore(Token.Kind.NAME_LIT) {
                consume().also { unit += consumedToken.toString() }
                optional(Token.Kind.EXP, consume = true) {
                    Token.Kind.INTEGER_LIT.consume()
                    unit += "^${consumedToken.integer.finiteValue}"
                }
                unit += " "
            }

            optional(Token.Kind.DIV, consume = true) {
                unit += "$consumedToken "
                while (token.kind == Token.Kind.NAME_LIT) {
                    consume().also { unit += consumedToken.toString() }
                    optional(Token.Kind.EXP, consume = true) {
                        Token.Kind.INTEGER_LIT.consume()
                        unit += "^${consumedToken.integer.finiteValue}"
                    }
                    unit += " "
                }
            }
        }
    }
    return unit.trim()
}

val CONST_VALUE_START = setOf(Token.Kind.FLOAT_LIT, Token.Kind.INTEGER_LIT, Token.Kind.MINUS, Token.Kind.PLUS, Token.Kind.TIMES)

/** union of possible value types */
data class ConstValue(val real : RealRange, val int : LongBound?)
{
    operator fun unaryMinus() = ConstValue(-real, int?.unaryMinus())
    fun negateIf(cond : Boolean) = if(cond) -this else this

    fun toVQ(b : DDBuilder) = int?.let { VectorQuantity(b.integer(it)) }
        ?: VectorQuantity(b.real(real), "?")
}

data class ConstRange(val real : RealRange, val int : IntegerRange?)
{
    override fun toString() = when {
        // avoid printing an integer value that overflowed
        int !== null && (int.isFinite() || real.isFinite()) -> when {
            // This preserved invalid ranged because the dozens of re-parses don't accept the empty set symbol
            int.isEmpty() -> "${int.min}..${int.max}"
            else -> int.toString()
        }
        else -> when {
            real.isEmpty() -> "${real.min}..${real.max}"
            else -> real.toString()
        }
    }
}

/** Parses a constant value, possibly with leading sign */
fun KerML.ConstValue() : ConstValue {
    var negate = false
    alternatives {
        Token.Kind.PLUS then {}
        Token.Kind.MINUS then { negate = true }
        others {}
    }

    lateinit var result : ConstValue

    alternatives {
        Token.Kind.FLOAT_LIT then {
            result = ConstValue(
                consumedToken.real,
                null
            ).negateIf(negate)
        }
        Token.Kind.INTEGER_LIT then {
            result = ConstValue(
                consumedToken.real,
                consumedToken.integer
            ).negateIf(negate)
        }
        Token.Kind.NAME_LIT starts {
            throw SyntaxError(this@ConstValue, "Unsupported Syntax; only number literals supported")
        }
        Token.Kind.TIMES then {
            result = ConstValue(
                RealRange(if(negate) DoubleBound.NegativeInfinity else DoubleBound.PositiveInfinity),
                if(negate) LongBound.NegativeInfinity else LongBound.PositiveInfinity
            )
        }
    }

    return result
}

fun KerML.ConstRange() : ConstRange
{
    val left = ConstValue()
    val right = if(consumeIfTokenIs(Token.Kind.DOTDOT)) ConstValue() else left

    return ConstRange(
        RealRange(left.real.min, right.real.max),
        if(left.int !== null && right.int !== null) IntegerRange(left.int, right.int) else null
    )
}

/** A number literal (Int or Float) as the closest representable doubles */
fun KerML.ConstReal(): RealRange = ConstValue().real

/** A number literal (Int) or a property with known value */
fun KerML.ConstInt(): LongBound = ConstValue().int
    ?: throw SyntaxError(this@ConstInt, "Unsupported Syntax; only integer literals supported")

/**
 * Value :-
 *    NUM_LIT
 * |  TRUE
 * |  FALSE
 * |  '[' ValueRange ']'
 * |  '(' ITE ("," ITE)* ')' ?????? FIX
 * |  QualifiedName Parameters
 */
fun KerML.Value(): AstNode
{
    var astNode: AstNode? = null            // Value or expression
    alternatives {
        Token.Kind.LCBRACE then {                   // Range of kind [number, number] unit
            val quantity: VectorQuantity
            var unit = ""
            parseValueRange().also { quantity = it }
            Token.Kind.RCBRACE.consume()

            optional (Token.Kind.LCBRACE or Token.Kind.NAME_LIT or Token.Kind.PERCENT) {
                alternatives {
                    Token.Kind.LCBRACE starts  {
                        Token.Kind.LCBRACE.consume()
                        Unit().also { unit = it }
                        Token.Kind.RCBRACE.consume()
                    }
                    others {
                        unit = token.string
                        Token.Kind.NAME_LIT.consume()
                    }
                }
            }

            astNode = when(quantity.values[0]){
                is AADD -> AstLeaf(model, VectorQuantity(quantity.values as List<AADD>, unit))
                is IDD -> AstLeaf(model, VectorQuantity(quantity.values))
                is StrDD -> AstLeaf(model, VectorQuantity(quantity.values))
                is BDD -> AstLeaf(model, VectorQuantity(quantity.values))
            }
        }

        CONST_VALUE_START starts { // number literal
            val r = ConstRange()
            var unit : String? = null

            alternatives {
                Token.Kind.LCBRACE then {
                    unit = Unit()
                    Token.Kind.RCBRACE.consume()
                }
                setOf(Token.Kind.NAME_LIT, Token.Kind.PERCENT) then {
                    unit = consumedToken.string
                }
                others {}
            }

            astNode = AstLeaf(model, when {
                r.int !== null && unit === null -> VectorQuantity(model.builder.integer(r.int))
                else -> VectorQuantity(model.builder.real(r.real), unit ?: "")
            })
        }

        Token.Kind.STRING_LIT then {            // A string literal
            astNode = AstLeaf(model, VectorQuantity((model.builder.string(consumedToken.string))))
        }

        Token.Kind.TRUE then {               // True literal
            astNode = AstLeaf(model, VectorQuantity(model.builder.Bool.True))
        }

        Token.Kind.FALSE then {              // False literal
            astNode = AstLeaf(model, VectorQuantity(model.builder.Bool.False))
        }

        // '(' Expression ( ',' Expression)* ')'
        Token.Kind.LBRACE then {
            var expression: AstNode
            val values = arrayListOf<AstNode>()
            do {  // Iterate through all vector elements
                expression = Expression()
                if(tokenIs(Token.Kind.RBRACE) && values.isEmpty() )
                    astNode = expression //no vector, only expression in braces
                else if(expression is AstLeaf || expression is AstUnaryOp) {
                    values.add(expression)
                }
            } while (consumeIfTokenIs(Token.Kind.COMMA))
            //Parse Unit
            Token.Kind.RBRACE.consume()
            var unit = ""
            optional (Token.Kind.LCBRACE or Token.Kind.NAME_LIT or Token.Kind.PERCENT) {
                if (tokenIs(Token.Kind.LCBRACE)) {
                    Token.Kind.LCBRACE.consume()
                    Unit().also { unit = it }
                    Token.Kind.RCBRACE.consume()
                } else {
                    Token.Kind.NAME_LIT.consume().also { unit=consumedToken.string }
                }
            }
            if(astNode == null)
                try {
                    val quantityValues = mutableListOf<DD<*>>()
                    values.forEach {
                        it.evalUp()
                        quantityValues.add(it.dd)
                    }
                    astNode =
                        AstLeaf(model, VectorQuantity(quantityValues, com.github.tukcps.sysmd.quantities.Unit(unit)))
                } catch (_: UninitializedPropertyAccessException) {
                    var resultingString = ""
                    values.forEach {
                        resultingString += (it as AstLeaf).qualifiedName
                        resultingString += ","
                    }
                    resultingString=resultingString.removeSuffix(",")
                    val resultingAST= values[0] as AstLeaf
                    resultingAST.qualifiedName = resultingString
                    astNode = resultingAST
                }
        }
        Token.Kind.NAME_LIT starts { // QualifiedName [ '(' Parameters ')' | '[' Integer ']' ]
            val name = QualifiedName()
            alternatives {
                Token.Kind.LBRACE starts {
                    Parameters().also {
                        astNode = semantics.handleFunctionCall(name, it!!, semantics)
                    }
                }
                Token.Kind.LCBRACE starts {
                    Token.Kind.LCBRACE.consume()
                    val position = parseIntegerRange()
                    val rangeQuantity = VectorQuantity(model.builder.integer(position))
                    astNode = semantics.handleFunctionCall(
                        function = "quantityOfVectorAtPosition",
                        param = arrayListOf(AstLeaf(semantics.namespace, name, model), AstLeaf(model, rangeQuantity)),
                        semantics = semantics
                    )
                    Token.Kind.RCBRACE.consume()
                }
                Token.Kind.ISTYPE starts {
                    Token.Kind.ISTYPE.consume()
                    QualifiedName().also { astNode = AstHasType(model, semantics.namespace, name, it) }
                }
                Token.Kind.HASTYPE starts {
                    Token.Kind.HASTYPE.consume()
                    QualifiedName().also { astNode = AstHasType(model, semantics.namespace, name, it) }
                }
                others {
                    astNode = AstLeaf(semantics.namespace, name, model)   // an identifier
                }
            }
        }
        Token.Kind.IF starts { ConditionalExpression().also { astNode = it  } }
    }
    return astNode!!
}
