package com.github.tukcps.sysmd.cspsolver.normalizer

class CDCL {

    private var debugMode = false
    private var onlyOneAssignment = false

    /**
     * Data class representing a tuple of three counters.
     *
     * @param size the amount of literals contained in the clause
     * @param asgTrue the amount of literals already assigned true of the clause
     * @param asgFalse the amount of literals already assigned false of the clause
     */
    private data class Counters(
        var size: Int,
        var asgTrue: Int,
        var asgFalse: Int
    )

    /**
     * Data class representing the current formula by three maps and the trail,
     * whereby the latter represents the current deduced/chosen assignments.
     * Each clause is represented by its index. Literals are represented by integers.
     *
     * @param f mapping the index of each clause to a set of its literals
     * @param lc mapping each literal to a set of the clauses it is contained in
     * @param counters mapping the index of each clause to its size, true assigned literals, and false assigned literals
     * @param trail trail consisting of triples (literal, 'g' for guessed or 'd' for deduced, index of the clause it was deduced of)
     */
    private data class CurrentFormula(
        val f: MutableMap<Int, Set<Int>>,
        val lc: MutableMap<Int, Set<Int>>,
        val u : MutableList<Int>,
        val counters: MutableMap<Int, Counters>,
        val trail: MutableList<Triple<Int, Char, Int?>>
    )

    /**
     * Data class to store the current formula with information whether there is a conflict.
     * If there is a conflict, then the [conflictClause] stores the clause which is unsatisfiable with the current trail.
     *
     * @param currentFormula the current formula represented by three maps and the trail
     * @param conflict true, if a clause has become unsatisfiable
     * @param conflictClause index of the clause which is now unsatisfiable
     */
    private data class CurrentFormulaWithConflictInfo(
        val currentFormula: CurrentFormula,
        val conflict: Boolean,
        val conflictClause: Int?)

    /**
     * Use Clause-driven Conflict Learning (CDCL) algorithm to get all solutions for the given normalized properties.
     * It combines iterative DPLL with backward resolution.
     *
     * @param cnf conjunctive normal form represented by a list of sets of integers (sets = clauses, integers = literals)
     * @return A set of all satisfying assignments, each represented by a set of integers,
     * whereby the integers represent the literals (negative value means negation).
     */
    fun cdcl(cnf: List<Set<Int>>) : Set<Set<Int>> {

        if(debugMode) {
            debugPrint("************** Starting CDCL **************")
        }

        if(cnf.isEmpty()) {
            if(debugMode) {
                debugPrint("An empty formula was given to CDCL")
            }
            return emptySet()
        }

        var currentFormula = initializeDataStructures(cnf)

        val satAssignments : MutableSet<Set<Int>> = mutableSetOf()
        var indexForNextNewClause = cnf.size

        while (true) {

            val currentFormulaWithConflictInfo = iterativeDPLL(currentFormula)
            currentFormula = currentFormulaWithConflictInfo.currentFormula

            // no conflict means a satisfying assignment was found
            if(!currentFormulaWithConflictInfo.conflict) {

                val assignment = getAssignmentFromTrail(currentFormula.trail)
                if(debugMode) {
                    debugPrint("Iterative DPLL has found a satisfying assignment: $assignment")
                }
                satAssignments.add(assignment)
                if(onlyOneAssignment) {
                    return satAssignments
                }
                if(debugMode) {
                    debugPrint("Determined satisfying assignment #${satAssignments.size}")
                }

                // exclude this satisfying assignment for the next runs by adding the clause with all literals negated
                val excludeAsgClause = buildNegatedClauseFromAssignment(assignment)
                if(debugMode) {
                    debugPrint("Excluding found assignment by adding clause $excludeAsgClause")
                }
                currentFormula = addNewClauseToFormula(currentFormula, indexForNextNewClause, excludeAsgClause)
                indexForNextNewClause++

                // prepare the formula for restarting with the found assignment excluded
                while(currentFormula.trail.isNotEmpty()) {
                    val trailElem = currentFormula.trail.last()
                    currentFormula = removeTrailElemAndUpdateFormula(currentFormula, trailElem)
                }

            }

            // a conflict was found
            else {

                // a conflict at decision level 0 means a resolution proof is found that the current formula is unsatisfiable
                if(getCurrentTrailLevel(currentFormula) == 0) {
                    if(debugMode) {
                        debugPrint("Conflict at decision level 0")
                    }
                    return satAssignments
                }

                if(debugMode) {
                    val conflictClause : Int = currentFormulaWithConflictInfo.conflictClause!!
                    debugPrint("Iterative DPLL has found a conflict clause: clause #$conflictClause " + currentFormula.f[conflictClause].toString())
                    debugPrint("Start backward resolution")
                }

                // add the clause for the back jump
                val backJumpClause = backwardResolution(currentFormulaWithConflictInfo)
                if(debugMode) {
                    debugPrint("Trail before backtracking: ${currentFormula.trail}")
                    debugPrint("backJumpClause: $backJumpClause")
                }

                currentFormula = addNewClauseToFormula(currentFormula, indexForNextNewClause, backJumpClause)
                indexForNextNewClause++

                // backJump using the backJumpClause from the backward resolution
                currentFormula = backJump(currentFormula, backJumpClause)

                if(debugMode) {
                    debugPrint("Backtracked trail: " + currentFormula.trail.toString())
                }

            }
        }

    }

