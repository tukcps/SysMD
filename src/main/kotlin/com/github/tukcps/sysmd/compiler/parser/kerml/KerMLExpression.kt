@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
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
fun KerML.Expression(): AstNode {
    return Comparison()
}

/**
 *      BooleanExpression =
 *          FeaturePrefix 'bool' FeatureDeclaration ValuePart? FunctionBody
 */
fun KerML.BooleanExpression(): AstNode {
    TODO()
}


/**
 * conditionalExpression :- IF expression ? expression ELSE expression
 */
fun KerML.ConditionalExpression(): AstNode {
    val action = ConditionalExpressionActions(semantics)
    IF.consume()
    Expression().also { action.condExpr = it }
    QUESTION.consume()
    Expression().also { action.thenExpr = it }
    ELSE.consume()
    Expression().also { action.elseExpr = it; return action.run() }
}

/**
 * parseComparison computes an expression and returns the AST as the result.
 *
 * Expression :- Sum [ relOp Sum]
 */
fun KerML.Comparison(): AstNode {
    var result = Sum()
    optional(GT or LT or EQ or GE or LE or EE or NEQ) {
        consume()
        val op = consumedToken.kind
        val t2 = Sum()
        result = AstBinOp(result, op, t2)
    }
    return result
}

/** Sum :- Product ( ("+"|"-"|"|") Product )*  */
fun KerML.Sum(): AstNode {
    var s1 = Product()
    while (consumeIfTokenIs(PLUS, MINUS, OR)) {
        val op = consumedToken.kind
        val s2 = Product()
        s1 = AstBinOp(s1, op, s2)
    }
    return s1
}

