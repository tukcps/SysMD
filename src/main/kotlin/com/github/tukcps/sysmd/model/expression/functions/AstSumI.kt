package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.functions.ceil
import io.github.tukcps.aadd.functions.floor
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * The sum_i function.
 * Takes three parameters:
 * 1) Initial value of run variable i, a constant.
 * 2) Final value of run variable i, an IDD.
 * 3) An AstNode that can make use of i.
 */
internal class AstSumI(
    private val namespace: Namespace,
    model: Session,
    args: ArrayList<AstNode>
) : AstFunction("sum_i", model, 3, args) {
    var sum: AstNode? = null

    private var startI: AstNode = getParam(0)
    private var endI: AstNode = getParam(1)
    private val iteration: AstNode = getParam(2)

    override fun initialize() {
        //TODO Support sumI for Vectors in iteration
        if (startI.upQuantity.values.size != 1 || endI.upQuantity.values.size != 1 || iteration.upQuantity.values.size != 1)
            throw VectorDimensionError("StartI, EndI and  in sum_i must be numbers and no Vectors")

        // Check that 1st parameter is int or real
        if (!startI.isInt && !startI.isReal)
            throw SemanticError("sum_i function expected: 1st parameter of type Real or Integer, got: ${getParam(0).upQuantity}")

        // Check that 2nd parameter is int or real
        if (!endI.isInt && !endI.isReal)
            throw SemanticError("sum_i function expected: 2nd parameter of type Integer or Real, got: ${getParam(1).upQuantity}")

        if(startI.isInt && startI.idd.min>endI.idd.min||startI.isReal && startI.aadd.min>endI.aadd.min)
            throw SemanticError("sum_i expects startI.min<=endI.min")

        if(startI.isInt && startI.idd.max>endI.idd.max || startI.isReal && startI.aadd.max>endI.aadd.max)
            throw SemanticError("sum_i expects startI.max<=endI.max")
        // Check that 3rd parameter is int or real
        if (!iteration.isInt && !iteration.isReal)
            throw SemanticError("sum_i function expected: 3rd parameter of type Real or Integer, got: ${getParam(2).upQuantity}")

        if (iteration.isReal) upQuantity =
            VectorQuantity(mutableListOf(model.builder.Reals.clone()), iteration.upQuantity.unit)
        if (iteration.isInt) upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.clone()))
        downQuantity = upQuantity.clone()
    }

    override fun evalUpRec() {
        startI.evalUpRec()
        endI.evalUpRec()
        evalUp()
    }

    override fun evalUp() {
        if (startI.isInt) {
            upQuantity = VectorQuantity(mutableListOf(model.builder.integer(0)))
            // test if startI or endI is not defined
            if (startI.idds[0].min == Long.MIN_VALUE || startI.idds[0].max == Long.MAX_VALUE ||
                endI.idds[0].min == Long.MIN_VALUE || endI.idds[0].max == Long.MAX_VALUE
            )
                return // no loop possible with infinite borders
            upQuantity = subSumInt(startI.idds[0], endI.idds[0])
        } else { //startI is Real (see initialize)
            upQuantity = VectorQuantity(mutableListOf(model.builder.real(0.0)), iteration.upQuantity.unit)
            // test if startI or endI is not defined
            if (startI.aadds[0].min.isInfinite() || startI.aadds[0].max.isInfinite() || endI.aadds[0].min.isInfinite() || endI.aadds[0].max.isInfinite()) {
                upQuantity = Quantity(model.builder.Reals, iteration.upQuantity.unit)
                return // no loop possible with infinite borders
            }
            upQuantity = subSumReal(startI.aadds[0], endI.aadds[0])
        }
    }

    /**
     * Just compute the AST as set up in the init section.
     */
    override fun evalDown() {
        if (downQuantity.values[0] is IDD) {
            if (startI.idds[0].getRange().min == Long.MIN_VALUE || startI.idds[0].getRange().max == Long.MAX_VALUE) {
                getParam(1).downQuantity = Quantity(model.builder.Integers) //no infinite loops
            } else {
                // calculate endI
                val endIReverseMin = reverseSumInt(
                    downQuantity.values[0].asIdd().getRange().min,
                    startI.idds[0].getRange().min,
                    isEnd = true
                )
                val endIReverseMax = reverseSumInt(
                    downQuantity.values[0].asIdd().getRange().max,
                    startI.idds[0].getRange().max,
                    isEnd = true
                )
                getParam(1).downQuantity = Quantity(
                    model.builder.integer(
                        min(endIReverseMin.min, endIReverseMax.min)..max(endIReverseMin.max, endIReverseMax.max)
                    )
                )
            }
            if (endI.idds[0].getRange().min == Long.MIN_VALUE || endI.idds[0].getRange().max == Long.MAX_VALUE) {
                getParam(0).downQuantity = Quantity(model.builder.Integers) //no infinite loops
            } else {
                // calculate startI
                val startIReverseMin = reverseSumInt(
                    downQuantity.values[0].asIdd().getRange().max,
                    endI.idds[0].getRange().min,
                    isEnd = false
                )
                val startIReverseMax = reverseSumInt(
                    downQuantity.values[0].asIdd().getRange().min,
                    endI.idds[0].getRange().max,
                    isEnd = false
                )
                getParam(0).downQuantity = Quantity(
                    model.builder.integer(
                        min(startIReverseMin.min, startIReverseMax.min)..max(startIReverseMin.max, startIReverseMax.max)
                    )
                )
            }
        } else if (downQuantity.values[0] is AADD) {
            val unit = getParam(2).upQuantity.unit
            if (startI.aadds[0].getRange().min.isInfinite() || startI.aadds[0].getRange().max.isInfinite()) {
                getParam(1).downQuantity = Quantity(model.builder.Reals, unit) //no infinite loops
            } else {
                // calculate endI
                val endIReverse = reverseSumReal(
                    downQuantity.values[0] as AADD,
                    startI.aadds[0].getRange(),
                    endI.aadds[0].getRange(),
                    isEnd = true
                )
                getParam(1).downQuantity = Quantity(model.builder.real(endIReverse), unit)
            }
            if (endI.aadds[0].getRange().min.isInfinite() || endI.aadds[0].getRange().max.isInfinite()) {
                getParam(0).downQuantity = Quantity(model.builder.Reals, unit) //no infinite loops
            } else {
                // calculate startI
                val startIReverse = reverseSumReal(
                    downQuantity.values[0] as AADD,
                    endI.aadds[0].getRange(),
                    startI.aadds[0].getRange(),
                    isEnd = false
                )
                getParam(0).downQuantity = Quantity(model.builder.real(startIReverse), unit)
            }
        }
        return
    }

    /**
     * Calculates Sum Reverse for evalDown of type Int
     * @param sum quantity for calculating the sum
     * @param startI i to start iteration
     * @param isEnd true if the endI is calculated, if false the startI is calculated
     * @return IntegerRange for endI (isEnd true) or startI (isEndFalse)
     */
    private fun reverseSumInt(sum: Long, startI: Long, isEnd: Boolean): IntegerRange {
        var currentSum = VectorQuantity(mutableListOf(model.builder.integer(sum)))
        var i = startI
        while (currentSum.values[0].asIdd().getRange().min > 0 && i >= 0) {
            // set variable to i and evaluate iteration for it.
            require(namespace.resolveVar("i") != null)
            namespace.resolveVar("i")!!.intSpec(IntegerRange(i)).initVectorQuantity()
            iteration.evalUpRec()
            currentSum -= iteration.upQuantity
            if (isEnd) i += 1 else i -= 1
            if(i<0 && currentSum.values[0].asIdd().getRange().min > 0)
                i=-2 //special case, because this case does not work otherwise. i+2 is calculated before returning
        }
        return if (currentSum.values[0].asIdd().getRange().min == 0L) //result exact
            if (isEnd)
                IntegerRange(IntegerRange().minusOverflowDetection(i, 1), IntegerRange().minusOverflowDetection(i, 1))
            else
                IntegerRange(IntegerRange().plusOverflowDetection(i, 1), IntegerRange().plusOverflowDetection(i, 1))
        else
            if (isEnd)
                IntegerRange(IntegerRange().minusOverflowDetection(i, 2), IntegerRange().minusOverflowDetection(i, 2))
            else
                IntegerRange(IntegerRange().plusOverflowDetection(i, 2), IntegerRange().plusOverflowDetection(i, 2))
    }

    /**
     * Calculates Sum Reverse for evalDown of type Real
     * @param sum quantity for calculating the sum
     * @param startCalculation i to start iteration
     * @param maximumEndI end for calculation, because interval can ot be extended
     * @param isEnd true if the endI is calculated, if false the startI is calculated
     * @return Range for endI (isEnd true) or startI (isEndFalse)
     */
    private fun reverseSumReal(sum: AADD, startCalculation: Range, maximumEndI: Range, isEnd: Boolean): Range {
        val resultIList = mutableListOf<Double>()
        for (i in startCalculation.min.roundToLong()..startCalculation.max.roundToLong()) {
            var iterator = i
            var currentSum = VectorQuantity(mutableListOf(sum), "?")
            while (iterator <= maximumEndI.max && isEnd || iterator >= maximumEndI.min && !isEnd) { //avoid infinite loops by maximum possible result
                // set variable to i and evaluate iteration for it.
                require(namespace.resolveVar("i") != null)
                namespace.resolveVar("i")!!.rangeSpec(Range(iterator.toDouble())).initVectorQuantity()
                iteration.evalUpRec()
                currentSum -= iteration.upQuantity
                if (0.0 in currentSum.aadd().min..currentSum.aadd().max) {
                    resultIList.add(iterator.toDouble())
                    if (-0.0001 < currentSum.aadd().min && currentSum.aadd().max < 0.0001)
                        break
                }
                if (isEnd) iterator += 1 else iterator -= 1
            }
        }
        if (resultIList.isEmpty()) {
            resultIList.add(if (!isEnd) startCalculation.min else 0.0)
            resultIList.add(if (isEnd) Double.POSITIVE_INFINITY else startCalculation.max)
        }
        resultIList.sort()
        val minResult = resultIList[0] - 0.5
        return Range(minResult, resultIList[resultIList.size - 1] + 0.5)
    }

    /**
     * Maximal SubSum for the function using Kadane's algorithm
     */
    private fun subSumReal(startI: AADD, endI: AADD): Quantity {
        var maxSum = model.builder.real(0.0)
        var minSum = model.builder.real(0.0)
        var currSumMax = model.builder.real(0.0)
        var currSumMin = model.builder.real(0.0)

        for (i in floor(startI).min.toLong()..ceil(endI).max.toLong()) {
            //either in startI, endI or between startI and endI
            if (startI.contains(i.toDouble()) || endI.contains(i.toDouble())
                || model.builder.real((startI.min + startI.max) / 2.0 .. (endI.min + endI.max) / 2.0)
                    .contains(i.toDouble())
            ) {
                // set variable to i and evaluate iteration for it.
                require(namespace.resolveVar("i") != null)
                namespace.resolveVar("i")!!.rangeSpec(Range(i.toDouble())).initVectorQuantity()
                iteration.evalUpRec()
                val iterationValue = iteration.upQuantity.values[0] as AADD
                currSumMax += iterationValue
                currSumMin += iterationValue
                if (i >= endI.min + 1) { // (i>=endLow+1) these elements are added to the sum if needed
                    // maxSum = (maxSum.greaterThanOrEquals(currSumMax)).asBdd().ite(maxSum, currSumMax) ==> exponential growth of tree size
                    maxSum = model.builder.real(max(maxSum.max, currSumMax.max))
                    // minSum = (minSum.lessThanOrEquals(currSumMin)).asBdd().ite(minSum, currSumMin) ==> exponential growth of tree size
                    minSum = model.builder.real(min(minSum.min, currSumMin.min))
                } else if (i <= startI.max) { // in this area the sum must start
                    //maxSum reset the start of the sum (use max instead of ite -> otherwise exponential growth of tree size)
                    //currSumMax = (currSumMax.greaterThanOrEquals(iterationValue)).asBdd().ite(currSumMax, iterationValue) ==> exponential growth of tree size
                    currSumMax = model.builder.real(max(currSumMax.max, iterationValue.max))
                    maxSum = currSumMax
                    //minSum  reset the start of the sum  (use min instead of ite -> otherwise exponential growth of tree size)
                    //currSumMin = (currSumMin.lessThanOrEquals(iterationValue)).asBdd().ite(currSumMin, iterationValue) ==> exponential growth of tree size
                    currSumMin = model.builder.real(min(currSumMin.min, iterationValue.min))
                    minSum = currSumMin
                } else { //between startI and endI
                    //these elements are required for the sum
                    //maxSum
                    maxSum = currSumMax // not reset of the sum, because this area must be included in the final result
                    //minSum
                    minSum = currSumMin // not reset of the sum, because this area must be included in the final result
                }
            }
        }
        return Quantity(model.builder.real(minSum.min..maxSum.max), iteration.upQuantity.unit)
    }

    /**
     * Maximal SubSum for the function using Kadane's algorithm
     */
    private fun subSumInt(startI: IDD, endI: IDD): Quantity {
        var maxSum = model.builder.integer(0)
        var minSum = model.builder.integer(0)
        var currSumMax = model.builder.integer(0)
        var currSumMin = model.builder.integer(0)

        for (i in startI.min..endI.max) {
            require(namespace.resolveVar("i") != null)
            namespace.resolveVar("i")!!.intSpec(IntegerRange(i)).initVectorQuantity()
            iteration.evalUpRec()
            val iterationValue = iteration.upQuantity.values[0] as IDD
            currSumMax += iterationValue
            currSumMin += iterationValue
            if (i > endI.min) { // (i>=endLow+1) these elements are added to the sum if needed
                maxSum = model.builder.integer(max(maxSum.max, currSumMax.max))
                minSum = model.builder.integer(min(minSum.min, currSumMin.min))
            } else if (i <= startI.max) { // in this area the sum must start
                currSumMax = model.builder.integer(max(currSumMax.max, iterationValue.max))
                maxSum = currSumMax
                currSumMin = model.builder.integer(min(currSumMin.min, iterationValue.min))
                minSum = currSumMin
            } else { //between startI and endI
                maxSum = currSumMax // not reset of the sum, because this area must be included in the final result
                minSum = currSumMin // not reset of the sum, because this area must be included in the final result
            }
        }
        return Quantity(model.builder.integer(minSum.min..maxSum.max), iteration.upQuantity.unit)
    }

    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone(): AstSumI {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstSumI(namespace, model, parClone)
    }
}