    /**
     * Use the iterative DPLL (Davis-Putnam-Logemann-Loveland) algorithm
     *
     * @param formula the current formula represented by three maps and the trail
     * @return the updated formula, as well as if there was a conflict and the index of the conflict clause
     */
    private fun iterativeDPLL(formula: CurrentFormula) : CurrentFormulaWithConflictInfo {

        if(debugMode) {
            debugPrint("************** Starting iterative DPLL **************")
        }

        var currentFormulaWithConflictInfo = CurrentFormulaWithConflictInfo(formula, false, -1)

        while(true) {

            // apply BCP
            currentFormulaWithConflictInfo = bcp(currentFormulaWithConflictInfo.currentFormula)

            // stop if conflict
            if(currentFormulaWithConflictInfo.conflict) {
                return currentFormulaWithConflictInfo
            }

            // apply Pure
            currentFormulaWithConflictInfo = CurrentFormulaWithConflictInfo(pure(currentFormulaWithConflictInfo.currentFormula), false, -1)

            // stop if formula is already satisfied
            if(allClausesSatisfied(currentFormulaWithConflictInfo.currentFormula)) {
                return currentFormulaWithConflictInfo
            }

            // case split (guess a literal)
            val guessedLiteral : Int = guessLiteral(currentFormulaWithConflictInfo.currentFormula) ?: throw RuntimeException("No literal to guess could be found!")
            val trailElem = Triple<Int, Char, Int?>(guessedLiteral, 'g', null)
            currentFormulaWithConflictInfo = addTrailElemAndUpdateFormula(currentFormulaWithConflictInfo.currentFormula, trailElem)

        }

    }

