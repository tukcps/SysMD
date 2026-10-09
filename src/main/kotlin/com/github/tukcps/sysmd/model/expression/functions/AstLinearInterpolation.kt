package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.dd.AADD

/**
 * Predefined functions: linear
 * Parameters are:
 * - 0 = x-value of result
 * - 1,2 = x0, y0 of input
 * - 3,4 = x1, y1 of input
 * - ...
 * The function will return a VectorQuantity with the y-value for the given x-value.
 */
internal class AstLinearInterpolation(model: Session, args: ArrayList<AstNode>) :
    AstFunction("linear", model, 5, args) {

    private val numberOfParameters = parameters.size

    init {
        if (numberOfParameters % 2 == 0)
            throw SemanticError("Linear expects an odd number of parameters >= 5")
        // Constant???
    }

    override fun initialize() {
        if (parameters.any { it.upQuantity.values.size != 1 })
            throw VectorDimensionError("Linear is not possible with Vectors of size > 1.")
        for (i in 1 until numberOfParameters step 2) {
            if (getParam(i).upQuantity.values[0] !is AADD || getParam(i + 1).upQuantity.values[0] !is AADD)
                throw SemanticError("Linear only takes Real parameter")
        }
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(model.builder.Reals.All, "?")
            else -> throw SemanticError("Linear only takes Real parameter")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    /**
     * Parameters are:
     * - 0 = x-value of result
     * - 1,2 = x0, y0 of input
     * - 3,4 = x1, y1 of input
     * - ...
     */
    override fun evalUp() {
        val x = getParam(0).upQuantity.asQuantity()
        val x0 = getParam(1).upQuantity.asQuantity()
        val y0 = getParam(2).upQuantity.asQuantity()
        val x1 = getParam(3).upQuantity.asQuantity()
        val y1 = getParam(4).upQuantity.asQuantity()

        if (parameters.size == 5) {
            upQuantity = (x.le(x0)).bdd().ite(
                // 1. Constant if smaller x0
                y0,
                (x.ge(x1)).bdd().ite(
                    // 2. Constant if bigger x1
                    y1,
                    // Interpolation: y(x) = y0 + (y1-y0) / (x1-x0) (x-x0)
                    y0 + (y1 - y0) / (x1 - x0) * (x - x0)
                )
            )
            return
        }

        if (parameters.size == 7) {
            val x2 = getParam(5).upQuantity.asQuantity()
            val y2 = getParam(6).upQuantity.asQuantity()
            upQuantity = (x.le(x0)).bdd().ite(
                // 1. Constant if smaller than x0
                y0,
                (x.ge(x2)).bdd().ite(
                    // 2. Constant if bigger x2
                    y2,
                    // Interpolation: y(x) = y0 + (y1-y0) / (x1-x0) (x-x0)
                    // Interpolation: y(x) = y1 + (y2-y1) / (x2-x1) (x-x1)
                    (x.le(x1)).bdd().ite(
                        y0 + (y1 - y0) / (x1 - x0) * (x - x0), // x between x0 and x1
                        y1 + (y2 - y1) / (x2 - x1) * (x - x1)
                    ) // x between x1 and x2
                )
            )
            return
        }

        // General recursive implementation for 9+ parameters
        val N = (parameters.size - 1) / 2 - 1 // number of segments
        val xPoints = (0..N).map { getParam(1 + it * 2).upQuantity.asQuantity() }
        val yPoints = (0..N).map { getParam(2 + it * 2).upQuantity.asQuantity() }

        fun getSegmentInterpolation(i: Int): VectorQuantity {
            val xi = xPoints[i]
            val yi = yPoints[i]
            val xNext = xPoints[i+1]
            val yNext = yPoints[i+1]
            return yi + (yNext - yi) / (xNext - xi) * (x - xi)
        }

        fun buildIte(i: Int): VectorQuantity {
            if (i == N - 1) {
                return getSegmentInterpolation(i)
            }
            return (x.le(xPoints[i+1])).bdd().ite(
                getSegmentInterpolation(i),
                buildIte(i + 1)
            )
        }

        val innerInterpolation = buildIte(0)

        upQuantity = (x.le(xPoints[0])).bdd().ite(
            yPoints[0],
            (x.ge(xPoints[N])).bdd().ite(
                yPoints[N],
                innerInterpolation
            )
        )
    }

    override fun evalDown() {
        // new Quantity of downQuantity with same range
        val y = VectorQuantity(
            model.builder.real((downQuantity.asQuantity()).getRange()),
            downQuantity.unit,
            downQuantity.unitSpec
        )
        val x0 = getParam(1).upQuantity.asQuantity()
        val y0 = getParam(2).upQuantity.asQuantity()
        val x1 = getParam(3).upQuantity.asQuantity()
        val y1 = getParam(4).upQuantity.asQuantity()
        // val increasing = y0.le(y1).bdd() // true if y0 <= y1
        if (parameters.size == 5) {
            val x = getParam(0)
            if (y0.getMinAsDouble() == y0.getMaxAsDouble() && y1.getMinAsDouble() == y1.getMaxAsDouble() &&
                y0.getMinAsDouble() == y1.getMinAsDouble()) {
                // Flat segment: x is unrestricted if the constant is in the result's range, else impossible.
                if (y0.getMinAsDouble() < y.getMinAsDouble() || y0.getMinAsDouble() > y.getMaxAsDouble())
                    x.downQuantity = x.downQuantity.constrain(VectorQuantity(model.builder.Reals.Empty, x0.unit, x0.unitSpec))
                return
            }
            //calc x with the help of the upQuantities of x0,x1,y0,y1 and with the downQuantity of y
            if (y0.getMinAsDouble() > y1.getMaxAsDouble()) {
                // decreasing segment: y >= y0 means x <= x0, y <= y1 means x >= x1
                x.downQuantity = x.downQuantity.constrain((y.ge(y0)).bdd().ite(
                    VectorQuantity(model.builder.real(Double.NEGATIVE_INFINITY..x0.getMaxAsDouble()), x0.unit, x0.unitSpec),
                    (y.le(y1)).bdd().ite(
                        VectorQuantity(model.builder.real(x1.getMinAsDouble()..Double.POSITIVE_INFINITY), x1.unit, x1.unitSpec),
                        x0 + (x1 - x0) / (y1 - y0) * (y - y0)
                    )
                ))
                return
            }
            x.downQuantity = x.downQuantity.constrain((y.le(y0)).bdd().ite(
                // must be same value as y0 or smaller
                VectorQuantity(model.builder.real(Double.NEGATIVE_INFINITY..x0.getMaxAsDouble()), x0.unit, x0.unitSpec),
                (y.ge(y1)).bdd().ite(
                    // must be same value as x1 or bigger
                    VectorQuantity(model.builder.real(x1.getMinAsDouble()..Double.POSITIVE_INFINITY), x1.unit, x1.unitSpec),
                    x0 + (x1 - x0) / (y1 - y0) * (y - y0)
                )
            ))
            return
        }

        evalDownPiecewise()
    }

    /**
     * Inverse for 3 or more points: the x-values are the union over all segments (and the constant parts left of the
     * first and right of the last point) whose y-values overlap the result; we return their hull.
     * Segments may be increasing, decreasing or flat. If a point is not a single value, narrowing is not sound
     * and x is left unchanged.
     */
    private fun evalDownPiecewise() {
        val n = (parameters.size - 1) / 2
        val xq = (0 until n).map { getParam(1 + it * 2).upQuantity.asQuantity() }
        val yq = (0 until n).map { getParam(2 + it * 2).upQuantity.asQuantity() }
        if ((xq + yq).any { it.getMinAsDouble() != it.getMaxAsDouble() })
            return
        val xs = xq.map { it.getMinAsDouble() }
        val ys = yq.map { it.getMinAsDouble() }
        val yLo = downQuantity.getMinAsDouble()
        val yHi = downQuantity.getMaxAsDouble()

        var lo = Double.POSITIVE_INFINITY
        var hi = Double.NEGATIVE_INFINITY
        fun add(a: Double, b: Double) {
            lo = minOf(lo, a)
            hi = maxOf(hi, b)
        }

        if (ys[0] in yLo..yHi) add(Double.NEGATIVE_INFINITY, xs[0])
        if (ys[n - 1] in yLo..yHi) add(xs[n - 1], Double.POSITIVE_INFINITY)
        for (i in 0 until n - 1) {
            val segLo = minOf(ys[i], ys[i + 1])
            val segHi = maxOf(ys[i], ys[i + 1])
            if (segHi < yLo || segLo > yHi) continue
            if (ys[i] == ys[i + 1]) {
                add(xs[i], xs[i + 1])
                continue
            }
            fun xAt(y: Double) = xs[i] + (xs[i + 1] - xs[i]) / (ys[i + 1] - ys[i]) * (y.coerceIn(segLo, segHi) - ys[i])
            val xa = xAt(yLo)
            val xb = xAt(yHi)
            add(minOf(xa, xb), maxOf(xa, xb))
        }
        val range = if (lo <= hi) model.builder.real(lo..hi) else model.builder.Reals.Empty
        val x = getParam(0)
        x.downQuantity = x.downQuantity.constrain(VectorQuantity(range, xq[0].unit, xq[0].unitSpec))
    }

    override fun clone() = AstLinearInterpolation(model, cloneParameters())
}
