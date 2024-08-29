package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.model.expression.DDInternal
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.functions.intersect

/**
 * This class will be new base class for BDD,IDD and AADD analyzer. Combining unit- dontcare and other analyzers
 */
open class DDAnalyzer(override val model: Session): StructuralAnalyzerIF {
    protected val builder get() = model.builder

    private val conditionIndexes
        get() = builder.conds.x.keys

    private val units: HashMap<Variable, HashMap<Int, DD>> = hashMapOf()
    private val dontCares: HashMap<Variable, HashSet<Int>> = hashMapOf()
    private val infeasibleCombinations: HashMap<Variable, SetOfSolutions> = hashMapOf()

    fun getUnits(value: Variable) : HashMap<Int, DD> {
        return if (units[value] != null) units[value]!!
        else hashMapOf()
    }

    fun getDontCares(value: Variable) : HashSet<Int> {
        return if (dontCares[value] != null) dontCares[value]!!
        else hashSetOf()
    }

    fun getInfeasibleCombinations(value: Variable): SetOfSolutions {
        return if (infeasibleCombinations[value] != null) infeasibleCombinations[value]!!
        else SetOfSolutions()
    }

    override fun updateProperty(updatedProperty: Variable) {
        findUnits(updatedProperty)
        findDontCares(updatedProperty)
        findInfeasibility(updatedProperty)
    }