    /**
     * Applying the BCP (Boolean Constraint Propagation)
     *
     * @param currentFormula the current formula represented by three maps and the trail
     * @return the updated formula, as well as if there was a conflict and the index of the conflict clause
     */
    private fun bcp(currentFormula: CurrentFormula) : CurrentFormulaWithConflictInfo {

        var currentFormulaWithConflictInfo = CurrentFormulaWithConflictInfo(currentFormula, false, null)
        val counters = currentFormulaWithConflictInfo.currentFormula.counters
        val unitClauses = currentFormula.u.toMutableList()
        var unitClausesIterator = unitClauses.listIterator()
        var newUnitFound = false

        while(unitClauses.isNotEmpty()) {

            if(newUnitFound) {
                unitClausesIterator = unitClauses.listIterator()
            }
            val unitClauseIndex : Int = unitClausesIterator.next()

            unitClausesIterator.remove()

            if(counters[unitClauseIndex]!!.asgTrue > 0) {
                if(debugMode) {
                    val f = currentFormulaWithConflictInfo.currentFormula.f
                    debugPrint("An already satisfied clause is contained in the list of unit clauses (clause $unitClauseIndex: ${f[unitClauseIndex]}). This clause is skipped.")
                }
                continue
            }

            // find the unit literal by removing all false assigned ones (true assigned would mean satisfied)
            var clauseLiterals = currentFormulaWithConflictInfo.currentFormula.f[unitClauseIndex]!!.toSet()
            val trail = currentFormulaWithConflictInfo.currentFormula.trail
            for(trailElem in trail) {
                // none of the literals is true, otherwise the clause would already be satisfied
                // => search only for negated version of literals of the clause in trail
                if(clauseLiterals.contains((-1*trailElem.first))) {
                    clauseLiterals = clauseLiterals.minus(-1*trailElem.first)
                }
            }

            if(clauseLiterals.size != 1) {
                if(debugMode) {
                    val f = currentFormulaWithConflictInfo.currentFormula.f
                    debugPrint("Found unit clause: clause #$unitClauseIndex containing literal $clauseLiterals (initially: ${f[unitClauseIndex]})")
                    debugPrint("unit clause size: ${counters[unitClauseIndex]!!.size}, true: ${counters[unitClauseIndex]!!.asgTrue}, false: ${counters[unitClauseIndex]!!.asgFalse}")
                }
                throw RuntimeException("Unit clause detected, but amount of not assigned literals in this clause is ${clauseLiterals.size}")
            }

            if(debugMode) {
                debugPrint("Unit clause literal found: ${clauseLiterals.first()}\t(from clause #$unitClauseIndex)")
            }

            val unitLiteral = clauseLiterals.first()
            val trailElem = Triple(unitLiteral, 'd', unitClauseIndex)

            // update counters and trail
            currentFormulaWithConflictInfo = addTrailElemAndUpdateFormula(currentFormula, trailElem)

            // add new generated unit clauses
            val newUnitClauses = currentFormulaWithConflictInfo.currentFormula.u.minus(unitClauses.toSet())
            if(newUnitClauses.isNotEmpty()) {
                newUnitFound = true
                for(newUnitClauseIndex in newUnitClauses) {
                    if(!unitClauses.contains(newUnitClauseIndex)) {
                        unitClausesIterator.add(newUnitClauseIndex)
                    }
                }
                if(debugMode) {
                    debugPrint("New unit clauses found and added, unitClauses = $unitClauses")
                }
            }

            // check if a conflict was found
            if(currentFormulaWithConflictInfo.conflict) {
                return currentFormulaWithConflictInfo
            }

        }

        return currentFormulaWithConflictInfo
    }

