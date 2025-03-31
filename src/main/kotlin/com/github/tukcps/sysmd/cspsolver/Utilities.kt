package com.github.tukcps.sysmd.cspsolver

import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.analyzer.SetOfSolutions


open class UnitMap(private val builder: DDBuilder) {
    private val unitMap: HashMap<Variable, HashMap<Int, DD<*>>> = hashMapOf()
    val keys
        get() = unitMap.keys
    val values
        get() = unitMap.values
    val entries
        get() = unitMap.entries
    private val reversed
        get() = unitMap.entries.associate { (k, v) -> v to k }

    fun getKey(element: HashMap<Int, DD<*>>) : Variable {
        return reversed[element]!!
    }

    @Suppress("UNCHECKED_CAST")
    operator fun iterator() : MutableIterator<HashMap<Variable, HashMap<Int, DD<*>>>> {
        return unitMap.iterator() as MutableIterator<HashMap<Variable, HashMap<Int, DD<*>>>>
    }

    fun update(value: Variable, units: HashMap<Int, DD<*>>, dontcares: HashSet<Int>) {
        for (e in units.keys) {
            if (dontcares.contains(e)) units[e] = XBool.X.bddLeafOf(builder)
        }
        if (units.isEmpty()) {
            for (i in dontcares) {
                units[i] = XBool.X.bddLeafOf(builder)
            }
        }
        unitMap[value] = units
    }

    operator fun get(i: Variable): HashMap<Int, DD<*>> {
        return unitMap[i] as HashMap<Int, DD<*>> /* = java.util.HashMap<kotlin.Int, com.github.tukcps.aadd.DD> */
    }

    override fun toString(): String {
        return unitMap.toString()
    }

}


/**
 * Stores paths that lead to infeasible result within Inequations
 */
open class InfeasibilityMap(private val builder: DDBuilder) {
    private val infeasibilityMap: HashMap<Variable, SetOfSolutions> = hashMapOf()
    private val reversed
        get() = infeasibilityMap.entries.associate { (k, v) -> v to k }

    val keys
        get() = infeasibilityMap.keys
    val values
        get() = infeasibilityMap.values
    val entries
        get() = infeasibilityMap.entries

    operator fun get(i: Variable): SetOfSolutions? {
        return infeasibilityMap[i]
    }

    @Suppress("UNCHECKED_CAST")
    operator fun iterator() : MutableIterator<HashMap<Variable, HashSet<HashMap<Int, DD<*>>>>> {
        return infeasibilityMap.iterator() as MutableIterator<HashMap<Variable, HashSet<HashMap<Int, DD<*>>>>>
    }

    fun update(uid: Variable, infeasiblePaths: SetOfSolutions) {
        infeasibilityMap[uid] = infeasiblePaths
    }

    fun getKey(element: SetOfSolutions): Variable {
        return reversed[element]!!
    }

    override fun toString(): String {
        return infeasibilityMap.toString()
    }
}

/**
Intended mostly as data class to store current state of the breath first search

Units: Boolean properties and their respective allocations that fullfill for conditions for the current evaluation
UnitMap: Intersection of units over all properties with the current evaluation
IntroducedProperties: Properties that have been introduced through analysis of units and unitmap
DontCares: Conditions that do not matter for the respective property (but exist in at least one path to the evaluation.
Conflicts: Possible conflicts detected up until now.

TODOs: - Reasons: Detailed reasoning for changes made from last step
 **/
open class DiscreteSolverStep(val stepNumber: Int) {
    lateinit var unitMap: UnitMap
    lateinit var infeasibilityMap: InfeasibilityMap
    val introducedProperties = mutableListOf<Variable>()
    val conflicts = hashMapOf<Int, HashSet<DD<*>>>()
    val updatedProperties = mutableListOf<Variable>()
}

open class DiscreteSolverDepth(var depthLevel: Int, var currentState: DiscreteSolverHistoryTreeNode) {
    //lateinit var currentState: DiscreteSolverHistoryTreeNode
    lateinit var tantativeStates: MutableSet<DiscreteSolverHistoryTreeNode>
}


data class DiscreteSolverHistoryTreeNode(val id: Int, val parent: DiscreteSolverHistoryTreeNode?) {
    val assignments = mutableSetOf<MutableMap<Int, DD<*>>>()
    val noGoods = mutableSetOf<Pair<Int, DD<*>>>()
    val children = mutableSetOf<DiscreteSolverHistoryTreeNode>()
}

//enum class Trail {
//    FREE, GUESSED, RELATED, NOGOOD
//}