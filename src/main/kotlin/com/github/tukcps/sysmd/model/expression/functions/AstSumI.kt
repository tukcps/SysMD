package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.*
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DDBuilder.IntMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.max
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.min
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.bounds.LongMath.max
import io.github.tukcps.aadd.values.bounds.LongMath.min
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange

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
            VectorQuantity.fromCanonical(mutableListOf(model.builder.Reals.All.clone()), iteration.upQuantity.unit, iteration.upQuantity.unitSpec, iteration.upQuantity.userWantedUnitSpec)
        if (iteration.isInt) upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.All.clone()))
        downQuantity = upQuantity.clone()
    }

    override fun evalUpRec() {
        startI.evalUpRec()
        endI.evalUpRec()
        evalUp()
    }

    override fun evalUp() {
        if (startI.isInt) {
            // test if startI or endI is not defined
            if (startI.idds[0].min.isInfinite || startI.idds[0].max.isInfinite ||
                endI.idds[0].min.isInfinite || endI.idds[0].max.isInfinite) {
                upQuantity = VectorQuantity(mutableListOf(model.builder.Integers.All))
                return // no loop possible with infinite borders
            }
            upQuantity = subSumInt(startI.idds[0], endI.idds[0])
        } else { //startI is Real (see initialize)
            upQuantity = VectorQuantity.fromCanonical(mutableListOf(model.builder.real(0.0)), iteration.upQuantity.unit, iteration.upQuantity.unitSpec, iteration.upQuantity.userWantedUnitSpec)
            // test if startI or endI is not defined
            if (startI.aadds[0].min.isInfinite || startI.aadds[0].max.isInfinite || endI.aadds[0].min.isInfinite || endI.aadds[0].max.isInfinite) {
                upQuantity = VectorQuantity.fromCanonical(model.builder.Reals.All, iteration.upQuantity.unit, iteration.upQuantity.unitSpec, iteration.upQuantity.userWantedUnitSpec)
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
            if (startI.idds[0].getRange().min == LongBound.NegativeInfinity || startI.idds[0].getRange().max == LongBound.PositiveInfinity) {
                getParam(1).downQuantity = getParam(1).downQuantity.constrain(VectorQuantity(model.builder.Integers.All)) //no infinite loops
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
                getParam(1).downQuantity = getParam(1).downQuantity.constrain(VectorQuantity(
                    model.builder.integer(
                        min(endIReverseMin.min, endIReverseMax.min)..max(endIReverseMin.max, endIReverseMax.max)
                    )
                ))
            }
            if (endI.idds[0].getRange().min.isInfinite) {
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(model.builder.Integers.All)) //no infinite loops
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
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity(
                    model.builder.integer(
                        min(startIReverseMin.min, startIReverseMax.min)..max(startIReverseMin.max, startIReverseMax.max)
                    )
                ))
            }
        } else if (downQuantity.values[0] is AADD) {
            val unit = getParam(2).upQuantity.unit
            val unitSpec = getParam(2).upQuantity.unitSpec
            val userWantedUnitSpec = getParam(2).upQuantity.userWantedUnitSpec
            if (!startI.aadds[0].getRange().isFinite()) {
                getParam(1).downQuantity = getParam(1).downQuantity.constrain(VectorQuantity.fromCanonical(model.builder.Reals.All, unit, unitSpec, userWantedUnitSpec)) //no infinite loops
            } else {
                // calculate endI
                val endIReverse = reverseSumReal(
                    downQuantity.values[0] as AADD,
                    startI.aadds[0].getRange(),
                    endI.aadds[0].getRange(),
                    isEnd = true
                )
                getParam(1).downQuantity = getParam(1).downQuantity.constrain(VectorQuantity.fromCanonical(model.builder.real(endIReverse), unit, unitSpec, userWantedUnitSpec))
            }
            if (endI.aadds[0].getRange().min.isInfinite || endI.aadds[0].getRange().max.isInfinite) {
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity.fromCanonical(model.builder.Reals.All, unit, unitSpec, userWantedUnitSpec)) //no infinite loops
            } else {
                // calculate startI
                val startIReverse = reverseSumReal(
                    downQuantity.values[0] as AADD,
                    endI.aadds[0].getRange(),
                    startI.aadds[0].getRange(),
                    isEnd = false
                )
                getParam(0).downQuantity = getParam(0).downQuantity.constrain(VectorQuantity.fromCanonical(model.builder.real(startIReverse), unit, unitSpec, userWantedUnitSpec))
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
    private fun reverseSumInt(sum: LongBound, startI: LongBound, isEnd: Boolean): IntegerRange {
        var currentSum = VectorQuantity(mutableListOf(model.builder.integer(sum)))
        var i = startI
        while (currentSum.values[0].asIdd().getRange().min > 0L && i >= 0) {
            // set variable to i and evaluate iteration for it.
            require(namespace.resolveVar("i") != null)
            namespace.resolveVar("i")!!.intSpec(IntegerRange(i)).initVectorQuantity()
            iteration.evalUpRec()
            currentSum -= iteration.upQuantity
            if (isEnd)
                i = (i + 1L)!! // for sure no NaN
            else
                i = (i - 1L)!!
            if(i<0 && currentSum.values[0].asIdd().getRange().min > 0)
                i = (i - 2L) !!
                //special case, because this case does not work otherwise. i+2 is calculated before returning ??
        }
        return if (currentSum.values[0].asIdd().getRange().min eq 0L) //result exact
            if (isEnd) IntegerRange((i - 1L) !! )
            else IntegerRange((i + 1L) !! )
        else
            if (isEnd) IntegerRange((i - 2L) !!)
            else IntegerRange((i +2L) !! )
    }

    /**
     * Calculates Sum Reverse for evalDown of type Real
     * @param sum quantity for calculating the sum
     * @param startCalculation i to start iteration
     * @param maximumEndI end for calculation, because interval can ot be extended
     * @param isEnd true if the endI is calculated, if false the startI is calculated
     * @return Range for endI (isEnd true) or startI (isEndFalse)
     */
    private fun reverseSumReal(sum: AADD, startCalculation: RealRange, maximumEndI: RealRange, isEnd: Boolean): RealRange {
        val resultIList = mutableListOf<Double>()

        if(! startCalculation.isFinite())
            throw SemanticError("starting index is unbounded")
        if(! maximumEndI.isFinite())
            throw SemanticError("ending index is unbounded")

        for (i0 in roundInwards(startCalculation).values) {
            var iterator = i0
            var currentSum = VectorQuantity(listOf(sum), "?")
            while(if(isEnd) iterator <= maximumEndI.max else iterator >= maximumEndI.min) { //avoid infinite loops by maximum possible result
                // set variable to i and evaluate iteration for it.
                require(namespace.resolveVar("i") != null)
                namespace.resolveVar("i")!!.rangeSpec(RealRange(iterator.toDouble())).initVectorQuantity()
                iteration.evalUpRec()
                currentSum -= iteration.upQuantity
                if (DoubleBound.Finite(0.0) in currentSum.aadd().min..currentSum.aadd().max) {
                    resultIList.add(iterator.toDouble())
                    if (-0.0001 < currentSum.aadd().min && currentSum.aadd().max < 0.0001)
                        break
                }
                if (isEnd) iterator += 1 else iterator -= 1
            }
        }
        if (resultIList.isEmpty()) {
            // we already checked that these are finite
            resultIList.add(if (!isEnd) startCalculation.min.toDouble() else 0.0)
            resultIList.add(if (isEnd) Double.POSITIVE_INFINITY else startCalculation.max.toDouble())
        }
        resultIList.sort()
        return RealRange(resultIList[0] - 0.5, resultIList[resultIList.size - 1] + 0.5)
    }

    /**
     * Maximal SubSum for the function using Kadane's algorithm
     */
    private fun subSumReal(startI: AADD, endI: AADD): VectorQuantity {
        var maxSum = model.builder.real(0.0)
        var minSum = model.builder.real(0.0)
        var currSumMax = model.builder.real(0.0)
        var currSumMin = model.builder.real(0.0)

        for (i in roundOutwards(startI.getRange() union endI.getRange()).values) {
            // set variable to i and evaluate iteration for it.
            val vari = namespace.resolveVar("i")
            require(vari != null)
            vari.rangeSpec(RealRange(i.toDouble())).initVectorQuantity()
            iteration.evalUpRec()
            val iterationValue = iteration.upQuantity.values[0] as AADD
            currSumMax += iterationValue
            currSumMin += iterationValue
            if (i > endI.min) { // these elements are added to the sum if needed
                // maxSum = (maxSum.greaterThanOrEquals(currSumMax)).asBdd().ite(maxSum, currSumMax) ==> exponential growth of tree size
                maxSum = model.builder.real(max(maxSum.max, currSumMax.max).toDouble())
                // minSum = (minSum.lessThanOrEquals(currSumMin)).asBdd().ite(minSum, currSumMin) ==> exponential growth of tree size
                minSum = model.builder.real(min(minSum.min, currSumMin.min).toDouble())
            } else if (i <= startI.max) { // in this area the sum must start
                //maxSum reset the start of the sum (use max instead of ite -> otherwise exponential growth of tree size)
                //currSumMax = (currSumMax.greaterThanOrEquals(iterationValue)).asBdd().ite(currSumMax, iterationValue) ==> exponential growth of tree size
                currSumMax = model.builder.real(max(currSumMax.max, iterationValue.max).toDouble())
                maxSum = currSumMax
                //minSum  reset the start of the sum  (use min instead of ite -> otherwise exponential growth of tree size)
                //currSumMin = (currSumMin.lessThanOrEquals(iterationValue)).asBdd().ite(currSumMin, iterationValue) ==> exponential growth of tree size
                currSumMin = model.builder.real(min(currSumMin.min, iterationValue.min).toDouble())
                minSum = currSumMin
            } else { //between startI and endI
                //these elements are required for the sum
                //maxSum
                maxSum = currSumMax // not reset of the sum, because this area must be included in the final result
                //minSum
                minSum = currSumMin // not reset of the sum, because this area must be included in the final result
            }
        }

        return VectorQuantity.fromCanonical(model.builder.real(minSum.min..maxSum.max), iteration.upQuantity.unit, iteration.upQuantity.unitSpec, iteration.upQuantity.userWantedUnitSpec)
    }

    /**
     * Maximal SubSum for the function using Kadane's algorithm
     */
    private fun subSumInt(startI: IDD, endI: IDD): VectorQuantity {
        var maxSum = model.builder.integer(0)
        var minSum = model.builder.integer(0)
        var currSumMax = model.builder.integer(0)
        var currSumMin = model.builder.integer(0)

        val ixRange = IntegerRange(startI.min, endI.max)

        if(! ixRange.isFinite())
            throw SemanticError("index range must be finite")

        for (i in ixRange.values) {
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
        return VectorQuantity(model.builder.integer(minSum.min..maxSum.max))
    }

    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters) p.runDepthFirst(block)
        return block()
    }

    override fun clone() = AstSumI(namespace, model, cloneParameters())
}