    /**
     * Applying the Pure rule, which means that a literal occurs either only positive or only negative in the CNF
     *
     * @param formula data structure of three maps and the trail representing the current formula
     * @return the updated formula
     * */
    private fun pure(formula: CurrentFormula) : CurrentFormula {

        var currentFormula = formula
        var foundPureLiteral = true

        while(foundPureLiteral) {

            val lc = currentFormula.lc
            val counters = currentFormula.counters
            val trail = currentFormula.trail

            foundPureLiteral = false
            var pureLiteral : Int

            for(literal in lc.keys) {

                // check if the variable of the literal was already assigned a value
                // (if so, it is not pure, since it does not occur anymore in the current formula)
                var alreadyAssigned = false
                for(trailElem in trail) {
                    if(trailElem.first == literal || trailElem.first == (-1*literal)) {
                        alreadyAssigned = true
                        break
                    }
                }
                if(alreadyAssigned) {
                    continue
                }

                //get all not yet satisfied clauses containing the literal or its negation
                val isNotYetSat: (Int) -> Boolean = { counters[it]!!.asgTrue == 0 }
                val notYetSatClausesLiteral = lc[literal]!!.filter{isNotYetSat(it)}.toSet()
                var notYetSatClauses = notYetSatClausesLiteral
                var notYetSatClausesNegativeLiteral: Set<Int> = setOf()
                if(lc.keys.contains(-1*literal)) {
                    notYetSatClausesNegativeLiteral = lc[-1*literal]!!.filter{isNotYetSat(it)}.toSet()
                    notYetSatClauses = notYetSatClauses.union(notYetSatClausesNegativeLiteral)
                }

                // if a pure literal gets found, store one of the clauses it can be deduced from
                val deducedFromClause : Int
                // if all clauses containing the literal or its negation are already satisfied, the variable needs no assignment
                if(notYetSatClauses.isEmpty()) {
                    continue
                } else {
                    // only clauses containing the negative literal are not yet satisfied
                    if(notYetSatClausesLiteral.isEmpty()) {
                        foundPureLiteral = true
                        pureLiteral = -1*literal
                        deducedFromClause = notYetSatClausesNegativeLiteral.first()
                    }
                    // only clauses containing the positive literal are not yet satisfied
                    else if (notYetSatClausesNegativeLiteral.isEmpty()) {
                        foundPureLiteral = true
                        pureLiteral = literal
                        deducedFromClause = notYetSatClausesLiteral.first()
                    }
                    // clauses containing the positive literal and clauses containing the negative literal are not yet satisfied -> not pure
                    else {
                        continue
                    }
                }

                // this point only gets reached if foundPure == true
                if(debugMode) {
                    debugPrint("Pure literal found: $pureLiteral")
                }

                // add the pure literal to the trail and update the formula
                val trailElem = Triple(pureLiteral, 'd', deducedFromClause)
                val currentFormulaWithConflictInfo = addTrailElemAndUpdateFormula(currentFormula, trailElem)
                currentFormula = currentFormulaWithConflictInfo.currentFormula

                // since the satisfied clauses changed, check the literals again from the beginning
                break

            }

        }

        return currentFormula
    }

    /**
     * Choose a literal to be set to true to continue the search for a satisfying assignment.
     * Currently, it is chosen by searching the literal (or one of the literals)
     * occurring in the most not yet satisfied clauses.
     *
     * @param currentFormula the current formula represented by three maps and the trail
     * @return The literal to be set to true represented by an integer (negative means negated one).
     * If null is returned, no literal to be guessed could be determined.
     */
    private fun guessLiteral(currentFormula: CurrentFormula): Int? {

        val lc = currentFormula.lc
        val counters = currentFormula.counters
        val trail = currentFormula.trail

        var maxAmountOfClauses = 0
        var currentMaxLiteral : Int? = null

        // find the literal (or one of the literals) occurring in the most not yet satisfied clauses
        for(literal in lc.keys) {
            var alreadySet = false
            for(trailElem in trail) {
                if(trailElem.first == literal || trailElem.first == (-1*literal)) {
                    alreadySet = true
                    break
                }
            }
            if(alreadySet) {
                continue
            } else {
                var amountOfUnsatisfiedClauses = 0
                for(clauseIndex in lc[literal]!!) {
                    if(counters[clauseIndex]!!.asgTrue == 0) {
                        amountOfUnsatisfiedClauses++
                    }
                }
                if(amountOfUnsatisfiedClauses > maxAmountOfClauses) {
                    maxAmountOfClauses = amountOfUnsatisfiedClauses
                    currentMaxLiteral = literal
                }
            }
        }

        if(debugMode) {
            debugPrint("Choosing to guess literal $currentMaxLiteral, which is contained in $maxAmountOfClauses clauses.")
        }

        return currentMaxLiteral
    }

