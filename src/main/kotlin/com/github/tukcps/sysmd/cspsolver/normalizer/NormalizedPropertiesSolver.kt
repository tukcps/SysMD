package com.github.tukcps.sysmd.cspsolver.normalizer

import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.values.XBool

/**
 * Class to solve the normalized properties [NormalizedProperties] produced by the [CNNormalizer]
 */
class NormalizedPropertiesSolver {

    private var debugMode = true
    private var onlyOneAssignment = false

    /**
     * Solve a normalized constraint net
     *
     * @param normalizedProperties the normalized properties as [NormalizedProperties]
     * @return A set of all satisfying assignments, each represented by a set of integers,
     * whereby the integers represent the literals (negative value means negation)
     * and are chosen as it was given by the [NormalizedProperties.model]
     * to represent the variables by their according numbers.
     */
    fun solveNormalizedCN(normalizedProperties: NormalizedProperties) : Set<Set<Int>> {

        if(debugMode) {
            debugPrint("********************************** Start solving the normalized CN **********************************")
        }

        // extract the variable name and the according number representing it of each variable
        /*
        val variables = normalizedProperties.model.builder.conds.indexes
        if(debugMode) {
            val amountOfVars = variables.size
            debugPrint("amount of variables: $amountOfVars")
            debugPrint("variables:")
            for(variable in variables.keys) {
                debugPrint("\t$variable represented by ${variables[variable]}")
            }
        }
        */

        // determine the satisfying assignments
        val props = normalizedProperties.properties

        // extract the relevant properties (remove variable declarations and store them in map variables)
        val variables = HashMap<Int, String>()
        //val variables2 = HashMap<String, DD<*>>()
        for(prop in props.reversed()) {
            if(debugMode) {
                debugPrint(prop.toString())
            }
            // variable declaration found
            if(prop.expression.isEmpty()) {
                // println("remove prop: $prop")
                props.remove(prop)
                variables[prop.dd.index] = prop.name
                //variables2[prop.name] = prop.dd
            }
        }
        if(debugMode) {
            val amountOfVars = variables/*2*/.size//variables.size
            debugPrint("amount of variables: $amountOfVars")
            debugPrint("variables:")
            for(variable in variables.keys) {
                debugPrint("\t${variables[variable]} represented by $variable")
            }
        }

        val cnf = createCNF(props).toList()

        val solver = CDCL()
        solver.deactivateDebugMode()
        val assignments : Set<Set<Int>> = when (props.size) {
            0 -> {
                // println("No properties to be solved!")
                return emptySet()
            }

            1 -> {
                if(debugMode) {
                    debugPrint("Only one property to be solved: getting assignments directly from BDD")
                }
                getAssignmentsFromBDD(props[0].dd.asBdd())
            }

            else -> {
                if(debugMode) {
                    debugPrint("More than one property to be solved: using CDCL")
                    solver.activateDebugMode()
                }
                if(onlyOneAssignment) {
                    solver.findOnlyOneAssignment()
                } else {
                    solver.findAllAssignments()
                }
                solver.cdcl(cnf)
            }
        }

        // print the result
        // printResult(assignments, variables)

        return assignments

    }

    /**
     * Print the result from solving the normalized CN. If it is satisfiable, all satisfying assignments are printed.
     *
     * @param assignments A set of all satisfying assignments, each represented by a set of integers,
     * whereby the integers represent the literals (negative value means negation)
     * and are chosen as it was given in the parameter [variables]
     * to represent the variables with their according numbers.
     * @param variables mapping each variable name onto the integer, which represents the variable
     */
    private fun printResult(assignments: Set<Set<Int>>, variables: HashMap<Int, String>) {
        if(assignments.isNotEmpty()) {
            println("************** Satisfiable with following partial Assignments: **************")
            for((i, asg) in assignments.withIndex()) {
                print("Assignment $i: (")
                for((k,literal) in asg.withIndex()) {
                    if(k>0) { print(", ") }
                    //val varName = variables.filterValues { it == literal.absoluteValue }.keys
                    val varName : String = if(literal in variables.keys) {
                        variables[literal].toString()
                    } else if(-1*literal in variables.keys) {
                        variables[-1*literal].toString()
                    } else {
                        throw RuntimeException("Assigned literal $literal is not contained in the variables.")
                    }
                    val assignedValue = literal>0
                    print("$varName = $assignedValue")
                }
                print(")")
                println()
            }
        } else {
            println("************** Unsatisfiable **************")
        }
    }

