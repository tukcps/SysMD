package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.util.SimpleName

/** Syntactic information on an operator
 * @param namespace The standard library in which the operator's definition is found
 * */
sealed class OperatorInformation(val namespace : SimpleName)
{
	companion object {
		operator fun get(symbol : String?) : OperatorInformation?
		= TernaryOperatorInformation[symbol] ?: BinaryOperatorInformation[symbol]
		?: UnaryOperatorInformation[symbol] ?: CallLikeOperatorInformation[symbol]
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
 * @param rightAssociative If true, the operator associates to the right rather than left
 * @param typeOperand If true, the second argument to this operator is a type name
 * @param special If true, this operator requires special parsing logic and should NOT be covered by the BinaryOperator production rule
 * @param leftSpace If true, a space is printed to the left of this operator
 * @param rightSpace If true, a space is printed to the right of this operator
 */
class BinaryOperatorInformation private constructor(
	val precedence : Int, namespace : SimpleName = "DataFunctions",
	val abelian : Boolean = false, val rightAssociative : Boolean = false, val typeOperand : Boolean = false,
	val special : Boolean = false,
	val leftSpace : Boolean = true,
	val rightSpace : Boolean = true,
) : OperatorInformation(namespace) {
	companion object
	{
		val bySymbol = mapOf(
			"." to BinaryOperatorInformation(999, "ControlFunctions", leftSpace = false, rightSpace = false, special = true),

			"^" to BinaryOperatorInformation(110, rightAssociative = true, leftSpace = false, rightSpace = false),
			"**" to BinaryOperatorInformation(110, rightAssociative = true, leftSpace = false, rightSpace = false),

			/* non-standard SysMD extension, only for legacy compatability */
			" " to BinaryOperatorInformation(105, "SysMD", leftSpace = false, rightSpace = false),

			"*" to BinaryOperatorInformation(100, abelian = true),
			"/" to BinaryOperatorInformation(100),
			"%" to BinaryOperatorInformation(100),
			"cross" to BinaryOperatorInformation(100),
			"dot" to BinaryOperatorInformation(100),

			"+" to BinaryOperatorInformation(90, abelian = true),
			"-" to BinaryOperatorInformation(90),

			".." to BinaryOperatorInformation(80),

			">" to BinaryOperatorInformation(70),
			"<" to BinaryOperatorInformation(70),
			"<=" to BinaryOperatorInformation(70),
			">=" to BinaryOperatorInformation(70),

			"istype" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),
			"hastype" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),
			"@" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),
			"@@" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),
			"as" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),
			"meta" to BinaryOperatorInformation(60, namespace = "BaseFunctions", typeOperand = true),

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

			"??" to BinaryOperatorInformation(0, namespace = "ControlFunctions"),

			"," to BinaryOperatorInformation(-10, abelian = true, namespace = "BaseFunctions", special = true, leftSpace = false),
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}

/**
 * @param typeOperand If true, this operator takes a type name instead of a normal expression
 * */
class UnaryOperatorInformation private constructor(
	namespace : SimpleName = "DataFunctions", val typeOperand : Boolean = false
) : OperatorInformation(namespace)
{
	companion object
	{
		val bySymbol = mapOf(
			"+" to UnaryOperatorInformation(),
			"-" to UnaryOperatorInformation(),
			"not" to UnaryOperatorInformation(),
			"~" to UnaryOperatorInformation(),
			"all" to UnaryOperatorInformation("BaseFunctions", typeOperand = true),

			// ref. 7.4.9.2
			// these are shorthands for binary operators that use `Anything::self` as first operand
			"istype" to UnaryOperatorInformation("BaseFunctions", typeOperand = true),
			"@" to UnaryOperatorInformation("BaseFunctions", typeOperand = true),
			"hastype" to UnaryOperatorInformation("BaseFunctions", typeOperand = true),
			"as" to UnaryOperatorInformation("BaseFunctions", typeOperand = true),
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}

/** Operators that are invokes with suffixed parentheses, like function calls  */
class CallLikeOperatorInformation private constructor(
	val left : String, val right : String,
	namespace : SimpleName = "BaseFunctions"
) : OperatorInformation(namespace)
{
	companion object
	{
		val bySymbol = mapOf(
			"#" to CallLikeOperatorInformation("#(", ")"),
			"[" to CallLikeOperatorInformation("[", "]")
		)

		operator fun get(symbol : String?) = bySymbol[symbol]
	}
}