    /**
     * Applied on a formula with a conflict, it determines the clause for [backJump].
     * The idea is to construct a partial conflict graph backwards.
     *
     * @param currentFormulaWithConflictInfo the current formula as well as the information about the conflict
     * @return the clause for [backJump] represented as a set of integers
     */
    private fun backwardResolution(currentFormulaWithConflictInfo: CurrentFormulaWithConflictInfo) : Set<Int> {

        if(!currentFormulaWithConflictInfo.conflict) {
            throw RuntimeException("Backward resolution was called even though no conflict occurred!")
        }

        val currentFormula = currentFormulaWithConflictInfo.currentFormula
        val f = currentFormula.f
        // copy the values into a new mutable list for the modifications to not modify the original values
        val trailToModify = currentFormula.trail.toMutableList()
        val conflictClauseIndex = currentFormulaWithConflictInfo.conflictClause
        var conflictClause = f[conflictClauseIndex]
        var currentLevelLiteralsNegated = getCurrentTrailLevelLiterals(currentFormula).map { -1*it }

        while(conflictClause!!.intersect(currentLevelLiteralsNegated.toSet()).size > 1) {
            val trailElem = trailToModify.removeLast()
            val resolveLiteral = trailElem.first
            // if a guessed literal is reached, determine the literals of the new reached decision level
            if(trailElem.second == 'g') {
                currentLevelLiteralsNegated = getCurrentTrailLevelLiterals(CurrentFormula(mutableMapOf(), mutableMapOf(), mutableListOf(), mutableMapOf(), trailToModify)).map { -1*it }
            }
            // only resolve if the negation of the literal is contained in the current conflict clause
            if(conflictClause.contains((-1*resolveLiteral))) {
                val clauseToResolveWithIndex = trailElem.third
                val clauseToResolveWith = f[clauseToResolveWithIndex]
                conflictClause = resolve(conflictClause, clauseToResolveWith!!, resolveLiteral)
            }
        }

        return conflictClause
    }

    /**
     * Backtrack up to (but excluding) the second-highest decision level in the given [backJumpClause].
     * (The 1-UIP has the highest decision level in the [backJumpClause])
     *
     * @param formula data structure of three maps and the trail representing the current formula
     * @param backJumpClause the clause determined by [backwardResolution] after a conflict occurred
     * @return the updated formula after backtracking
     */
    private fun backJump(formula: CurrentFormula, backJumpClause: Set<Int>) : CurrentFormula {

        var currentFormula = formula
        var finished = false
        var atHighestLevel = false
        var checkForBacktrackLevel = false
        var currTrailLevelLiterals : MutableSet<Int>
        val negatedBackJumpClauseLiterals = backJumpClause.map { it*-1 }.toSet()

        while(!finished) {
            // no guessed literals left in the trail
            if(getCurrentTrailLevel(currentFormula) == 0) {
                break
            }
            val trail = currentFormula.trail
            val trailElem = trail.last()
            currentFormula = removeTrailElemAndUpdateFormula(currentFormula, trailElem)

            // removed last element of the trail
            if(trail.isEmpty()) {
                break
            }
            // highest level of a literal of the backJumpClause reached
            if(!atHighestLevel && !checkForBacktrackLevel && (trailElem.first in negatedBackJumpClauseLiterals)) {
                atHighestLevel = true
            }
            // in the next iterations the first found literal of the backJumpClause is the one with the second-highest decision level
            if(atHighestLevel && trailElem.second == 'g') {
                checkForBacktrackLevel = true
                atHighestLevel = false
                currTrailLevelLiterals = getCurrentTrailLevelLiterals(currentFormula)
                // the next level already is the second-highest of the backJumpClause => stop backJump
                if(currTrailLevelLiterals.intersect(negatedBackJumpClauseLiterals).isNotEmpty()) {
                    finished = true
                } else {
                    continue
                }
            }
            // last element of current trail level reached and check if backtrack level is reached
            if(checkForBacktrackLevel && trailElem.second == 'g') {
                currTrailLevelLiterals = getCurrentTrailLevelLiterals(currentFormula)
                // the next level already is the second-highest of the backJumpClause => stop backJump
                if(currTrailLevelLiterals.intersect(negatedBackJumpClauseLiterals).isNotEmpty()) {
                    finished = true
                }
            }
        }

        return currentFormula
    }

