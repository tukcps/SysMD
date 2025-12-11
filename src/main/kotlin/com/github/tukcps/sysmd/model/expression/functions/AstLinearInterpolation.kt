package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.session.Session

/**
 * Predefined functions: linear
 * This function implements a linear interpolation between two points or more points
 * It takes an x-value and pairs of x0, y0 and x1, y1 as parameters.
 * The first parameter is the x-value for which the y-value is calculated.
 * The second and third parameters are the first point (x0, y0).
 * The fourth and fifth parameters are the second point (x1, y1).
 * If more points are given, the function will interpolate between them.
 * The function will return a Quantity with the y-value for the given x-value.
 */
internal class AstLinearInterpolation(model: Session, args: ArrayList<AstNode>) :
    AstFunction("linearInterpolation", model, 5, args) {

    private val numberOfParameters = parameters.size

    init {
        if (numberOfParameters % 2 != 1 && numberOfParameters >= 3)
            throw SemanticError("linear needs an even number of real-valued parameters")
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
            is AADD -> Quantity(model.builder.Reals, "?")
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
        }
    }

    override fun evalDown() {
        // new Quantity of downQuantity with same range
        val y = Quantity(
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
            //calc x with the help of the upQuantities of x0,x1,y0,y1 and with the downQuantity of y
            getParam(0).downQuantity = (y.le(y0)).bdd().ite(
                // must be same value as y0 or smaller
                Quantity(model.builder.real(-Double.MAX_VALUE..x0.getMaxAsDouble()), x0.unit, x0.unitSpec),
                (y.ge(y1)).bdd().ite(
                    // must be same value as x1 or bigger
                    Quantity(model.builder.real(x1.getMinAsDouble()..Double.MAX_VALUE), x1.unit, x1.unitSpec),
                    x0 + (x1 - x0) / (y1 - y0) * (y - y0)
                )
            )
        }

        if (parameters.size == 7) {
            val x2 = getParam(5).upQuantity.asQuantity()
            val y2 = getParam(6).upQuantity.asQuantity()
            val xValueOfInterpolY0Y1 = x0 + (x1 - x0) / (y1 - y0) * (y - y0)
            val xValueOfInterpolY1Y2 = x1 + (x2 - x1) / (y2 - y1) * (y - y1)
            val y0y1BothBiggerOrSmallerThanY =
                ((y.ge(y0)).bdd().and(y.ge(y1).bdd())).or((y.lt(y0)).bdd().and(y.lt(y1).bdd()))
            val y1y2BothBiggerOrSmallerThanY =
                ((y.ge(y0)).bdd().and(y.ge(y1).bdd())).or((y.lt(y0)).bdd().and(y.lt(y1).bdd()))
            getParam(0).downQuantity = (y.eq(y0)).bdd().ite(
                y1y2BothBiggerOrSmallerThanY.ite(
                    Quantity(
                        model.builder.real(-Double.MAX_VALUE..x0.getMaxAsDouble()),
                        x0.unit,
                        x0.unitSpec
                    ),
                    Quantity(
                        model.builder.real(-Double.MAX_VALUE..xValueOfInterpolY1Y2.getMaxAsDouble()),
                        x0.unit,
                        x0.unitSpec
                    )
                ),
                (y.eq(y2)).bdd().ite(
                    y0y1BothBiggerOrSmallerThanY.ite(
                        Quantity(
                            model.builder.real(x1.getMinAsDouble()..Double.MAX_VALUE),
                            x1.unit,
                            x1.unitSpec
                        ),
                        Quantity(
                            model.builder.real(xValueOfInterpolY0Y1.getMinAsDouble()..Double.MAX_VALUE),
                            x1.unit,
                            x1.unitSpec
                        )
                    ),
                    ((y.le(y1)).bdd().and(y0.le(y1).bdd())).or((y.gt(y1)).bdd().and(y0.gt(y1).bdd())).ite(
                        y1y2BothBiggerOrSmallerThanY.ite(
                            xValueOfInterpolY0Y1,
                            Quantity(
                                model.builder.real(
                                    xValueOfInterpolY0Y1.getMinAsDouble()..xValueOfInterpolY1Y2.getMaxAsDouble()
                                ).asAadd(), x1.unit, x1.unitSpec
                            )
                        ),
                        xValueOfInterpolY1Y2
                    )
                )
            )
        }
    }

    override fun clone(): AstLinearInterpolation {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstLinearInterpolation(model, parClone)
    }
}
