package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.util.SimpleName

/** Syntactic information on an operator
 * @param namespace The standard library in which the operator's definition is found
 * */
sealed class OperatorInformation(val namespace : SimpleName)
{
	companion object {
		operator fun get(symbol : String?) : OperatorInformation?
		= TernaryOperatorInformation[symbol] ?: BinaryOperatorInformation[symbol] ?: UnaryOperatorInformation[symbol]
	}
}

/** Syntactic information for ternary operators (KerML only has if ? else)
 * @param prefix Component that appears left of first operand
 * @param leftInfix Component that appears between first and second operands
 * @param rightInfix Component that appears between second and third operands
 * */
class TernaryOperatorInformation private constructor(
	val prefix : String, val leftInfix : String, val rightInfix : String,
	namespace : SimpleName
) : OperatorInformation(namespace)
{
	companion object {
		val bySymbol = mapOf(
			"if" to TernaryOperatorInformation("if ", " ? ", " else ", "ControlFunctions")
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}

/** Syntactic information for binary operators
 * @param precedence Operator precedence. Lower = binds weaker, higher = binds stronger (ref. 8.2.5.8.1)
 * @param abelian Whether the operator can be chained without parenthesis
 */
class BinaryOperatorInformation private constructor(
	val precedence : Int, namespace : SimpleName = "DataFunctions", val abelian : Boolean = false
) : OperatorInformation(namespace) {
	companion object
	{
		val bySymbol = mapOf(
			"^" to BinaryOperatorInformation(110),
			"**" to BinaryOperatorInformation(110),

			"*" to BinaryOperatorInformation(100, abelian = true),
			"/" to BinaryOperatorInformation(100),
			"%" to BinaryOperatorInformation(100),

			"+" to BinaryOperatorInformation(90, abelian = true),
			"-" to BinaryOperatorInformation(90),

			".." to BinaryOperatorInformation(80),

			">" to BinaryOperatorInformation(70),
			"<" to BinaryOperatorInformation(70),
			"<=" to BinaryOperatorInformation(70),
			">=" to BinaryOperatorInformation(70),

			"istype" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),
			"hastype" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),
			"@" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),
			"@@" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),
			"as" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),
			"meta" to BinaryOperatorInformation(60, namespace = "BaseFunctions"),

			"==" to BinaryOperatorInformation(50, namespace = "BaseFunctions"),
			"!=" to BinaryOperatorInformation(50, namespace = "BaseFunctions"),
			"===" to BinaryOperatorInformation(50, namespace = "BaseFunctions"),
			"!==" to BinaryOperatorInformation(50, namespace = "BaseFunctions"),

			"&" to BinaryOperatorInformation(40, abelian = true),
			"and" to BinaryOperatorInformation(40, abelian = true, namespace = "ControlFunctions"),

			"xor" to BinaryOperatorInformation(30),

			"|" to BinaryOperatorInformation(20, abelian = true),
			"or" to BinaryOperatorInformation(20, abelian = true, namespace = "ControlFunctions"),

			"implies" to BinaryOperatorInformation(10, namespace = "ControlFunctions"),

			"??" to BinaryOperatorInformation(0, namespace = "ControlFunctions")
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}

class UnaryOperatorInformation(namespace : SimpleName = "DataFunctions") : OperatorInformation(namespace)
{
	companion object
	{
		val bySymbol = mapOf(
			"+" to UnaryOperatorInformation(),
			"-" to UnaryOperatorInformation(),
			"not" to UnaryOperatorInformation(),
			"~" to UnaryOperatorInformation(),
			"all" to UnaryOperatorInformation("BaseFunctions"),
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}