    /**
     * Resolve two clauses with respect to a given literal
     *
     * @param c1 one of the clauses to be resolved
     * @param c2 one of the clauses to be resolved
     * @param resolveLiteral the literal for the resolution
     * @return resolvent of [c1] and [c2] w.r.t. [resolveLiteral]
     */
    private fun resolve(c1: Set<Int>, c2: Set<Int>, resolveLiteral: Int) : Set<Int> {

        var c = c1.union(c2)
        if(resolveLiteral in c && (-1*resolveLiteral) in c) {
            c = c.minus(resolveLiteral)
            c = c.minus((-1*resolveLiteral))
        }
        return c
    }

    /**
     * Initialize the data structures that represent the formula by a given CNF.
     *
     * @param cnf The formula given in conjunctive normal form (CNF).
     * It is represented by a list of the clauses.
     * Each clause is a set containing integers representing the literals.
     * @return the initialized data structures representing the formula
     */
    private fun initializeDataStructures(cnf: List<Set<Int>>): CurrentFormula {
        // maps to represent the formula, each clause is represented by its index
        val f = mutableMapOf<Int, Set<Int>>()  // clause -> literals
        val lc = mutableMapOf<Int, Set<Int>>()  // literal -> clauses
        val u = mutableListOf<Int>()  // indexes of unit clauses
        // map to represent three counters for each clause
        val counters = mutableMapOf<Int, Counters>() // clause -> (size, assigned true, assigned false)

        // initialize the maps
        for((i,clause) in cnf.withIndex()) {
            counters[i] = Counters(size = clause.size, asgTrue = 0, asgFalse = 0)
            if(clause.size == 1) {
                u.add(i)
            }
            f[i] = clause as MutableSet<Int>
            for(literal in clause) {
                if(lc.containsKey(literal)) {
                    lc[literal] = lc[literal]!!.plus(i)
                } else {
                    lc[literal] = setOf(i)
                }
            }
        }

        return CurrentFormula(f, lc, u, counters, mutableListOf())
    }

    /**
     * Build the clause containing the negation of each literal from the given assignment
     *
     * @param assignment a set of integers representing the literals set to true in an assignment
     * @return the clause containing for each literal of the [assignment] its negation
     */
    private fun buildNegatedClauseFromAssignment(assignment: Set<Int>): Set<Int> {
        return assignment.map { it*-1 }.toSet()
    }

    /**
     * Extract the assignment from a given trail
     *
     * @param trail trail consisting of triples (literal, 'g' for guessed or 'd' for deduced, index of the clause it was deduced of)
     * @return Assignment represented by a set of integers, whereby the integers represent the literals set to true
     * (negative value means the negation is set to true, so the variable need to be set to false)
     */
    private fun getAssignmentFromTrail(trail: MutableList<Triple<Int, Char, Int?>>): Set<Int> {
        var assignment = setOf<Int>()
        for(triple in trail) {
            assignment = assignment.plus(triple.first)
        }
        return assignment
    }

    /**
     * Get a set of the literals, which have the current highest level in the trail.
     *
     * @param currentFormula data structure of three maps and the trail representing the current formula
     * @return set of literals, which have the current highest level in the trail
     */
    private fun getCurrentTrailLevelLiterals(currentFormula: CurrentFormula) : MutableSet<Int> {

        val trail = currentFormula.trail

        val currentLevelLiterals = mutableSetOf<Int>()

        for(trailElem in trail.reversed()) {
            currentLevelLiterals.add(trailElem.first)
            if(trailElem.second == 'g') {
                break
            }
        }

        return currentLevelLiterals
    }

    /**
     * Check whether all clauses and therefore the whole CNF is already satisfied.
     *
     * @param currentFormula the current formula represented by three maps and the trail
     * @return true, if all clauses of the given formula are satisfied, false otherwise
     */
    private fun allClausesSatisfied(currentFormula: CurrentFormula) : Boolean {
        var foundNotYetSatClause = false
        for(clauseIndex in currentFormula.counters.keys) {
            if(currentFormula.counters[clauseIndex]!!.asgTrue == 0) {
                foundNotYetSatClause = true
                break
            }
        }
        return !foundNotYetSatClause
    }