    override fun conditionInAPath(conditionIndex: Int, property: Variable): Boolean {
        return when (property.vectorQuantity.values[0]) {
            is BDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asBdd(), property.boolSpecs[0].bddLeafOf(builder))
                for (c in currentPaths) {
                    if (c.containsKey(conditionIndex)) return true
                }
                false
            }
            is IDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asIdd().evaluate(), builder.leaf(property.intSpecs[0]))
                for (c in currentPaths) {
                    if (c.containsKey(conditionIndex)) return true
                }
                false
            }
            is AADD -> {
                //FIXME: target constructe correctly?
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asAadd().evaluate(), builder.leaf(builder.interval(property.rangeSpecs[0])))
                for (c in currentPaths) {
                    if (c.containsKey(conditionIndex)) return true
                }
                false
            }
            is StrDD -> throw Exception("${property.feature.escapedName()}: StrDD not supported")
            else -> throw Exception("${property.feature.escapedName()} of unknown data type")
        }
    }

    override fun conditionInAllPaths(conditionIndex: Int, property: Variable): Boolean {
        return when (property.vectorQuantity.values[0]) {
            is BDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asBdd().evaluate(), property.boolSpecs[0].bddLeafOf(builder))
                if (currentPaths.isEmpty()) return false
                var result = true
                for (c in currentPaths) {
                    result = result && c.containsKey(conditionIndex)
                }
                result
            }
            is IDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asIdd().evaluate(), builder.leaf(property.intSpecs[0]))
                if (currentPaths.isEmpty()) return false
                var result = true
                for (c in currentPaths) {
                    result = result && c.containsKey(conditionIndex)
                }
                result
            }
            is AADD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asAadd().evaluate(), builder.leaf(builder.interval(property.rangeSpecs[0])))
                if (currentPaths.isEmpty()) return false
                var result = true
                for (c in currentPaths) {
                    result = result && c.containsKey(conditionIndex)
                }
                result
            }
            is StrDD -> throw Exception("${property.feature.escapedName()}: StrDD not supported")
            else -> throw Exception("${property.feature.escapedName()} of unknown data type")
        }
    }

    override fun getAllocationFromAllPaths(index: Int, property: Variable): DD {
        return when (property.vectorQuantity.values[0]) {
            is BDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asBdd().evaluate(), property.boolSpecs[0].bddLeafOf(builder))
                var result = builder.Bool
                for (c in currentPaths) {
                    val value = if (c[index] == true) builder.True else builder.False
                    result = result.intersect(value)
                }
                result
            }
            is IDD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asIdd().evaluate(),builder.leaf(property.intSpecs[0]))
                var result = builder.Integers
                for (c in currentPaths) {
                    val value = followPathTo(property.vectorQuantity.values[0].asIdd().evaluate(), c, index)
                    result = result.intersect(value as IDD.Leaf) as IDD.Leaf
                }
                result
            }
            is AADD -> {
                val currentPaths = findPathsTo(property.vectorQuantity.values[0].asAadd().evaluate(), builder.leaf(builder.interval(property.rangeSpecs[0])))
                var result: AADD = builder.Reals//AFReals
                for (c in currentPaths) {
                    val value = followPathTo(property.vectorQuantity.values[0].asAadd().evaluate(), c, index)
                    result = result.intersect(value.asAadd())
                }
                result
            }
            is StrDD -> throw Exception("${property.feature.escapedName()}: StrDD not supported")
            else -> throw Exception("${property.feature.escapedName()} of unknown data type")
        }
    }

    // V3.0:
    fun <T: Any> findPathsTo(dd:DDcond<T>, target: DDcond<T>, result: SetOfSolutions = SetOfSolutions(), partialSolution: Solution = Solution()): SetOfSolutions {
        if (dd.structurallyEquals(target)) { //recursion end case 1
            result.add(partialSolution)
            return result//.filter { it.isNotEmpty() } as HashSet<Solution>
        }
        else {
            if (dd is DDcond.Internal) {
                if (dd.T.containsSubDD(target)) {
                    val partialSolutionT = partialSolution.clone()
                    partialSolutionT[dd.index] = true
                    result.addAll(findPathsTo(dd.T, target, result.clone(), partialSolutionT))
                }
                if (dd.F.containsSubDD(target)) {
                    val partialSolutionF = partialSolution.clone()
                    partialSolutionF[dd.index] = false
                    result.addAll(findPathsTo(dd.F, target, result.clone(), partialSolutionF))
                }
                //Recursion end case 2: Nothing happes... result will be returned unchanged next step
            }
        }
        return result//.filter { it.isNotEmpty() } as HashSet<Solution>
    }

    private fun followPath(dd: DD, path: Solution): DD {
        return if (path.isNotEmpty() && dd is DDInternal) {
            val condition = path.remove(dd.index)!!
            if (condition) followPath(dd.T, path)
            else followPath(dd.F, path)
        } else {
            dd //we're there!
        }
    }

    private fun followPathTo(dd: DD, path: Solution, index: Int): DD {
        if (path.isNotEmpty() && path.containsKey(index) && dd is DDcond.Internal) {
            val condition = path.remove(dd.index)!!
            return if (dd.index == index) {
                if (condition) dd.T
                else dd.F
            } else {
                if (condition) followPathTo(dd.T, path, index)
                else followPathTo(dd.F, path, index)
            }
        }
        else if (!path.containsKey(index)) {
            return builder.Empty
        }
        else {
            return if (dd.index == index) dd
            else builder.Empty
        }
    }

    //FXIME: Adapt do DD instead of only BDD!
    private fun findUnits(updatedProperty: Variable) {
        when (updatedProperty.vectorQuantity.values[0]) {
            is BDD -> {
                var possibleUnitExists: Boolean
                variables@ for (i in conditionIndexes) {
                    if (builder.conds.x[i] !is BDD) continue@variables //not a bool cond
                    possibleUnitExists = conditionInAllPaths(i, updatedProperty)

                    var booleanAllocation: DD
                    if (possibleUnitExists) {
                        booleanAllocation = getAllocationFromAllPaths(i, updatedProperty) as BDD
                        if (booleanAllocation.toString() == "Contradiction") continue@variables
                        if (booleanAllocation.index == builder.True.index || booleanAllocation.index == builder.False.index) {
                            if (units.containsKey(updatedProperty)) units[updatedProperty]!![i] =
                                (booleanAllocation as BDD.Leaf)
                            else units[updatedProperty] = hashMapOf(Pair(i, booleanAllocation))
                        }
                    }
                }
            }
            is IDD -> {
                var possibleUnitExists: Boolean
                variables@ for (i in conditionIndexes) {
                    if (builder.conds.x[i] !is IDD) continue@variables
                    possibleUnitExists = conditionInAllPaths(i, updatedProperty)

                    val allocation = getAllocationFromAllPaths(i, updatedProperty) as IDD
                    if (allocation.index != builder.Empty.index && allocation.index != builder.Integers.index) {
                        if (units.containsKey(updatedProperty)) units[updatedProperty]!![i] =
                            (allocation as IDD.Leaf)
                        else units[updatedProperty] = hashMapOf(Pair(i, allocation))
                    }
                }
            }
            is AADD -> {
                var possibleUnitExists: Boolean
                variables@ for (i in conditionIndexes) {
                    if (builder.conds.x[i] !is AADD) continue@variables
                    possibleUnitExists = conditionInAllPaths(i, updatedProperty)

                    val allocation = getAllocationFromAllPaths(i, updatedProperty) as AADD
                    if (allocation.index != builder.Empty.index && allocation.index != builder.Reals.index) {
                        if (units.containsKey(updatedProperty)) units[updatedProperty]!![i] =
                            (allocation as AADD.Leaf)
                        else units[updatedProperty] = hashMapOf(Pair(i, allocation))
                    }
                }
            }
            is StrDD -> {
                //TODO!
            }
            else -> throw Exception("${updatedProperty.feature.escapedName()} of unknown data type")
        }
    }

    private fun findDontCares(updatedProperty: Variable) {
        when (updatedProperty.vectorQuantity.values[0]) {
            is BDD -> {
                var freeVarValue = builder.True
                var freeVarExists: Boolean
                var possibleUnitExists: Boolean
                variables@ for (i in conditionIndexes) {
                    if (builder.conds.x[i] !is BDD) {
                        if (conditionInAllPaths(i, updatedProperty) && getAllocationFromAllPaths(i, updatedProperty).toString() == "Contradiction") {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                    else {
                        if (conditionInAllPaths(i, updatedProperty) && getAllocationFromAllPaths(i, updatedProperty).toString() == "Contradiction") {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                }
            }
            is IDD -> {
                var freeVarValue = builder.Integers
                var freeVarExists: Boolean
                var possibleUnitExists: Boolean
                variables@ for (i in conditionIndexes) {
                    if (builder.conds.x[i] !is IDD) {
                        if (conditionInAllPaths(i, updatedProperty) && getAllocationFromAllPaths(i, updatedProperty).toString() == "Contradiction") {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                    else {//not a bool cond
                        freeVarExists = true
                        possibleUnitExists = conditionInAllPaths(i, updatedProperty)

                        freeVarExists = freeVarExists && possibleUnitExists
                        var allocation = builder.Integers
                        if (possibleUnitExists) {
                            allocation = getAllocationFromAllPaths(i, updatedProperty) as IDD.Leaf
                        }
                        if (freeVarExists) {
                            //Intersect instead of "and" so that wie get NaB as result of true x false instead of false from true and false
                            freeVarValue =
                                freeVarValue.intersect(allocation) as IDD.Leaf//and(p.getBooleanAllocationFromAllPaths(i) as BDD)
                        }

                        //TEST!
                        if (!conditionInAllPaths(i, updatedProperty) && conditionInAPath(i, updatedProperty)) {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                }
            }
            is AADD -> {
                //TODO! FIXME: Hier weiter
                var freeVarValue: DD = builder.Reals
                var freeVarExists: Boolean
                var possibleUnitExists: Boolean
                var i = conditionIndexes.minOrNull()
                variables@ while ((i != null) && (conditionIndexes.maxOrNull() != null) && (i!! < conditionIndexes.maxOrNull()!!) ) {
                    if (builder.conds.x[i] !is AADD) {
                        if (conditionInAllPaths(i, updatedProperty) && getAllocationFromAllPaths(i, updatedProperty).toString() == "Contradiction") {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                    else {//not a bool cond
                        freeVarExists = true
                        possibleUnitExists = conditionInAllPaths(i, updatedProperty)

                        freeVarExists = freeVarExists && possibleUnitExists
                        var allocation: DD = builder.Reals
                        if (possibleUnitExists) {
                            allocation = getAllocationFromAllPaths(i, updatedProperty) // as DD//as AADD//.Leaf //FIXME: CAST ERROR!
                            if (allocation !is AADD.Leaf) continue@variables
                        }
                        if (freeVarExists) {
                            //Intersect instead of "and" so that wie get NaB as result of true x false instead of false from true and false
                            freeVarValue =
                                freeVarValue.intersect(allocation) //as AADD.Leaf//and(p.getBooleanAllocationFromAllPaths(i) as BDD)
                        }

                        //TEST!
                        if (!conditionInAllPaths(i, updatedProperty) && conditionInAPath(i, updatedProperty)) {
                            if (dontCares.containsKey(updatedProperty)) dontCares[updatedProperty]!!.add(i)
                            else dontCares[updatedProperty] = hashSetOf(i)
                        }
                    }
                    i++
                }
            }
            is StrDD -> {
                //TODO!
            }
            else -> throw Exception("${updatedProperty.feature.escapedName()} of unknown data type")
        }
    }

    private fun findInfeasibility(updatedProperty: Variable) {
        val pathsToInfeasibility = findInfeasiblePaths(updatedProperty.vectorQuantity.values[0].evaluate())
        infeasibleCombinations[updatedProperty] = pathsToInfeasibility
    }

    private fun findInfeasiblePaths(dd: DD): SetOfSolutions {
        return if (dd is DDcond.Leaf && dd.isFeasible) SetOfSolutions()
        else when (dd) {
            is BDD  -> {
                findPathsTo(dd, builder.InfeasibleB)
            }
            is AADD -> {
                findPathsTo(dd, builder.Infeasible)
            }
            is IDD -> {
                findPathsTo(dd, builder.InfeasibleI)
            }
            is StrDD -> {
                findPathsTo(dd, builder.InfeasableS)
            }
            else -> throw Exception("Unknown data type") //this should catch IDDs
        }
    }
}