    /**
     * Get the satisfying assignments from a given BDD.
     *
     * @param bdd the given BDD
     * @return A set of all satisfying assignments, each represented by a set of integers,
     * whereby the integers represent the literals (negative value means negation)
     */
    private fun getAssignmentsFromBDD(bdd: BDD): MutableSet<MutableSet<Int>> {

        when (bdd) {
            is BDD.Internal -> {

                val topAssignments = getAssignmentsFromBDD(bdd.T)
                val downAssignments = getAssignmentsFromBDD(bdd.F)

                // add the current looked at variable to the assignments from the top child
                topAssignments.forEach {
                    it.add(bdd.index)
                }
                // add the current looked at variable negated to the assignments from the down child
                downAssignments.forEach {
                    it.add((-1*bdd.index))
                }

                // return all assignments from top and down child with the current variable added which are leading to true
                return topAssignments.union(downAssignments) as MutableSet<MutableSet<Int>>

            }

            is BDD.Leaf -> {
                return when (bdd.value) {
                    XBool.True -> {
                        // add an empty set for a path leading to true
                        mutableSetOf(mutableSetOf())
                    }

                    XBool.False -> {
                        // path not leading to true gives no satisfying assignment -> do not add a set
                        mutableSetOf()
                    }

                    else -> {
                        mutableSetOf()//throw RuntimeException("BDD Leaf with unexpected value.")
                    }
                }
            }
        }
    }

    /**
     * Get the CNF from a single given BDD
     *
     * @param bdd the given BDD
     * @return CNF represented as a set of sets of integers, whereby the integers represent the literals (negative value means negation).
     * Each 'inner' set of integers represents a clause.
     */
    private fun getCNFFromBDD(bdd: BDD) : MutableSet<MutableSet<Int>> {

        when (bdd) {
            is BDD.Internal -> {

                val topCNF = getCNFFromBDD(bdd.T)
                val downCNF = getCNFFromBDD(bdd.F)

                // use (x => a | b) <=> ((x -> a) and (-x -> b)) <=> (-x or a) and (x or b)
                // add the current looked at variable negated to each clause of the CNF from the top child
                topCNF.forEach {
                    it.add((-1*bdd.index))
                }
                // add the current looked at variable to each clause of the CNF from the down child
                downCNF.forEach {
                    it.add(bdd.index)
                }

                return topCNF.union(downCNF) as MutableSet<MutableSet<Int>>

            }

            is BDD.Leaf -> {
                return when (bdd.value) {
                    XBool.True -> {
                        mutableSetOf()
                    }

                    XBool.False -> {
                        mutableSetOf(mutableSetOf())
                    }

                    else -> {
                        mutableSetOf(mutableSetOf())//throw RuntimeException("BDD Leaf with unexpected value.")
                    }
                }
            }
        }
    }

    /**
     * Create a CNF (Conjunctive Normal Form) out of the DDs (Decision Diagrams) of the given normalized properties
     *
     * @param normalizedProps list of the normalized properties
     * @return CNF represented as a set of sets of integers, whereby the integers represent the literals (negative value means negation).
     * Each 'inner' set of integers represents a clause.
     */
    private fun createCNF(normalizedProps: MutableList<SimpleProperty<XBool>>) : Set<Set<Int>> {

        var cnf = mutableSetOf<Set<Int>>()
        // build the conjunction of the CNFs of all given properties
        for(prop in normalizedProps) {
            cnf = cnf.union(getCNFFromBDD(prop.dd.asBdd())) as MutableSet<Set<Int>>
        }

        if(debugMode) {
            var msg = "Created CNF:\t("
            for((i,clause) in cnf.withIndex()) {
                if(i>0) { msg = "$msg) & (" }
                for((k,literal) in clause.withIndex()) {
                    if(k>0) { msg = "$msg | " }
                    msg = "$msg$literal"
                }
            }
            msg = "$msg)"
            debugPrint(msg)
        }

        return cnf
    }

    /**
     * Print a debug message
     *
     * @param message the message to be printed in the debug format
     */
    private fun debugPrint(message: String) {
        println("[DEBUG] $message")
    }

    /**
     * Activate the debug mode (additional outputs)
     */
    fun activateDebugMode() {
        debugMode = true
    }

    /**
     * Deactivate the debug mode (additional outputs)
     */
    fun deactivateDebugMode() {
        debugMode = false
    }

    /**
     * Activates the mode in which the solver searches only for one satisfying assignments and stops if it is found.
     * If all satisfying assignments are wanted, use [findAllAssignments].
     */
    fun findOnlyOneAssignment() {
        onlyOneAssignment = true
    }

    /**
     * Activates the mode in which the solver searches for all satisfying assignments.
     * If only one satisfying assignment is wanted (i.e. if the formula is satisfiable), use [findOnlyOneAssignment].
     */
    fun findAllAssignments() {
        onlyOneAssignment = false
    }

}