    /**
     * Add the [trailElem] to the trail (assign the literal true) and update the formula accordingly
     *
     * @param formula the current formula represented by three maps and the trail
     * @param trailElem the triple which will be added to the trail of the form
     * (literal, 'g' for guessed or 'd' for deduced, index of the clause it was deduced of)
     * @return the updated formula, as well as if there was a conflict and the index of the conflict clause
     */
    private fun addTrailElemAndUpdateFormula(formula: CurrentFormula, trailElem: Triple<Int, Char, Int?>) : CurrentFormulaWithConflictInfo {

        val f = formula.f
        val lc = formula.lc
        val u = formula.u
        val counters = formula.counters
        val trail = formula.trail

        // adjust the trail
        trail.add(trailElem)

        val literal = trailElem.first

        var conflict = false
        var conflictClause : Int? = null

        // adjust the counters
        for(index in lc[literal]!!) {
            counters[index]!!.asgTrue++
            if(counters[index]!!.asgTrue + counters[index]!!.asgFalse > counters[index]!!.size) {
                throw RuntimeException("Error while adjusting the counters for an added trail element.\ncounters[$index].asgTrue = ${counters[index]!!.asgTrue} \n" +
                        "counters[$index].asgFalse = ${counters[index]!!.asgFalse} \n" +
                        "counters[$index].size = ${counters[index]!!.size}")
            }
            if(u.contains(index)) {
                u.remove(index)
            }
        }
        if(lc.keys.contains(-1*literal)) {
            for(index in lc[-1*literal]!!) {
                counters[index]!!.asgFalse++
                if(counters[index]!!.asgTrue + counters[index]!!.asgFalse > counters[index]!!.size) {
                    throw RuntimeException("Error while adjusting the counters for an added trail element.\ncounters[$index].asgTrue = ${counters[index]!!.asgTrue} \n" +
                            "counters[$index].asgFalse = ${counters[index]!!.asgFalse} \n" +
                            "counters[$index].size = ${counters[index]!!.size}")
                }
                if(counters[index]!!.size == counters[index]!!.asgFalse) {
                    conflict = true
                    conflictClause = index
                }
                if((counters[index]!!.size - counters[index]!!.asgFalse) == 1 && counters[index]!!.asgTrue == 0) {
                    u.add(index)
                }
            }
        }

        return CurrentFormulaWithConflictInfo(CurrentFormula(f, lc, u, counters, trail), conflict, conflictClause)
    }

