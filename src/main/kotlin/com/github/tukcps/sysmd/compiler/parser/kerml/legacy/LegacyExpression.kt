package com.github.tukcps.sysmd.compiler.parser.kerml.legacy

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.kerml.parseIntegerRange
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConditionalExpressionActions
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.AstUnaryOp
import com.github.tukcps.sysmd.model.expression.functions.AstHasType
import com.github.tukcps.sysmd.model.expression.functions.AstNot
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.*

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
            return AstBinOp(exponent, Token.Kind.EXP, it)
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
                if (consumedToken.number.toInt() != 1)
                    throw SyntaxError(this, "Unit must not start with number not equal to 1")
                unit = "1 "
            }

            noOrMore(Token.Kind.NAME_LIT) {
                consume().also { unit += consumedToken.toString() }
                optional(Token.Kind.EXP, consume = true) {
                    Token.Kind.INTEGER_LIT.consume().also { unit += "^${consumedToken.number.toInt()}" }
                }
                unit += " "
            }

            optional(Token.Kind.DIV, consume = true) {
                unit += "$consumedToken "
                while (token.kind == Token.Kind.NAME_LIT) {
                    consume().also { unit += consumedToken.toString() }
                    optional(Token.Kind.EXP, consume = true) {
                        Token.Kind.INTEGER_LIT.consume().also { unit += "^${consumedToken.number.toInt()}" }
                    }
                    unit += " "
                }
            }
        }
    }
    return unit.trim()
}


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
            val quantity: Quantity
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

            astNode = when(quantity.value){
                is AADD -> AstLeaf(model, Quantity(quantity.value as AADD, unit))
                is IDD -> AstLeaf(model, Quantity(quantity.value as IDD))
                is StrDD -> AstLeaf(model, Quantity(quantity.value as StrDD))
                is BDD -> AstLeaf(model, Quantity(quantity.value as BDD))
                else -> throw SemanticError("Unsupported type for $quantity.")
            }
        }

        Token.Kind.FLOAT_LIT then {              // Floating point literal of kind number unit
            val value = consumedToken.number
            var upperBound: Double? = null
            var unit = ""
            optional(Token.Kind.DOTDOT, consume = true) {
                Token.Kind.FLOAT_LIT.consume().also { upperBound = consumedToken.number }
            }

            optional (Token.Kind.LCBRACE or Token.Kind.NAME_LIT or Token.Kind.PERCENT) {
                alternatives {
                    Token.Kind.LCBRACE then {
                        Unit().also { unit = it }
                        Token.Kind.RCBRACE.consume()
                    }
                    others {
                        unit = token.string
                        Token.Kind.NAME_LIT.consume()
                    }
                }
            }
            val ub = upperBound?:value
            astNode = AstLeaf(model, Quantity(model.builder.real(value..ub), unit))
        }

        Token.Kind.INTEGER_LIT then  {            // Integer literal
            val min = consumedToken.number.toLong()
            // optional: Extension to range by
            val max = optional(Token.Kind.DOTDOT, consume = true, noMatch = min) {
                when(token.kind) {
                    Token.Kind.INTEGER_LIT -> {
                        Token.Kind.INTEGER_LIT.consume()
                        consumedToken.number.toLong()
                    }
                    Token.Kind.TIMES -> {
                        Token.Kind.TIMES.consume()
                        Long.MAX_VALUE
                    }
                    else -> min
                }
            }!!
            astNode = AstLeaf(model, Quantity(model.builder.integer(min..max)))
        }

        Token.Kind.STRING_LIT then {            // A string literal
            astNode = AstLeaf(model, Quantity(StrDD.Leaf(model.builder, consumedToken.string)))
        }

        Token.Kind.TRUE then {               // True literal
            astNode = AstLeaf(model, Quantity(model.builder.True))
        }

        Token.Kind.FALSE then {              // False literal
            astNode = AstLeaf(model, Quantity(model.builder.False))
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
                    val position = parseIntegerRange().toLongRange()
                    val rangeQuantity = Quantity(model.builder.integer(position))
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

/** A number literal (Int or Float) */
fun KerML.ConstReal(): Double {
    var result = 0.0
    val neg = consumeIfTokenIs(Token.Kind.MINUS) // Sign of negative value.
    alternatives {
        (Token.Kind.INTEGER_LIT or Token.Kind.FLOAT_LIT) starts {           // Number literal
            val value = token.number
            consume()
            result = if (neg) -value else value
        }
        Token.Kind.NAME_LIT starts {
            throw SyntaxError(this@ConstReal, "Unsupported Syntax; only number literals supported")
        }
        Token.Kind.TIMES starts   {
            consume()
            result = if(neg) model.settings.minReal else model.settings.maxReal
        }
    }
    return result
}

/** A number literal (Int or Float) or a property with a known value */
fun KerML.Number(): String {
    val neg = consumeIfTokenIs(Token.Kind.MINUS) // Sign of negative value.
    var result = ""
    alternatives {
        (Token.Kind.INTEGER_LIT or Token.Kind.FLOAT_LIT) starts {           // Number literal
            val value = if (token.number.rem(1).equals(0.0))
                token.number.toLong().toString()  // No ".0" as in Double.toString ...
            else
                token.number.toString()
            consume()
            result=if (neg) "-$value" else value
        }
        Token.Kind.TIMES then  { result="*" }
    }
    return result
}

/** A number literal (Int) or a property with known value */
fun KerML.ConstInt(): Long {
    val neg = consumeIfTokenIs(Token.Kind.MINUS) // Sign of negative value.
    var result: Long = 0
    alternatives {
        Token.Kind.INTEGER_LIT starts {           // Number literal
            val value = token.number
            consume()
            result = if (neg) -value.toLong() else value.toLong()
        }
        Token.Kind.NAME_LIT starts {
            throw SyntaxError(this@ConstInt, "Unsupported Syntax; only number literals supported")
        }
        Token.Kind.TIMES starts {
            consume()
            result = if (neg) model.settings.minInt else model.settings.maxInt
        }
    }
    return result
}