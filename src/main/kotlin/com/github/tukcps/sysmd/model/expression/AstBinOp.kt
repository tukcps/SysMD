package com.github.tukcps.sysmd.model.expression

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import kotlin.math.max
import kotlin.math.min


/**
 * @class AstBinOp
 * A binary operation.
 *
 * @author Christoph Grimm, Jack D. Martin
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
        upQuantity = if (op in setOf(GE, LE, EE, GT, LT, AND, OR, NEQ)) VectorQuantity(mutableListOf(model.builder.Bool))
        else if (l.isReal) VectorQuantity(mutableListOf(model.builder.Reals),"?")
        else if (l.isInt) VectorQuantity(mutableListOf(model.builder.Integers))
        else VectorQuantity(mutableListOf(model.builder.Reals),"?")
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
            EXP -> l.upQuantity pow r.upQuantity
            EE  -> l.upQuantity eq r.upQuantity
            NEQ -> l.upQuantity neq r.upQuantity
            else -> throw SemanticError("Operation $op resp. $op not supported here.")
        }
    }


    /** Computes one level downwards, from parent to children */
    override fun evalDown() {
        val prevL = l
        val prevR = r
        // Only for arithmetic operations; boolean are handled via BDD only bottom-up.

        when (op) {
            AND -> {
                l.downQuantity = downQuantity and r.upQuantity
                r.downQuantity = downQuantity and l.upQuantity
            }

            OR -> {
                // 0 1 -> 1 resp. 0, 1 - 0
                // 1 0 -> 1 resp. 0, 0,- 0
                // 1 1 -> 1 resp. 1, 1, X
                // 0 0 -> 0 resp. 1, 0, 1
                val resultsR = mutableListOf<BDD>()
                val resultsL = mutableListOf<BDD>()
                downQuantity.values.indices.forEach {
                    resultsR.add( downQuantity.values[it].asBdd().ite(
                        l.upQuantity.values[it].asBdd().ite(model.builder.Bool, model.builder.False),
                        l.upQuantity.values[it].asBdd().ite(model.builder.False, model.builder.True)
                    ))
                    resultsL.add( downQuantity.values[it].asBdd().ite(
                        r.upQuantity.values[it].asBdd().ite(model.builder.Bool, model.builder.False),
                        r.upQuantity.values[it].asBdd().ite(model.builder.False, model.builder.True)
                    ))
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
            TIMES -> {
                l.downQuantity = downQuantity / prevR.upQuantity
                r.downQuantity = downQuantity / prevL.upQuantity
            }
            DIV -> {
                l.downQuantity = downQuantity * prevR.upQuantity
                r.downQuantity = prevL.upQuantity / downQuantity
            }
            EXP -> {
                l.downQuantity = when(prevR.upQuantity.values[0]){
                    is AADD -> {
                        val results = mutableListOf<AADD>()
                        downQuantity.values.indices.forEach {
                            results.add(downQuantity.values[it].asAadd() power model.builder.real(1.0).div(prevR.upQuantity.values[it] as AADD) )
                        }
                        VectorQuantity(results,downQuantity.unit,downQuantity.unitSpec)
                    }
                    is IDD ->{
                        val results = mutableListOf<IDD>()
                        downQuantity.values.indices.forEach { results.add(downQuantity.values[it].asIdd().root(prevR.upQuantity.values[it] as IDD) ) }
                        VectorQuantity(results)
                    }
                    else -> throw SemanticError("Expect base of type Real or Integer")
                }
                r.downQuantity = when(prevL.upQuantity.values[0]){
                    is AADD -> {
                        val results = mutableListOf<AADD>()
                        downQuantity.values.indices.forEach {
                            if (prevL.aadds[it].min == 1.0 && prevL.aadds[it].max  == 1.0) //Log with 1 not possible. All resulting values allowed
                                results.add(model.builder.Reals.clone())
                            else
                                results.add((downQuantity.values[it] as AADD).log()/prevL.aadds[it].log())
                        }
                        VectorQuantity(results,downQuantity.unit,downQuantity.unitSpec)
                    }
                    is IDD  -> {
                        val results = mutableListOf<IDD>()
                        downQuantity.values.indices.forEach {
                            if (prevL.idds[it].min == 1L && prevL.idds[it].max  == 1L) //Log with 1 not possible. All resulting values allowed
                                results.add(model.builder.Integers.clone())
                            else
                                results.add((downQuantity.values[it] as IDD).log(prevL.idds[it]))
                        }
                        VectorQuantity(results)
                    }
                    else -> throw SemanticError("Expect base of type Real or Integer")
                }
            }
            EE -> {
                when {
                    prevL.isReal && prevR.isReal -> {
                        val resultsR = mutableListOf<AADD>()
                        val resultsL = mutableListOf<AADD>()
                        prevR.aadds.indices.forEach { resultsR.add(downQuantity.values[it].asBdd().ite(prevR.aadds[it], prevR.aadd.builder.Reals)) }
                        prevL.aadds.indices.forEach { resultsL.add(downQuantity.values[it].asBdd().ite(prevL.aadds[it], prevL.aadd.builder.Reals)) }
                        l.downQuantity = VectorQuantity(resultsR, prevL.downQuantity.unit, prevL.downQuantity.unitSpec)
                        r.downQuantity = VectorQuantity(resultsL, prevR.downQuantity.unit, prevR.downQuantity.unitSpec)
                    }
                    prevL.isInt && prevR.isInt -> {
                        val results = mutableListOf<IDD>()
                        prevR.idds.indices.forEach {
                            val down = downQuantity.values[it].asBdd()
                            val intersect = l.idds[it].clone() intersect r.idds[it].clone().clone()
                            val result: IDD = if (down.value in setOf(XBool.True, XBool.X))
                                intersect
                            else
                                this.model.builder.EmptyIntegerRange
                            results.add(result)
                        }
                        l.downQuantity = VectorQuantity(results)
                        r.downQuantity = VectorQuantity(results)
                    }
                    prevL.isBool && prevR.isBool -> {
                        // We leave Booleans for the discrete solver; no error!
                    }
                    prevL.isString && prevR.isString -> {
                        l.downQuantity = downQuantity.value.asBdd().ite(prevR.upQuantity,VectorQuantity(prevR.upQuantity.value.builder.Strings))
                        r.downQuantity = downQuantity.value.asBdd().ite(prevL.upQuantity,VectorQuantity(prevL.upQuantity.value.builder.Strings))
                    }
                    else ->
                        throw SemanticError("Comparison only defined between Integers, Reals, and Booleans.")
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
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] greaterThan r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] lessThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] lessThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] greaterThan r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs, prevL.upQuantity.unit,prevL.upQuantity.unitSpec)
                    r.downQuantity = VectorQuantity(downRs, prevR.upQuantity.unit,prevR.upQuantity.unitSpec)
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
                                if (minR < maxL) l.idd.builder.integer(max(minL, minR+1) .. maxL) else l.idd.builder.EmptyIntegerRange,
                                if (minL < maxR) l.idd.builder.integer( minL..min(maxL, maxR-1)) else l.idd.builder.EmptyIntegerRange
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR < maxL) l.idd.builder.integer(minR..min(maxL-1, maxR)) else l.idd.builder.EmptyIntegerRange,
                                if (minL < maxR) l.idd.builder.integer(max(minL+1, minR) .. maxR)  else l.idd.builder.EmptyIntegerRange
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
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] greaterThanOrEquals  r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] lessThan r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] lessThan r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] greaterThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs, prevL.upQuantity.unit,prevL.upQuantity.unitSpec)
                    r.downQuantity = VectorQuantity(downRs, prevR.upQuantity.unit,prevR.upQuantity.unitSpec)
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
                                if (minR <= maxL) l.idd.builder.integer(max(minL, minR) .. maxL) else l.idd.builder.EmptyIntegerRange,
                                if (minL <= maxR) l.idd.builder.integer( minL..min(maxL, maxR)) else l.idd.builder.EmptyIntegerRange
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minR <= maxL) l.idd.builder.integer(minR..min(maxL, maxR)) else l.idd.builder.EmptyIntegerRange,
                                if (minL <= maxR) l.idd.builder.integer(max(minL, minR) .. maxR) else l.idd.builder.EmptyIntegerRange
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
                        downLs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] lessThan r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] greaterThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                (l.aadds[it] greaterThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                (l.aadds[it] lessThan r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec)
                    r.downQuantity = VectorQuantity(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec)
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
                                if (minL < maxR) l.idd.builder.integer(minL..min(maxL, maxR-1)) else l.idd.builder.EmptyIntegerRange,
                                if (minR < maxL) l.idd.builder.integer( max(minL, minR+1) .. maxL) else l.idd.builder.EmptyIntegerRange
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL < maxR) l.idd.builder.integer( max(minL+1, minR) .. maxR) else l.idd.builder.EmptyIntegerRange,
                                if (minR < maxL) l.idd.builder.integer(minR..min(maxL-1, maxR)) else l.idd.builder.EmptyIntegerRange
                            )
                        )
                    }
                    l.downQuantity = VectorQuantity(downLs)
                    r.downQuantity = VectorQuantity(downRs)
                }
            }

            LE -> {
                if (l.isReal && r.isReal) {
                    if (l.isReal && r.isReal) {
                        val downLs = mutableListOf<AADD>()
                        val downRs = mutableListOf<AADD>()
                        downQuantity.values.indices.forEach {
                            downLs.add(
                                downQuantity.values[it].asBdd().ite(
                                    (l.aadds[it] lessThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                    (l.aadds[it] greaterThan r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                                )
                            )
                            downRs.add(
                                downQuantity.values[it].asBdd().ite(
                                    (l.aadds[it] greaterThan r.aadds[it]).ite(l.aadds[it], r.aadds[it]),
                                    (l.aadds[it] lessThanOrEquals r.aadds[it]).ite(l.aadds[it], r.aadds[it])
                                )
                            )
                        }
                        l.downQuantity = VectorQuantity(downLs, prevL.upQuantity.unit, prevL.upQuantity.unitSpec)
                        r.downQuantity = VectorQuantity(downRs, prevR.upQuantity.unit, prevR.upQuantity.unitSpec)
                    }
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
                                if (minL <= maxR) l.idd.builder.integer(minL..min(maxL, maxR)) else l.idd.builder.EmptyIntegerRange,
                                if (minR <= maxL) l.idd.builder.integer( max(minL, minR) .. maxL) else l.idd.builder.EmptyIntegerRange
                            )
                        )
                        downRs.add(
                            downQuantity.values[it].asBdd().ite(
                                if (minL <= maxR) l.idd.builder.integer( max(minL, minR) .. maxR) else l.idd.builder.EmptyIntegerRange,
                                if (minR <= maxL) l.idd.builder.integer(minR..min(maxL, maxR)) else l.idd.builder.EmptyIntegerRange
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
        with(receiver) {
            withDepthFirst(l, block)
            withDepthFirst(r, block)
            return block()
        }

    override fun toString() = "AstBinOp($l $op $r)"
    override fun clone(): AstBinOp =
        AstBinOp(l.clone(), op, r.clone()).also { it.root = root  }
}