    /**
     * Remove the [trailElem] from the trail (undo the assignment) and update the formula accordingly
     *
     * @param formula the current formula represented by three maps and the trail
     * @param trailElem the triple which will be removed from the trail of the form
     * (literal, 'g' for guessed or 'd' for deduced, index of the clause it was deduced of)
     * @return the updated formula
     */
    private fun removeTrailElemAndUpdateFormula(formula: CurrentFormula, trailElem: Triple<Int, Char, Int?>) : CurrentFormula {

        val f = formula.f
        val lc = formula.lc
        val u = formula.u
        val counters = formula.counters
        val trail = formula.trail

        if(trail.last() != trailElem) {
            throw RuntimeException("The requested element to be removed from the trail is not the last one! " +
                    "Please remove trail elements only in reversed order of the trail.")
        }
        // adjust the trail
        trail.removeLast()

        val literal = trailElem.first

        // adjust the counters
        for(index in lc[literal]!!) {
            counters[index]!!.asgTrue--
            if(counters[index]!!.asgTrue < 0) {
                throw RuntimeException("Error while adjusting the counters for a removed trail element.\ncounters[$index].asgTrue = ${counters[index]!!.asgTrue} \n" +
                        "counters[$index].asgFalse = ${counters[index]!!.asgFalse} \n" +
                        "counters[$index].size = ${counters[index]!!.size}")
            }
            // add to u if it becomes a unit clause
            if((counters[index]!!.size - counters[index]!!.asgFalse) == 1 && counters[index]!!.asgTrue == 0 && !u.contains(index)) {
                u.add(index)
            }
            // remove from u if it is not a unit clause anymore
            if((counters[index]!!.size - counters[index]!!.asgFalse) > 1 && counters[index]!!.asgTrue == 0 && u.contains(index)) {
                u.remove(index)
            }
        }
        if(lc.keys.contains(-1*literal)) {
            for(index in lc[-1*literal]!!) {
                counters[index]!!.asgFalse--
                if(counters[index]!!.asgFalse < 0) {
                    throw RuntimeException("Error while adjusting the counters for a removed trail element.\ncounters[$index].asgTrue = ${counters[index]!!.asgTrue} \n" +
                            "counters[$index].asgFalse = ${counters[index]!!.asgFalse} \n" +
                            "counters[$index].size = ${counters[index]!!.size}")
                }
                // add to u if it becomes a unit clause
                if((counters[index]!!.size - counters[index]!!.asgFalse) == 1 && counters[index]!!.asgTrue == 0 && !u.contains(index)) {
                    u.add(index)
                }
                // remove from u if it is not a unit clause anymore
                if((counters[index]!!.size - counters[index]!!.asgFalse) > 1 && counters[index]!!.asgTrue == 0 && u.contains(index)) {
                    u.remove(index)
                }
            }
        }

        return CurrentFormula(f, lc, u, counters, trail)
    }

    /**
     * Add a new clause to the given [formula] and update the data structures accordingly.
     *
     * @param formula the current formula represented by three maps and the trail
     * @param clauseIndex the index for the new added clause, which is not allowed to be already used in the formula
     * @param clause the clause to be added, represented by a set of integers
     * @return the updated [formula]
     */
    private fun addNewClauseToFormula(formula: CurrentFormula, clauseIndex: Int, clause: Set<Int>) : CurrentFormula {

        val f = formula.f
        val lc = formula.lc
        val u = formula.u
        val counters = formula.counters
        val trail = formula.trail

        if(clauseIndex in f.keys) {
            throw RuntimeException("Tried to added a clause with a clause index that is already existing (clauseIndex = $clauseIndex)")
        }
        if(f.containsValue(clause)) {
            throw RuntimeException("Tried to added a clause, which is already existing (clause: $clause)")
        }

        counters[clauseIndex] = Counters(size = clause.size, asgTrue = 0, asgFalse = 0)
        f[clauseIndex] = clause
        for (literal in clause) {
            if(lc.containsKey(literal)) {
                lc[literal] = lc[literal]!!.plus(clauseIndex)
            } else {
                lc[literal] = setOf(clauseIndex)
            }
            // update counters
            for (trailTuple in trail) {
                // literal assigned true in trail
                if (trailTuple.first == literal) {
                    counters[clauseIndex]!!.asgTrue += 1
                }
                if (trailTuple.first == (-1*literal)) {
                    counters[clauseIndex]!!.asgFalse += 1
                }
            }
        }
        if((counters[clauseIndex]!!.size - counters[clauseIndex]!!.asgFalse) == 1 && counters[clauseIndex]!!.asgTrue == 0) {
            u.add(clauseIndex)
        }

        return CurrentFormula(f, lc, u, counters, trail)
    }

    /**
     * Determine the highest level of the trail.
     *
     * @param currentFormula the current formula represented by three maps and the trail
     * @return the highest level of the trail of [currentFormula]
     */
    private fun getCurrentTrailLevel(currentFormula: CurrentFormula) : Int {

        val trail = currentFormula.trail

        var currentLevel = 0
        for(trailElem in trail) {
            if(trailElem.second == 'g') {
                currentLevel++
            }
        }

        return currentLevel
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