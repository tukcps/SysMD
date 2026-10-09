package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import io.github.tukcps.aadd.DDBuilder.BoolMath.not
import io.github.tukcps.aadd.DDBuilder.IntMath.log
import io.github.tukcps.aadd.DDBuilder.IntMath.root
import io.github.tukcps.aadd.DDBuilder.RealMath.log
import io.github.tukcps.aadd.DDBuilder.RealMath.root
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongMath.max
import io.github.tukcps.aadd.values.bounds.LongMath.min
import kotlin.math.max
import kotlin.math.min

/** @param arity Legal number of parameters for this function */
abstract class BuiltinFunction(val arity : IntRange)
{
	init {
		require(! arity.isEmpty())
	}

	/** Evaluates the operator on two quantities. Corresponds to upwards evaluation. */
	abstract fun evalUp(args : List<VectorQuantity>) : VectorQuantity

	abstract fun evalDown(result : VectorQuantity, args : List<VectorQuantity>) : List<VectorQuantity>
}

private abstract class UnaryFunction : BuiltinFunction(1 .. 1)
{
	abstract fun evalDown(result : VectorQuantity, argument : VectorQuantity) : VectorQuantity
	abstract fun evalUp(argument : VectorQuantity) : VectorQuantity

	override fun evalDown(result : VectorQuantity, args : List<VectorQuantity>) : List<VectorQuantity>
	= listOf( evalDown(result, args.single()) )

	override fun evalUp(args : List<VectorQuantity>) : VectorQuantity
	= evalUp(args.single())
}

/** selects between two functions based on arity */
private class VariantFunction(val f : BuiltinFunction, val g : BuiltinFunction)
	: BuiltinFunction(min(f.arity.first, g.arity.first) .. max(f.arity.last, g.arity.last))
{
	init {
		// arities are non-overlapping and immediately after another
		require(min(f.arity.last, g.arity.last) + 1 == max(f.arity.first, g.arity.first))
	}

	override fun evalUp(args : List<VectorQuantity>) : VectorQuantity = when(args.size) {
		in f.arity -> f.evalUp(args)
		in g.arity -> g.evalUp(args)
		else -> throw IllegalArgumentException("Arity mismatch on builtin function")
	}

	override fun evalDown(result : VectorQuantity, args : List<VectorQuantity>) : List<VectorQuantity>
	= when(args.size) {
		in f.arity -> f.evalDown(result, args)
		in g.arity -> g.evalDown(result, args)
		else -> throw IllegalArgumentException("Arity mismatch on builtin function")
	}
}

private abstract class BinaryFunction : BuiltinFunction(2..2)
{
	override fun evalDown(result : VectorQuantity, args : List<VectorQuantity>) : List<VectorQuantity>
	{
		val (l,r) = args
		return evalDown(result, l, r).toList()
	}

	abstract fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) : Pair<VectorQuantity, VectorQuantity>

	override fun evalUp(args : List<VectorQuantity>) : VectorQuantity
	{
		val (l,r) = args
		return evalUp(l, r)
	}

	/** Evaluates the operator on two quantities. Corresponds to upwards evaluation. */
	abstract fun evalUp(l : VectorQuantity, r : VectorQuantity) : VectorQuantity
}

/** Shorthand for defining a function with flipped operands, e.g. '<=' from '=>' */
private class FlippedOperator(private val flipped : BinaryFunction) : BinaryFunction()
{
	override fun evalUp(l : VectorQuantity, r : VectorQuantity) = flipped.evalUp(r, l)

	override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity)
			= flipped.evalDown(result, right, left).let { (r,l) -> l to r }

}

private abstract class MemberwiseOperator : BinaryFunction()
{
	/** evalDown  */
	abstract fun evalDown(result : DD<*>, left : DD<*>, right : DD<*>) : Pair<DD<*>, DD<*>>

	final override fun evalDown(
		result : VectorQuantity,
		left : VectorQuantity,
		right : VectorQuantity
	) : Pair<VectorQuantity, VectorQuantity>
			= pairedQuantities(result.values.mapIndexed { ix, v ->
		evalDown(v, left.values[ix], right.values[ix])
	})

}

private fun pairedQuantities(fields : List<Pair<DD<*>, DD<*>>>) : Pair<VectorQuantity, VectorQuantity>
		= fields.unzip().let { (ls,rs) -> VectorQuantity(ls) to VectorQuantity(rs) }

/** Shorthand for commutative operators where downwards eval happens independently */
private abstract class AbelianOperator : BinaryFunction()
{
	/** Downward-evaluates the operator
	 * @param result The current down quantity of the result
	 * @param operand The previous up quantity of the OTHER operand
	 * @return The new up quantity for the operand that DOESN'T correspond to the `operand` argument
	 */
	abstract fun evalDown(result : VectorQuantity, operand : VectorQuantity) : VectorQuantity

	final override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity
	) : Pair<VectorQuantity, VectorQuantity>
			= Pair(evalDown(result, right), evalDown(result, left))
}