/** Product :- Exponent ( ("*"|"/"|"&") Exponent )*     */
fun KerML.Product(): AstNode {
    var f1 = Exponent()
    noOrMore (TIMES or DIV or AND or CROSS or DOTProduct, consume = true) {
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
fun KerML.Exponent(): AstNode {
    val exponent = UnaryOperatorExpression()
    if (tokenIs(EXP)) {
        consume(EXP)
        Exponent() .also {
            return AstBinOp(exponent, EXP, it)
        }
    }
    return exponent
}

/**
 * UnaryOperatorExpression :- ["+" Value | "-" Value | "not" Value | Value|
 */
fun KerML.UnaryOperatorExpression(): AstNode {
    var result: AstNode? = null
    alternatives {
        PLUS  then { Value().also { result = it } }
        MINUS then { Value().also { result = AstUnaryOp(MINUS, it) }}
        NOT   then { Value().also { result = AstNot(model, arrayListOf(it)) }}
        others     { Value().also { result = it }}
    }.also { return result!!  }
}

/**
 *  Parameters :-
 *      [ "(" [ Expression ("," Expression)*] ")" ]
 */
fun KerML.Parameters(): ArrayList<AstNode>? =
    optional(LBRACE, noMatch = null, consume = true) {
        val parameters = ArrayList<AstNode>()
        noOrMore ({token.kind != RBRACE }) {
            Expression().also { parameters.add(it)}
            while (consumeIfTokenIs(COMMA)) {
                Expression().also { parameters.add(it)}
            }
        }
        RBRACE.consume()
        parameters
    }


/**
 *  Unit :-> "%" // Percent as a unit
 *          | ["1"] (NAME_LIT ["^" INTEGER_LIT])* ["/" (NAME_LIT [^INTEGER_LIT] )+]
 **/
fun KerML.Unit(): String {
    var unit = ""

    alternatives {
        PERCENT then  { unit = "%" }
        others {
            optional(INTEGER_LIT, consume = true) {
                if (consumedToken.number.toInt() != 1)
                    throw SyntaxError(this, "Unit must not start with number not equal to 1")
                unit = "1 "
            }

            noOrMore(NAME_LIT or EURO) {
                consume().also { unit += consumedToken.toString() }
                optional(EXP, consume = true) {
                    INTEGER_LIT.consume().also { unit += "^${consumedToken.number.toInt()}" }
                }
                unit += " "
            }

            optional(DIV, consume = true) {
                unit += "$consumedToken "
                while (token.kind == NAME_LIT) {
                    consume().also { unit += consumedToken.toString() }
                    optional(EXP, consume = true) {
                        INTEGER_LIT.consume().also { unit += "^${consumedToken.number.toInt()}" }
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
fun KerML.Value(): AstNode {
    var astNode: AstNode? = null            // Value or expression
    alternatives {
        LCBRACE then {                   // Range of kind [number, number] unit
            val quantity: Quantity
            var unit = ""
            parseValueRange().also { quantity = it }
            RCBRACE.consume()

            optional (LCBRACE or NAME_LIT or PERCENT) {
                alternatives {
                    LCBRACE starts  {
                        LCBRACE.consume()
                        Unit().also { unit = it }
                        RCBRACE.consume()
                    }
                    others {
                        unit = token.string
                        NAME_LIT.consume()
                    }
                }
            }

            astNode = when(quantity.value){
                is AADD ->  AstLeaf(model, Quantity(quantity.value as AADD, unit))
                is IDD ->  AstLeaf(model, Quantity(quantity.value as IDD))
                is StrDD ->  AstLeaf(model, Quantity(quantity.value as StrDD))
                is BDD ->  AstLeaf(model, Quantity(quantity.value as BDD))
                else -> throw SemanticError("Unsupported type for $quantity.")
            }
        }

        FLOAT_LIT then {              // Floating point literal of kind number unit
            val value = consumedToken.number
            var upperBound: Double? = null
            var unit = ""
            optional(DOTDOT, consume = true) {
                FLOAT_LIT.consume().also { upperBound = consumedToken.number }
            }

            optional (LCBRACE or NAME_LIT or PERCENT) {
                alternatives {
                    LCBRACE then {
                        Unit().also { unit = it }
                        RCBRACE.consume()
                    }
                    others {
                        unit = token.string
                        NAME_LIT.consume()
                    }
                }
            }
            val ub = upperBound?:value
            astNode = AstLeaf(model, Quantity(model.builder.real(value .. ub), unit))
        }

        INTEGER_LIT then  {            // Integer literal
            val min = consumedToken.number.toLong()
            // optional: Extension to range by
            val max = optional(DOTDOT, consume = true, noMatch = min) {
                INTEGER_LIT.consume()
                consumedToken.number.toLong()
            }!!
            astNode = AstLeaf(model, Quantity(model.builder.integer(min..max)))
        }

        STRING_LIT then {            // A string literal
            astNode = AstLeaf(model, Quantity(StrDD.Leaf(model.builder, consumedToken.string)))
        }

        TRUE then {               // True literal
            astNode = AstLeaf(model, Quantity(model.builder.True))
        }

        FALSE then {              // False literal
            astNode = AstLeaf(model, Quantity(model.builder.False))
        }

        // '(' Expression ( ',' Expression)* ')'
        LBRACE then {
            var expression: AstNode
            val values = arrayListOf<AstNode>()
            do {  // Iterate through all vector elements
                expression = Expression()
                if(tokenIs(RBRACE) && values.isEmpty() )
                    astNode = expression //no vector, only expression in braces
                else if(expression is AstLeaf || expression is AstUnaryOp) {
                    values.add(expression)
                }
            } while (consumeIfTokenIs(COMMA))
            //Parse Unit
            RBRACE.consume()
            var unit = ""
            optional (LCBRACE or NAME_LIT or PERCENT) {
                if (tokenIs(LCBRACE)) {
                    LCBRACE.consume()
                    Unit().also { unit = it }
                    RCBRACE.consume()
                } else {
                    NAME_LIT.consume().also { unit=consumedToken.string }
                }
            }
            if(astNode == null)
                try {
                    val quantityValues = mutableListOf<DD<*>>()
                    values.forEach {
                        it.evalUp()
                        quantityValues.add(it.dd)
                    }
                    astNode = AstLeaf(model, VectorQuantity(quantityValues, com.github.tukcps.sysmd.quantities.Unit(unit)))
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
        NAME_LIT starts { // QualifiedName [ '(' Parameters ')' | '[' Integer ']' ]
            var name = QualifiedName()
            alternatives {
                LBRACE starts {
                    Parameters().also {
                        astNode = semantics.handleFunctionCall(name, it!!, semantics)
                    }
                }
                LCBRACE starts {
                    LCBRACE.consume()
                    val position = parseIntegerRange()
                    val rangeQuantity = Quantity(model.builder.integer(position))
                    astNode = semantics.handleFunctionCall(
                        function = "quantityOfVectorAtPosition",
                        param = arrayListOf(AstLeaf(semantics.namespace, name, model), AstLeaf(model,rangeQuantity)),
                        semantics = semantics
                    )
                    RCBRACE.consume()
                }
                ISTYPE starts {
                    ISTYPE.consume()
                    QualifiedName().also { astNode = AstHasType(model, semantics.namespace, name, it) }
                }
                HASTYPE starts {
                    HASTYPE.consume()
                    QualifiedName().also { astNode = AstHasType(model, semantics.namespace, name, it) }
                }
                others {
                    astNode = AstLeaf(semantics.namespace, name, model)   // an identifier
                }
            }
        }
        IF starts { ConditionalExpression().also { astNode = it  } }
    }
    return astNode!!
}

/** A number literal (Int or Float) */
fun KerML.ConstReal(): Double {
    var result = 0.0
    val neg = consumeIfTokenIs(MINUS) // Sign of negative value.
    alternatives {
        (INTEGER_LIT or FLOAT_LIT) starts {           // Number literal
            val value = token.number
            consume()
            result = if (neg) -value else value
        }
        NAME_LIT starts {
            throw SyntaxError(this@ConstReal, "Unsupported Syntax; only number literals supported")
        }
        TIMES starts   {
            consume()
            result = if(neg) model.settings.minReal else model.settings.maxReal
        }
    }
    return result
}

/** A number literal (Int or Float) or a property with a known value */
fun KerML.Number(): String {
    val neg = consumeIfTokenIs(MINUS) // Sign of negative value.
    var result = ""
    alternatives {
        (INTEGER_LIT or FLOAT_LIT) starts {           // Number literal
            val value = if (token.number.rem(1).equals(0.0))
                token.number.toLong().toString()  // No ".0" as in Double.toString ...
            else
                token.number.toString()
            consume()
            result=if (neg) "-$value" else value
        }
        TIMES then  { result="*" }
    }
    return result
}

/** A number literal (Int) or a property with known value */
fun KerML.ConstInt(): Long {
    val neg = consumeIfTokenIs(MINUS) // Sign of negative value.
    var result: Long = 0
    alternatives {
        INTEGER_LIT starts {           // Number literal
            val value = token.number
            consume()
            result = if (neg) -value.toLong() else value.toLong()
        }
        NAME_LIT starts {
            throw SyntaxError(this@ConstInt, "Unsupported Syntax; only number literals supported")
        }
        TIMES starts {
            consume()
            result = if (neg) model.settings.minInt else model.settings.maxInt
        }
    }
    return result
}
