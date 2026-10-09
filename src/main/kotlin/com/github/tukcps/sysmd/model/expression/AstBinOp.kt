package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.contains
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.LongMath.max
import io.github.tukcps.aadd.values.bounds.LongMath.min
import io.github.tukcps.aadd.values.real.ia.RealRange


/**
 * @class AstBinOp
 * A binary operation.
 */
class AstBinOp(
    val l: AstNode,
    val op: Token.Kind,
    val r: AstNode
) : AstNode(l.model) {

    /** Initialization */
    init {
        l.parent = this
        r.parent = this
    }

    /** Initialization; starts from bottom-up */
    override fun initialize() {
        upQuantity = if (op in setOf(GE, LE, EE, GT, LT, AND, OR, NEQ)) VectorQuantity(mutableListOf(model.builder.Bool.All))
        else if (l.isReal) VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
        else if (l.isInt) VectorQuantity(mutableListOf(model.builder.Integers.All))
        else VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All), Unit("?"), "?")
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /** Root is next-higher statement of other kind or null if this is overall root */
    override var root: AstNode? = null
        set(value) {
            field = value; l.root = value; r.root = value
        }

    /** Computes operands from leaves upwards */
    override fun evalUpRec() {
        l.evalUpRec()
        r.evalUpRec()
        evalUp()
    }

    /** Computes one level upwards, from children to parent */
    override fun evalUp() {
        upQuantity = when (op) {
            PLUS -> l.upQuantity + r.upQuantity
            MINUS -> l.upQuantity - r.upQuantity
            TIMES -> l.upQuantity * r.upQuantity
            DIV -> l.upQuantity / r.upQuantity
            CROSS -> l.upQuantity cross r.upQuantity
            DOTProduct -> l.upQuantity dot r.upQuantity
            GT -> l.upQuantity gt r.upQuantity
            LT -> l.upQuantity lt r.upQuantity
            GE -> l.upQuantity ge r.upQuantity
            LE -> l.upQuantity le r.upQuantity
            AND -> l.upQuantity and r.upQuantity
            OR -> l.upQuantity or r.upQuantity
            EE  -> l.upQuantity eq r.upQuantity
            NEQ -> l.upQuantity neq r.upQuantity
            else -> throw SemanticError("Operation $op resp. $op not supported here.")
        }
    }


    /** Computes one level downwards, from parent to children */
    override fun evalDown() {
        val prevL = l
        val prevR = r

        when (op) {
            AND -> {
                // result true: both operands true. result false: l must be false if r is true, else unknown.
                val bool = model.builder.Bool
                val resultsL = mutableListOf<BDD>()
                val resultsR = mutableListOf<BDD>()
                downQuantity.values.indices.forEach {
                    val down = downQuantity.values[it].asBdd()
                    resultsL.add(down.ite(bool.True, r.upQuantity.values[it].asBdd().ite(bool.False, bool.All)))
                    resultsR.add(down.ite(bool.True, l.upQuantity.values[it].asBdd().ite(bool.False, bool.All)))
                }
                l.downQuantity = VectorQuantity(resultsL)
                r.downQuantity = VectorQuantity(resultsR)
            }

            OR -> {
                // result false: both operands false. result true: operand must be true if the other is false, else unknown.
                val bool = model.builder.Bool
                val resultsL = mutableListOf<BDD>()
                val resultsR = mutableListOf<BDD>()
                downQuantity.values.indices.forEach {
                    val down = downQuantity.values[it].asBdd()
                    resultsR.add(down.ite(l.upQuantity.values[it].asBdd().ite(bool.All, bool.True), bool.False))
                    resultsL.add(down.ite(r.upQuantity.values[it].asBdd().ite(bool.All, bool.True), bool.False))
                }
                r.downQuantity = VectorQuantity(resultsR)
                l.downQuantity = VectorQuantity(resultsL)
            }

            PLUS -> {
                l.downQuantity = downQuantity - prevR.upQuantity
                r.downQuantity = downQuantity - prevL.upQuantity
            }
            MINUS -> {
                l.downQuantity = downQuantity + prevR.upQuantity
                r.downQuantity = prevL.upQuantity - downQuantity
            }
            /* In the case of e.g. `0 = x * 0`, x becomes a don't care, but AADD division will yield an infeasible value */
            TIMES if !(downQuantity.values.all { 0 in it } && (prevL.upQuantity.values.any { it.isZero } || prevR.upQuantity.values.any { it.isZero })) -> {
                l.downQuantity = downQuantity / prevR.upQuantity
                r.downQuantity = downQuantity / prevL.upQuantity
            }
            DIV -> {
                l.downQuantity = downQuantity * prevR.upQuantity
                r.downQuantity = prevL.upQuantity / downQuantity
            }
            EE -> {
                when {
                    prevL.isReal && prevR.isReal -> {
                        val resultsR = mutableListOf<AADD>()
                        val resultsL = mutableListOf<AADD>()
                        prevR.aadds.indices.forEach {
                            val down = downQuantity.values[it].asBdd()
                            val intersect = prevL.aadds[it].clone() intersect prevR.aadds[it].clone()
                            resultsL.add(down.ite(intersect, prevL.aadds[it]))
                            resultsR.add(down.ite(intersect, prevR.aadds[it]))
                        }
                        l.downQuantity = VectorQuantity.fromCanonical(resultsL, prevL.upQuantity.unit, prevL.upQuantity.unitSpec, prevL.upQuantity.userWantedUnitSpec)
                        r.downQuantity = VectorQuantity.fromCanonical(resultsR, prevR.upQuantity.unit, prevR.upQuantity.unitSpec, prevR.upQuantity.userWantedUnitSpec)
                    }
                    prevL.isInt && prevR.isInt -> {
                        val resultsL = mutableListOf<IDD>()
                        val resultsR = mutableListOf<IDD>()
                        prevR.idds.indices.forEach {
                            val down = downQuantity.values[it].asBdd()
                            if (down.value in setOf(XBool.True, XBool.All)) {
                                val intersect = prevL.idds[it].clone() intersect prevR.idds[it].clone()
                                resultsL.add(intersect)
                                resultsR.add(intersect)
                            } else {
                                // l != r gives no interval information; keep operands unchanged
                                resultsL.add(prevL.idds[it])
                                resultsR.add(prevR.idds[it])
                            }
                        }
                        l.downQuantity = VectorQuantity(resultsL)
                        r.downQuantity = VectorQuantity(resultsR)
                    }
                    prevL.isBool && prevR.isBool -> {
                        // We leave Booleans for the discrete solver; no error!
                    }
                    prevL.isString && prevR.isString -> {
                        l.downQuantity = downQuantity.values[0].asBdd().ite(prevR.upQuantity,VectorQuantity(prevR.upQuantity.values[0].builder.Strings.All))
                        r.downQuantity = downQuantity.values[0].asBdd().ite(prevL.upQuantity,VectorQuantity(prevL.upQuantity.values[0].builder.Strings.All))
                    }
                    else -> throw SemanticError("Comparison only defined between Integers, Reals, and Booleans.")
                }
            }
            NEQ -> {
                //Nothing to do; handled by discrete solver.
            }

            GT -> {
                if (l.isReal && r.isReal) {
                    val downLs = mutableListOf<AADD>()
                    val downRs = mutableListOf<AADD>()
                    downQuantity.values.indices.forEach {
                        val isTrue = downQuantity.values[it].asBdd()
                        downLs.add(
                            isTrue.ite(
                                // When l > r must be true, constrain l to be > r
                                l.aadds[it] intersect RealRange(r.aadds[it].min, DoubleBound.PositiveInfinity),
                                // When l > r must be false, constrain l to be <= r  
                                l.aadds[it] intersect RealRange(DoubleBound.NegativeInfinity, r.aadds[it].max)
                            )
                        )
                        downRs.add(
                            isTrue.ite(
                                // When l > r must be true, constrain r to be < l
                                r.aadds[it] intersect RealRange(DoubleBound.NegativeInfinity, l.aadds[it].max),
                                // When l > r must be false, constrain r to be >= l
                                r.aadds[it] intersect RealRange(l.aadds[it].min, DoubleBound.PositiveInfinity)
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity.fromCanonical(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec, prevL.upQuantity.userWantedUnitSpec)
                    r.downQuantity = VectorQuantity.fromCanonical(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec, prevR.upQuantity.userWantedUnitSpec)
                } else if (l.isInt && r.isInt) {
                    val downLs = mutableListOf<IDD>()
                    val downRs = mutableListOf<IDD>()
                    downQuantity.values.indices.forEach {
                        val minL = l.idds[it].min
                        val maxL = l.idds[it].max
                        val minR = r.idds[it].min
                        val maxR = r.idds[it].max
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR < maxL) l.idd.builder.integer(max(minL, minR + 1L) .. maxL) else l.idd.builder.Integers.Empty,
                                if (minL <= maxR) l.idd.builder.integer(minL..min(maxL, maxR)) else l.idd.builder.Integers.Empty
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR < maxL) l.idd.builder.integer(minR..min(maxL-1L, maxR)) else l.idd.builder.Integers.Empty,
                                if (minL <= maxR) l.idd.builder.integer(max(minL, minR) .. maxR)  else l.idd.builder.Integers.Empty
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs)
                    r.downQuantity = VectorQuantity(downRs)
                }
            }

            GE -> {
                if (l.isReal && r.isReal) {
                    val downLs = mutableListOf<AADD>()
                    val downRs = mutableListOf<AADD>()
                    downQuantity.values.indices.forEach {
                        val isTrue = downQuantity.values[it].asBdd()
                        downLs.add(
                            isTrue.ite(
                                // When l >= r must be true, constrain l to be >= r
                                l.aadds[it] intersect RealRange(r.aadds[it].min, DoubleBound.PositiveInfinity),
                                // When l >= r must be false, constrain l to be < r  
                                l.aadds[it] intersect RealRange(DoubleBound.NegativeInfinity, r.aadds[it].max )
                            )
                        )
                        downRs.add(
                            isTrue.ite(
                                // When l >= r must be true, constrain r to be <= l
                                r.aadds[it].constrainTo(RealRange(DoubleBound.NegativeInfinity, l.aadds[it].max)),
                                // When l >= r must be false, constrain r to be > l
                                r.aadds[it].constrainTo(RealRange(l.aadds[it].min, DoubleBound.PositiveInfinity))
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity.fromCanonical(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec, prevL.upQuantity.userWantedUnitSpec)
                    r.downQuantity = VectorQuantity.fromCanonical(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec, prevR.upQuantity.userWantedUnitSpec)
                } else if (l.isInt && r.isInt) {
                    val downLs = mutableListOf<IDD>()
                    val downRs = mutableListOf<IDD>()
                    downQuantity.values.indices.forEach {
                        val minL = l.idds[it].min
                        val maxL = l.idds[it].max
                        val minR = r.idds[it].min
                        val maxR = r.idds[it].max
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR <= maxL) l.idd.builder.integer(max(minL, minR) .. maxL) else l.idd.builder.Integers.Empty,
                                if (minL < maxR) l.idd.builder.integer(minL..min(maxL, maxR - 1L)) else l.idd.builder.Integers.Empty
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR <= maxL) l.idd.builder.integer(minR..min(maxL, maxR)) else l.idd.builder.Integers.Empty,
                                if (minL < maxR) l.idd.builder.integer(max(minL + 1L, minR) .. maxR) else l.idd.builder.Integers.Empty
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs)
                    r.downQuantity = VectorQuantity(downRs)
                }
            }

            LT -> {
                if (l.isReal && r.isReal) {
                    val downLs = mutableListOf<AADD>()
                    val downRs = mutableListOf<AADD>()
                    downQuantity.values.indices.forEach {
                        val isTrue = downQuantity.values[it].asBdd()
                        downLs.add(
                            isTrue.ite(
                                // When l < r must be true, constrain l to be < r
                                l.aadds[it].constrainTo(RealRange(DoubleBound.NegativeInfinity, r.aadds[it].max )),
                                // When l < r must be false, constrain l to be >= r  
                                l.aadds[it].constrainTo(RealRange(r.aadds[it].min, DoubleBound.PositiveInfinity)),
                            )
                        )
                        downRs.add(
                            isTrue.ite(
                                // When l < r must be true, constrain r to be > l
                                r.aadds[it] intersect RealRange(l.aadds[it].min, DoubleBound.PositiveInfinity),
                                // When l < r must be false, constrain r to be <= l
                                r.aadds[it] intersect RealRange(DoubleBound.NegativeInfinity, l.aadds[it].max)
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity.fromCanonical(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec, prevL.upQuantity.userWantedUnitSpec)
                    r.downQuantity = VectorQuantity.fromCanonical(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec, prevR.upQuantity.userWantedUnitSpec)
                } else if (l.isInt && r.isInt) {
                    val downLs = mutableListOf<IDD>()
                    val downRs = mutableListOf<IDD>()
                    downQuantity.values.indices.forEach {
                        val minL = l.idds[it].min
                        val maxL = l.idds[it].max
                        val minR = r.idds[it].min
                        val maxR = r.idds[it].max
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL < maxR) l.idd.builder.integer(minL..min(maxL, maxR-1L)) else l.idd.builder.Integers.Empty,
                                if (minR <= maxL) l.idd.builder.integer( max(minL, minR) .. maxL) else l.idd.builder.Integers.Empty
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL < maxR) l.idd.builder.integer( max(minL+1L, minR) .. maxR) else l.idd.builder.Integers.Empty,
                                if (minR <= maxL) l.idd.builder.integer(minR..min(maxL, maxR)) else l.idd.builder.Integers.Empty
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs)
                    r.downQuantity = VectorQuantity(downRs)
                }
            }

            LE -> {
                if (l.isReal && r.isReal) {
                    val downLs = mutableListOf<AADD>()
                    val downRs = mutableListOf<AADD>()
                    downQuantity.values.indices.forEach {
                        val isTrue = downQuantity.values[it].asBdd()
                        downLs.add(
                            isTrue.ite(
                                // When l <= r must be true, constrain l to be <= r
                                l.aadds[it].constrainTo(RealRange(DoubleBound.NegativeInfinity, r.aadds[it].max)),
                                // When l <= r must be false, constrain l to be > r  
                                l.aadds[it].constrainTo(RealRange(r.aadds[it].min, DoubleBound.PositiveInfinity))
                            )
                        )
                        downRs.add(
                            isTrue.ite(
                                // When l <= r must be true, constrain r to be >= l
                                r.aadds[it].constrainTo(RealRange(l.aadds[it].min, DoubleBound.PositiveInfinity)),
                                // When l <= r must be false, constrain r to be < l
                                r.aadds[it].constrainTo(RealRange(DoubleBound.NegativeInfinity, l.aadds[it].max ))
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity.fromCanonical(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec, prevL.upQuantity.userWantedUnitSpec)
                    r.downQuantity = VectorQuantity.fromCanonical(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec, prevR.upQuantity.userWantedUnitSpec)
                } else if (l.isInt && r.isInt) {
                    val downLs = mutableListOf<IDD>()
                    val downRs = mutableListOf<IDD>()
                    downQuantity.values.indices.forEach {
                        val minL = l.idds[it].min
                        val maxL = l.idds[it].max
                        val minR = r.idds[it].min
                        val maxR = r.idds[it].max
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL <= maxR) l.idd.builder.integer(minL..min(maxL, maxR)) else l.idd.builder.Integers.Empty,
                                if (minR < maxL) l.idd.builder.integer( max(minL, minR + 1L) .. maxL) else l.idd.builder.Integers.Empty
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL <= maxR) l.idd.builder.integer( max(minL, minR) .. maxR) else l.idd.builder.Integers.Empty,
                                if (minR < maxL) l.idd.builder.integer(minR..min(maxL - 1L, maxR)) else l.idd.builder.Integers.Empty
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs)
                    r.downQuantity = VectorQuantity(downRs)
                }
            }
            else -> {
                // Else we do nothing; job of discrete solver.
            }
        }
    }


    override fun toExpressionString(): String {
        return l.toExpressionString() + " " + op + " " + r.toExpressionString()
    }

    override fun evalDownRec() {
        evalDown()
        l.evalDownRec()
        r.evalDownRec()
    }

    /** Executes a lambda on each AstNode in an Ast and returns its result */
    override fun <R> runDepthFirst(block: AstNode.() -> R): R {
        l.runDepthFirst(block)
        r.runDepthFirst(block)
        return this.run(block)
    }

    /** Executes a lambda on each AstNode in an Ast and returns its result */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R =
        run {
            l.withDepthFirst(l, block)
            r.withDepthFirst(r, block)
            receiver.block()
        }

    override fun toString() = "AstBinOp($l $op $r)"
    override fun clone(): AstBinOp =
        AstBinOp(l.clone(), op, r.clone()).also { it.root = root  }
}