private fun<T> List<T>.put(ix : Int, y : T)
= if(ix !in this@put.indices) throw IndexOutOfBoundsException(ix)
	else mapIndexed { j, x -> if(ix == j) y else x }

/** Predefined primitive functions that cannot be expressed in SysML or KerML syntax.
 * When set as the `builtin` field of an `InstantiationExpression`, these are used for propagation semantics
 * instead of `result` or `function`
 * */
enum class BuiltinFunctions(val f : BuiltinFunction)
{
	NOT(object : UnaryFunction()
	{
		override fun evalDown(
			result : VectorQuantity,
			argument : VectorQuantity
		) : VectorQuantity
		= VectorQuantity(result.values.map { !(it as BDD) })

		override fun evalUp(argument : VectorQuantity) : VectorQuantity
		= VectorQuantity(argument.values.map { !(it as BDD) })
	}),

	AND(object : AbelianOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l and r

		override fun evalDown(result : VectorQuantity, operand : VectorQuantity) = result and operand
	}),

	OR(object : AbelianOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l or r

		// 0 1 -> 1 resp. 0, 1 - 0
		// 1 0 -> 1 resp. 0, 0,- 0
		// 1 1 -> 1 resp. 1, 1, X
		// 0 0 -> 0 resp. 1, 0, 1
		override fun evalDown(result : VectorQuantity, operand : VectorQuantity) = VectorQuantity(
			result.values.mapIndexed { ix, v ->
				val b = v.builder
				val o = operand.values[ix].asBdd()
				v.asBdd().ite(o.ite(b.Bool.All, b.Bool.False), o.ite(b.Bool.False, b.Bool.True))
			}
		)
	}),

	PLUS_1(object : UnaryFunction()
	{
		override fun evalDown(
			result : VectorQuantity,
			argument : VectorQuantity
		) : VectorQuantity
			= result

		override fun evalUp(argument : VectorQuantity) : VectorQuantity
			= argument
	}),

	PLUS_2(object : AbelianOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l + r
		override fun evalDown(result : VectorQuantity, operand : VectorQuantity) = result - operand
	}),

	PLUS(VariantFunction(PLUS_1.f, PLUS_2.f)),

	MINUS_1(object : UnaryFunction()
	{
		override fun evalDown(result : VectorQuantity, argument : VectorQuantity) : VectorQuantity = result.negate()

		override fun evalUp(argument : VectorQuantity) : VectorQuantity = argument.negate()
	}),

	MINUS_2(object : BinaryFunction()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l - r

		override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) = Pair(
			result + right, left - result
		)
	}),

	MINUS(VariantFunction(MINUS_1.f, MINUS_2.f)),

	TIMES(object : AbelianOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l * r
		override fun evalDown(result : VectorQuantity, operand : VectorQuantity) = result / operand
	}),

	DIV(object : BinaryFunction()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l / r
		override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) = Pair(
			result * right, left / result
		)
	}),
	EXP(object : BinaryFunction()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l pow r
		override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) = Pair(
			when(left.values[0])
			{
				is AADD -> VectorQuantity.fromCanonical(
					result.values.mapIndexed { ix, v -> root(v.asAadd(), right.values[ix] as AADD) },
					result.unit,
					result.unitSpec,
					result.userWantedUnitSpec
				)

				is IDD -> VectorQuantity(
					result.values.mapIndexed { ix, v -> root(v.asIdd(), (right.values[ix] as IDD)) }
				)

				else -> throw SemanticError("Expect base of type Real or Integer")
			},
			when(left.values[0])
			{
				is AADD -> VectorQuantity(
					result.values.mapIndexed { ix, v ->
						val lv = left.values[ix] as AADD
						//Log with 1 not possible. All resulting values allowed
						if(lv.min.toDouble() == 1.0 && lv.max.toDouble() == 1.0)
							lv.builder.Reals.All
						else
							log(v as AADD,  lv)
					}
				)

				is IDD -> VectorQuantity(
					result.values.mapIndexed { ix, v ->
						val lv = left.values[ix] as IDD
						//Log with 1 not possible. All resulting values allowed
						if(lv.min == LongBound.Finite(1L) && lv.max == LongBound.Finite(1L))
							lv.builder.Integers.All
						else
							log(v as IDD, lv)
					}
				)

				else -> throw SemanticError("Expect base of type Real or Integer")
			}
		)
	}),

	// FIXME: Standard says == as arity 0..2 ??
	EE(object : BinaryFunction()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l eq r
		override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) =
			when(left.values[0]) {
				is AADD ->
				{
					val rr = right.values.mapIndexed { ix, v ->
						result.values[ix].asBdd().ite(
							v as AADD,
							v.builder.Reals.All
						)
					}
					val lr = left.values.mapIndexed { ix, v ->
						result.values[ix].asBdd().ite(
							v as AADD,
							v.builder.Reals.All
						)
					}
					Pair(VectorQuantity.fromCanonical(rr, left.unit, left.unitSpec, left.userWantedUnitSpec),
						VectorQuantity.fromCanonical(lr, right.unit, right.unitSpec, right.userWantedUnitSpec))
				}
				is IDD -> {
					val rs = right.values.mapIndexed { ix, r ->
						val down = result.values[ix].asBdd()
						if(down.value in setOf(XBool.True, XBool.All))
						// intersect
							left.values[ix].asIdd() intersect r.asIdd() // FIXME: clone?
						else
							r.builder.Integers.All
					}
					Pair(VectorQuantity(rs), VectorQuantity(rs))
				}

				is BDD -> Pair(left, right) // noop
				is StrDD -> Pair(
					result.value.asBdd().ite(right, VectorQuantity(right.value.builder.Strings.All)),
					result.value.asBdd().ite(left, VectorQuantity(left.value.builder.Strings.All)),
				)
			}
	}),

	// FIXME: stdlib defines != via ==
	NEQ(object : BinaryFunction()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l neq r

		// noop
		override fun evalDown(result : VectorQuantity, left : VectorQuantity, right : VectorQuantity) = Pair(
			left, right
		)
	}),

	GT(object : MemberwiseOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l gt r

		override fun evalDown(result : DD<*>, left : DD<*>, right : DD<*>) = when {
			left is AADD && right is AADD -> Pair(
				result.asBdd().ite(
					(left greaterThan right).ite(left, right),
					(left lessThanOrEquals right).ite(left, right)
				) as DD<*>,
				result.asBdd().ite(
					(left lessThanOrEquals right).ite(left, right),
					(left greaterThan right).ite(left, right)
				) as DD<*>
			)
			left is IDD && right is IDD -> {
				val b = result.builder
				val (minL, maxL) = left.getRange().run { min to max }
				val (minR, maxR) = right.getRange().run { min to max }

				Pair(
					result.asBdd().ite(
						if (minR < maxL) b.integer(max(minL, minR + 1)..maxL)
						else b.Integers.Empty,
						if (minL < maxR) b.integer( minL..min(maxL, maxR - 1))
						else b.Integers.Empty
					) as DD<*>,
					result.asBdd().ite(
						if (minR < maxL) b.integer(minR..min(maxL-1L, maxR))
						else b.Integers.Empty,
						if (minL < maxR) b.integer(max(minL+1L, minR)..maxR)
						else b.Integers.Empty
					) as DD<*>
				)
			}
			(left is AADD || left is IDD) && (right is AADD || right is IDD) -> throw SemanticError(
				"Comparison between different types"
			)
			else -> throw SemanticError("Comparison only defined between Integers and Reals.")
		}
	}),

	GE(object : MemberwiseOperator()
	{
		override fun evalUp(l : VectorQuantity, r : VectorQuantity) = l ge r

		override fun evalDown(result : DD<*>, left : DD<*>, right : DD<*>) = when {
			left is AADD && right is AADD -> Pair(
				result.asBdd().ite(
					(left greaterThanOrEquals right).ite(left, right),
					(left lessThan right).ite(left, right)
				),
				result.asBdd().ite(
					(left lessThan right).ite(left, right),
					(left greaterThanOrEquals right).ite(left, right)
				)
			)
			left is IDD && right is IDD -> {
				val b = result.builder
				val (minL, maxL) = left.getRange().run { min to max }
				val (minR, maxR) = right.getRange().run { min to max }

				Pair(
					result.asBdd().ite(
						if (minR <= maxL) b.integer(max(minL, minR)..maxL)
						else b.Integers.Empty,
						if (minL < maxR) b.integer( minL..min(maxL, maxR))
						else b.Integers.Empty
					) as DD<*>,
					result.asBdd().ite(
						if (minR < maxL) b.integer(minR..min(maxL, maxR))
						else b.Integers.Empty,
						if (minL < maxR) b.integer(max(minL, minR)..maxR)
						else b.Integers.Empty
					) as DD<*>
				)
			}
			(left is AADD || left is IDD) && (right is AADD || right is IDD) -> throw SemanticError(
				"Comparison between different types"
			)
			else -> throw SemanticError("Comparison only defined between Integers and Reals.")
		}

	}),

	LT(FlippedOperator(GT.f as BinaryFunction)),
	LE(FlippedOperator(GE.f as BinaryFunction)),

	ITE(object : BuiltinFunction(3..3)
	{
		override fun evalUp(args : List<VectorQuantity>) : VectorQuantity
		{
			val (c,t,e) = args

			return c.bdd().ite(t, e)
		}

		override fun evalDown(result : VectorQuantity, args : List<VectorQuantity>) : List<VectorQuantity>
		= when (result) {
            args[1] if result != args[2] -> args.put(0, VectorQuantity(result.value.builder.Bool.True))
            args[2] if result != args[1] -> args.put(0, VectorQuantity(result.value.builder.Bool.False))
            // As we work on BDD for the digital side that are generated by evalUp, evalDown does nothing.
            else -> args
        }
	})
}