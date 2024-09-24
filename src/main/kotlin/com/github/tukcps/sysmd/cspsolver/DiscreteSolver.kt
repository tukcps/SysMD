package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.functions.intersect
import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.analyzer.DDAnalyzer
import com.github.tukcps.sysmd.cspsolver.analyzer.SemanticAnalyzer
import com.github.tukcps.sysmd.cspsolver.analyzer.Solution
import com.github.tukcps.sysmd.cspsolver.normalizer.CNNormalizer
import com.github.tukcps.sysmd.cspsolver.normalizer.NormalizedProperties
import com.github.tukcps.sysmd.cspsolver.normalizer.NormalizedPropertiesSolver
import com.github.tukcps.sysmd.cspsolver.propagator.NetworkConsistencyPropagator
import com.github.tukcps.sysmd.cspsolver.propagator.PropagatorIF
import com.github.tukcps.sysmd.cspsolver.propagator.RequirementPropagator
import com.github.tukcps.sysmd.cspsolver.valuefeatures.GuardValueFeature
import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedExpression
import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedValueFeature
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDInternalError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import java.util.*
import kotlin.math.absoluteValue

/**
 *  Handles discrete propagation. Currently only through one propagate method. Eventually will split in initialization and update.
 */

/**
 * TODO's: - ConflictTracer (own class?)
 *         - Human readable reasoning (string-msg?)
 */

//typealias UnitMap = MutableMap<UId, MutableMap<Int, DD>>

class DiscreteSolver(
    val model: Session
) : DiscreteSolverIF {
    //Done refactoring:
    private var debugMode = false
    private var allSolutions = false
    private var initLevel = 0

    private val schedule: MutableList<Variable> //TODO: refactor. Not sure if still needed.
        get() = model.repo.schedule

    val builder: DDBuilder
        get() = model.builder


    private lateinit var booleanConditions: MutableMap<Int, Pair<DD, UUID?>>//Map<Int, Triple<DD, Trail, UUID?>>
        //get() = builder.conds.x.filter { it.value is BDD }

    private var depth = DiscreteSolverDepth(0, DiscreteSolverHistoryTreeNode(0, null))

    private val ddAnalyzer: DDAnalyzer = DDAnalyzer(model)
    private val semanticAnalyzer: SemanticAnalyzer = SemanticAnalyzer(model)
    private lateinit var unitMap: UnitMap //will be set in initialize function
    private lateinit var infeasibilityMap: InfeasibilityMap //will be set in initialize function

    private lateinit var networkConsistencyPropagator: NetworkConsistencyPropagator
    private lateinit var requirementPropagator: RequirementPropagator
    private val propagators: HashSet<PropagatorIF> = hashSetOf()

    private val evaluations: HashMap<Variable, DD?> = hashMapOf()

    private val updatedProperties: HashSet<Variable> = hashSetOf()

    private val requirements: MutableList<Variable> = mutableListOf()

    //Normalizer + Boolean SAT Solver
    private var activateNormalizedBooleanValueFeatureSolver = true
    /*private*/ val booleanValueFeatureNormalizer = CNNormalizer()
    /*private*/ lateinit var normalizedBooleanModel: NormalizedProperties //will be set in initialize function
    /*private*/ var normalizedBooleanValueFeatureSolver = NormalizedPropertiesSolver()


    //TODO: Refactor
    //private lateinit var step: DiscreteSolverStep Replaced by Depth

    //private var currentStepCount = 0 //planned for conflict detection to comprehend where conflicts could stem from
    //private var history = mutableListOf<DiscreteSolverStep>()
    private val noGoods = hashMapOf<Variable, DD>()

    private val relatedValueFeatures: HashSet<RelatedValueFeature> = hashSetOf()
    private val guards: HashSet<GuardValueFeature> = hashSetOf()
    // private val hasAs: HashSet<HasAValueFeature> = hashSetOf()
    // private val isAs: HashSet<IsAValueFeature> = hashSetOf()

    private val conflicts = hashMapOf<Int, HashSet<DD>>()
    private val contradictions = hashSetOf<Variable>()


    private val conditionIndexes: MutableSet<Int>
        get() = builder.conds.x.keys


    //Done refactoring
    fun initialize(model: Session, level: Int) {
        //Copies structure from Agila-Session, to be consistent with the current state of initialization of the model
        if (level > 0) {
            unitMap = UnitMap(builder)
            infeasibilityMap = InfeasibilityMap(builder)

            propagators.clear()
            initLevel = level
        }

        if (level > 1) {
            //TODO!
            initLevel = level
            //initClassifiers()     // Calls initialize of classifiers that will add inherited properties.
        }
        if (level > 2) {
            //TODO!
            initLevel = level
            //resolveNames()        // Again, update name resolution considering inheritance
        }
        if (level > 5) {
            //TODO!
            initLevel = level
            //initRelationships()
        }
        if (level > 4) {
            schedule.forEach { update(it) }
            initLevel = level
        }

        // Now, we only do checking and reporting of issues to the Agenda.
        if (level > 6) {
            //TODO: Nothing?
            initLevel = level
            //checkIdentifications()
        }
        if (level > 7) {
            networkConsistencyPropagator = NetworkConsistencyPropagator(model)
            //requirementPropagator = RequirementPropagator(model)
            //ddAnalyzer = DDAnalyzer(model)
            //semanticAnalyzer = SemanticAnalyzer(model)
            //initBooleanConditions() FIXME: Write back in! Causes bug...

            //val booleanProps = valFeats.filter { it.ofClass?.ref == model.repo.booleanType }
            //requirements.addAll(processRequirements(booleanProps)) FIXME: No ValFeats at that time?! Schedule also empty...
            advanceState()
            normalizedBooleanModel = normalizeBooleanValueFeatures()
            if (activateNormalizedBooleanValueFeatureSolver) initNormalizedBooleanValueFeatureSolver(allSolutions, debugMode)
            initLevel = level
        }
    }


        fun resetNormalizedBooleanValueFeatureSolver(all: Boolean = true, debug: Boolean = false) {
            normalizedBooleanValueFeatureSolver = NormalizedPropertiesSolver()
            initNormalizedBooleanValueFeatureSolver(all, debug)
        }

    //fun isInitialized() : Boolean {
    //    return initLevel > 0 //Possible to stop mid initialization e.g. at level = 3?
    //}

    /*private*/ fun normalizeBooleanValueFeatures() : NormalizedProperties {
        return booleanValueFeatureNormalizer.normalizeBooleanConstraints(model)
    }

    fun normalizeBooleanValueFeatures(model: Session) : NormalizedProperties {
        return booleanValueFeatureNormalizer.normalizeBooleanConstraints(model)
    }

    fun activateNormalizedBooleanValueFeatureSolver() {
        activateNormalizedBooleanValueFeatureSolver = true
    }

    fun deactivateNormalizedBooleanValueFeatureSolver() {
        activateNormalizedBooleanValueFeatureSolver = false
    }

    fun computeAllPartialSolutions(all: Boolean) {
        if (all) normalizedBooleanValueFeatureSolver.findAllAssignments()
        else normalizedBooleanValueFeatureSolver.findOnlyOneAssignment()
    }

    private fun initBooleanConditions() {
        val boolConds = builder.conds.x.filter { it.value is BDD }
        val booleanConditions = mutableMapOf<Int, Pair<DD, UUID?>>()//mutableMapOf<Int, Triple<DD, Trail, UUID?>>()
        boolConds.forEach {
            val uuid: String = builder.conds.indexes.filter { (key, value) -> value == it.key }.keys.first()
            booleanConditions[it.key] = Pair(it.value, UUID.fromString(uuid))
        }//Triple(it.value, Trail.FREE, null) }
        this.booleanConditions = booleanConditions
    }

    private fun initNormalizedBooleanValueFeatureSolver(allAssignments: Boolean = true, debugMode: Boolean = false) {
        if (debugMode) normalizedBooleanValueFeatureSolver.activateDebugMode() else normalizedBooleanValueFeatureSolver.deactivateDebugMode()
        if (allAssignments) normalizedBooleanValueFeatureSolver.findAllAssignments() else normalizedBooleanValueFeatureSolver.findOnlyOneAssignment()
    }

    /**
     * NormalizedBooleanValueFeature returns satisfying assignments via CNF depicted as Set of Set of Ints
     * The Function is intended to translate back those CNFs into our Model
     */
    /*private*/ fun translateCNFsBack(cnf: Set<Set<Int>>) : Set<MutableMap<Int, BDD>> {
        val assignments = mutableSetOf<MutableMap<Int, BDD>>()

        cnf.forEach { assignments.add(translateCNFBack(it)) }

        return assignments
    }

    private fun translateCNFBack(cnf: Set<Int>) : MutableMap<Int, BDD> {
        val assignment = mutableMapOf<Int, BDD>()

        for (c in cnf) {
            if (c < 0 )
                assignment[c.absoluteValue] = builder.False //negative case
            else
                assignment[c] = builder.True //positive case
        }
        return assignment
    }

    private fun solveNormalizedNet() : Set<MutableMap<Int, BDD>>
    {
        return translateCNFsBack(normalizedBooleanValueFeatureSolver.solveNormalizedCN(this.normalizedBooleanModel))
    }

    private fun initDepth() {
        val history = DiscreteSolverHistoryTreeNode(0, null)
        //history.assignments = TODO()
        // history.noGoods = TODO()
        // depth = DiscreteSolverDepth(0, history)
    }

    private fun updateFeature(feature: Feature) {
        TODO()
    }

    private fun updateValueFeature(valueFeature: Variable) {
        if (containsInequation(valueFeature) && valueFeature.vectorQuantity.value !is BDD) {//(containsInequation(up)) {
            infeasibilityMap.update(valueFeature, ddAnalyzer.getInfeasibleCombinations(valueFeature))
        }
        unitMap.update(valueFeature, ddAnalyzer.getUnits(valueFeature), ddAnalyzer.getDontCares(valueFeature))
    }

    private fun updateSpezialization(specialization: Specialization) {
        //TODO()
    }

    private fun updateMultiplicity(multiplicity: Multiplicity) {
        //TODO()
    }

    private fun updateRelationship(relationship: Relationship) {
        //TODO()
    }

    private fun processUpdates() {
        updatedProperties.forEach { variable -> updateValueFeature(variable) }
    }

    //TODO: Refactoring
    @Deprecated("Will be phased out. Use initialize function with an Integer Param!")
    override fun initialize(model: Session) {

        unitMap = UnitMap(builder)
        infeasibilityMap = InfeasibilityMap(builder)

        propagators.clear()
        //ddAnalyzer = DDAnalyzer(model)
        //semanticAnalyzer = SemanticAnalyzer(model)
        networkConsistencyPropagator = NetworkConsistencyPropagator(model)

        //step = DiscreteSolverStep(0)
        //step.unitMap = this.unitMap
        //step.infeasibilityMap = this.infeasibilityMap
        //history.add(step)

        model.get().filterIsInstance<Variable>().forEach { update(it) }
        advanceState()
    }

    fun isInitialized(): Boolean = (/*this::ddAnalyzer.isInitialized && this::semanticAnalyzer.isInitialized &&*/ this::networkConsistencyPropagator.isInitialized && this::unitMap.isInitialized && this::infeasibilityMap.isInitialized)


    fun getCreatingProperty(relatedProperty: Variable): Variable? {
        return if (relatedProperty !is RelatedValueFeature) null
        else relatedProperty.createdBy
    }

    fun getRelatedValueFeatures(id: UUID): HashSet<RelatedValueFeature> {
        return relatedValueFeatures.filter { it.createdBy.elementId == id }.toHashSet()
    }

    fun getGuardValueFeatures(id: UUID): HashSet<GuardValueFeature> {
        return guards.filter { it.createdBy.elementId == id }.toHashSet()
    }

    override fun update(scheduledProperties: List<Variable>) {
        for (s in scheduledProperties) {
            if (s.updated) update(s)
        }
    }

    override fun update(updatedProperty: Variable) {
        updateEvaluations(updatedProperty)
        updateAnalyzer(updatedProperty)
        updatedProperties.add(updatedProperty)
    }

    /**
     * Called After Updates have been reported. Will analyze properties and advance search or
     * return to the previous step respectively
     */
    override fun advanceState() {
        //TODO: Sepparate into 1. analyzing part 2. propagation part 3. error checking/handling
        //TODO: This goes into the analyzing part
        processUpdates()
        val nameIndexMapping = builder.conds.indexes.entries.associate { (key, value) -> value to key }


        //val introducedRelatedProperties = introduceRelatedValueFeatures(unitMap)
        val decisionExpressions = introduceExpressionsFromDecisionVariables() //Expr "pointing" to introduced decvars. prevent undefined state

        val inferredDecisions = setAndPropagateBooleanUnits(unitMap)
        //set unit conditions and propagate!!
        for (d in inferredDecisions) {
            //set unit condition
            builder.conds.x[d.key] = d.value

            //propagate result
            val createdByString = builder.conds.introducedDecVars[nameIndexMapping[d.key]!!]!!

            val creatingElement = model.global.resolve<Element>(createdByString)
            val createdBy = if (creatingElement != null)
                    (creatingElement as Feature).variable!!
                else if (createdByString != "noSourceExpression")
                    (model[UUID.fromString(createdByString)]!! as Feature).variable!!
                else null
            //     TODO("TODO: handle variables with no source expression")
            if (createdBy != null) {
                createdBy.vectorQuantity.values = listOf(createdBy.vectorQuantity.value.evaluate())
                createdBy.updated = true
            }
        }

        //val infeasibilityGuardingProperties = introduceGuardValueFeatures(infeasibilityMap) Commented out to be replaced by nogood recording


        //recordNoGoods(infeasibilityGuardingProperties as MutableList<GuardValueFeature>)
        //enforceNodeConsistency(introducedRelatedProperties as MutableList<RelatedValueFeature>)

        //verifyState(introducedRelatedProperties, infeasibilityGuardingProperties, possibleConflicts)

        //assertConstraints()
        updatedProperties.clear()
    }

    //FIXME: intersect with dependency!
    fun assertConstraints() {
        val booleanConstraints = schedule.filter { it.baseType == Variable.BaseType.Bool }
        val booleanConstrained = booleanConstraints.filter { it.boolSpecs[0] in setOf(XBool.True, XBool.False) }

        booleanConstrained.forEach {
            val oldValue = /*evaluations[it] as DDcond // */
                if (it.vectorQuantity.value.toString() == "Unknown") XBool.X.bddLeafOf(builder)
                else it.vectorQuantity.value
            val specc = it.boolSpecs[0].bddLeafOf(builder)
            it.vectorQuantity.values = mutableListOf(specc.intersect(oldValue))
        }
    }


    private fun analyzeState():Pair<HashSet<RelatedValueFeature>, HashSet<GuardValueFeature>> {
        //TODO
        return Pair(hashSetOf(), hashSetOf())
    }

    private fun propagateState(analysis: Pair<HashSet<RelatedValueFeature>, HashSet<GuardValueFeature>>) {
        //FIXME: Since generated value features are registered and scheduled input parameters still necessary?
        schedule.forEach { ot -> propagators.forEach { it.execute(ot) } }
    }

    private fun verifyState(introducedRelatedProperties: List<Variable>, introducedGuardProperties: List<Variable>, possibleConflicts: HashMap<Int,  DD>) {
        //TODO
        val conflicts = checkForConflicts(possibleConflicts)
        val errors = checkForErrors(updatedProperties as HashSet<Variable>)
        if (conflicts.isEmpty() && errors.isEmpty()) { //best case

            //val newStep = increaseStep(currentStepCount, introducedRelatedProperties)
            //history.add(newStep)
        }
        else {
            if (conflicts.isNotEmpty() && errors.isEmpty()) { //conflict between two conditions, but not necessary unsatsfiable
                for (c in conflicts) {
                    //TODO
                    //println("conflict: " + c.toString())
                }
            }
            else if (conflicts.isEmpty() && errors.isNotEmpty()) { //no conflict, but somehow property got usnatisfiable
                for (e in errors) {
                    //TODO
                    //println("errors: " + e.toString())
                    if (e.ast != null) {
                        val dependencies = e.ast!!.getDependencies()
                        for (d in dependencies)
                            d.ast!!.evalUpRec()
                    }
                }
            }
            else { // conflict that likely makes at least one property unsatisifable
                //TODO!
                for (c in conflicts) {
                    //TODO
                    //println("conflict: " + c.toString())
                }
                for (e in errors) {
                    //TODO
                    //println("errors: " + e.toString())
                }
            }
            //determine wether conflict or error or both and return to previous step and record nogoods.
        }
    }

    private fun checkForConflicts(possibleConflicts: HashMap<Int, DD>): List<Pair<Int, HashSet<Variable>>> {
        val result = mutableListOf<Pair<Int, HashSet<Variable>>>()
        for (i in possibleConflicts.keys) {
            if (possibleConflicts[i].toString() == "Contradiction") {
                val involvedProperties = hashSetOf<Variable>()
                for (u in unitMap.values) {
                        if (u.containsKey(i)) {
                            involvedProperties.add(unitMap.getKey(u))
                        }
                }
                result.add(Pair(i, involvedProperties))
            }
        }
        return result
    }

    private fun checkForErrors(updatedProperties: HashSet<Variable>): List<Variable> {
        //FIXME: Only checking updated properties sufficient?
        val result = mutableListOf<Variable>()
        for (p in updatedProperties) {
            if (p.boolSpecs != XBool.X) {
                if (p.vectorQuantity.bdd().value != p.boolSpecs) result.add(p)
            }
        }
        return result
    }

    private fun recordNoGoods(guards: MutableList<GuardValueFeature>) {
        guards.forEach { recordNoGood(it.createdBy.elementId, it) }
    }

    private fun recordNoGood(id: UUID, guard: GuardValueFeature) {
        //println(guard.quantity.value.toIteString())
        //println(guard.dependency.toString())
    }

    /**
     * This function will set Conditions of Builder according to introduced Related Properties
     */
    private fun enforceNodeConsistency(relatedValueFeatures: MutableList<RelatedValueFeature>) {
        relatedValueFeatures.forEach { networkConsistencyPropagator.execute(it) }
    }

    //Will create "or" expressions specified to true for blocks of dec vars
    private fun introduceExpressionsFromDecisionVariables() : List<Variable> {
        //if model.
        //val workaround = model.global.resolveName<Type>("ScalarValues::Boolean")
        //if (workaround == null) return listOf()
        val result = mutableListOf<Variable>()
        val nameIndexMapping = builder.conds.indexes.entries.associate { (key, value) -> value to key }

        for (cond in conditionIndexes) {
            val condName = nameIndexMapping[cond] //should be always there?
            condName?.let {
                if (builder.conds.isDecVar.contains(condName)) {
                    if (builder.conds.isDecVar[condName]!!) {
                        val createdBy = builder.conds.introducedDecVars[condName]!!
                        val others = builder.conds.decVarsIntroducedBy[createdBy]!!
                        if (others.size > 1) { //cluster of dec vars
                            //TODO: Create expression over whole cluster
                            val otherExprNames = mutableListOf<String>()
                            for ((othersCounter, _) in others.withIndex()) {
                                val name = "$createdBy-DecisionVariable-$othersCounter"
                                val relatedExpression = RelatedExpression(
                                    name = name,
                                    dependency = "true",//"$condName",
                                    valueSpecs = mutableListOf(XBool.X), //If we encode choice in 1 var => spec has to be bool
                                    relatedIndex = cond,
                                    createdBy = model.global.resolveVar(createdBy) ?: model.getVariable(UUID.fromString(createdBy))
                                )
                                relatedExpression.vectorQuantity = Quantity(builder.conds.x[cond] as BDD)
                                relatedExpression.oldVectorQuantity = relatedExpression.vectorQuantity
                                val registered = registerOrUpdateProperty(relatedExpression, model.global.resolveVar(createdBy)
                                    ?: model.getVariable(UUID.fromString(createdBy))
                                )
                                result.add(registered)
                                otherExprNames.add(name)
                            }
                            //TODO: or-expression over dec vars
                            for (name in otherExprNames) {
                                //TODO!
                            }
                        }
                        else { //only one
                            //TODO!
                            val relatedExpression = RelatedExpression(
                                name = createdBy+"-DecisionVariable",
                                dependency = "$condName",
                                valueSpecs = mutableListOf(XBool.X), //If we encode choice in 1 var => spec has to be bool
                                relatedIndex = cond,
                                createdBy = model.global.resolveVar(createdBy)
                                    ?: (model[UUID.fromString(createdBy)]!! as Feature).variable!!
                            )
                            relatedExpression.vectorQuantity = Quantity(builder.conds.x[cond] as BDD)
                            relatedExpression.oldVectorQuantity = relatedExpression.vectorQuantity
                            val registered = registerOrUpdateProperty(relatedExpression, model.global.resolveVar(createdBy)
                                ?: (model[UUID.fromString(createdBy)]!! as Feature).variable!!
                            )
                            result.add(registered)
                        }
                    }
                }
            }
        }
        //model.resolveNames()
        return result
    }

    private fun setAndPropagateBooleanUnits(unitMap: UnitMap) : HashMap<Int, DD> {
        val pinnedUnits = hashMapOf<Int, DD>()

        conditions@ for (cond in conditionIndexes) {
            var unitOverExpressions = builder.Bool
            expressions@ for (ex in unitMap.keys) {
                val unit = unitMap[ex][cond]
                if (unit == null || (unit != builder.True && unit != builder.False)) continue@expressions
                    unitOverExpressions = unitOverExpressions.intersect(unit as BDD.Leaf) //FIXME: DDcond should be a true/false leave
            }
            if (unitOverExpressions != builder.Bool) {//unit over all expressions
                pinnedUnits[cond] = unitOverExpressions
            }
        }

        return pinnedUnits
    }

    @Deprecated("Unfinished. Will be phased out after refactor")
    private fun introduceRelatedExpressions(unitMap: UnitMap) : MutableList<Variable> {
        val newExpressions = mutableListOf<Variable>()
        val nameIndexMapping = builder.conds.indexes.entries.associate { (key, value) -> value to key }

        expressionLoop@ for (ex in unitMap.keys) {
            if (ex.baseType == Variable.BaseType.Bool) {//boolean case
                //TODO!
                if (ex.boolSpecs[0] != builder.True && ex.boolSpecs[0] != builder.False) { //means no spec!
                    continue@expressionLoop //Fixme: will skip the expression, but not the conditions within it?!
                }
                else { //true or false
                    conditionLoop@ for (cond in conditionIndexes) {
                        val unit = unitMap[ex][cond]
                        if (unit != builder.True && unit != builder.False) continue@conditionLoop
                        val relatedExpression = ex
                        var reasonString: String? = null
                        if (nameIndexMapping[cond] != null) {
                            val fromExpr = builder.conds.introducedDecVars[nameIndexMapping[cond]]!!
                            val created = builder.conds.decVarsIntroducedBy[fromExpr]!!
                            var varCreatedFrom: Variable
                            try { //try qualified name!
                                varCreatedFrom = model.global.resolveVar(fromExpr)!!
                            }
                            catch (e: Exception) { //try UUID!
                                varCreatedFrom = model.get(UUID.fromString(fromExpr))!! as Variable //FIXME: Will explode on Enums?
                            }

                            //FIXME: Look at the type of condition, not the type of result!
                            if (varCreatedFrom.vectorQuantity.value !is BDD) continue@conditionLoop //condition/decVar stems from expressions that is not boolean!

                            if (created.size > 1) { //enums... and what else? - but enums have no bool-type and no unit?
                                //construct or string for alternatives
                                // created.forEach { println(it) }
                            }
                            else {
                                reasonString = varCreatedFrom.feature.name.toString() + " Condition " + cond + " = " + unit.toString()
                                //dependencyString = ""
                                // println("${varCreatedFrom!!.qualifiedName} = $unit")
                            }
                        }
                    }
                }
            }
            else { //Int, Real, Str?

            }
        }
        return newExpressions
    }

    @Deprecated("Historically overgrown code. Will be split up in different functions.")
    private fun introduceRelatedValueFeatures(unitMap: UnitMap): MutableList<Variable> {
        val newProperties = mutableListOf<Variable>()

        val reversed = builder.conds.indexes.entries.associate { (key, value) -> value to key }

        for (prop in unitMap.keys) {
            if (prop.baseType == Variable.BaseType.Bool) continue
            if (prop.baseType == Variable.BaseType.Bool && prop.boolSpecs[0] != builder.True && prop.boolSpecs[0] != builder.False ) continue
            innerloop@ for (i in conditionIndexes) {
                val unit = unitMap[prop][i]
                if (unit != builder.True && unit != builder.False) continue
                val relatedProperty = prop//model.getProperty(k)
                var dependencyString: String? = null
                if (reversed[i] != null ) {//reversed[i]?.let {
                    try {
                        var condIndex = ""
                        var cutReversed: String
                        // println("reversed: ${reversed[i]}")
                        val fromExpr = builder.conds.introducedDecVars[reversed[i]]!!
                        val created = builder.conds.decVarsIntroducedBy[fromExpr]!!

                        val varCreatedFrom: Variable =
                            model.global.resolveVar(fromExpr)
                            ?: model[UUID.fromString(fromExpr)]!! as Variable

                        if (created.size > 1) { //enums
                            //construct or string for alternatives
                            // created.forEach { println(it) }
                        }
                        else {
                            //dependencyString = ""
                            // println("${varCreatedFrom!!.qualifiedName} = $unit")
                        }

                        if (varCreatedFrom.vectorQuantity.value !is BDD) continue@innerloop //condition/decVar stems from expressions that is not boolean!



                        if (reversed[i]!!.toString().contains("Condition", true)) {
                            cutReversed = reversed[i].toString().split("Condition").first()
                            condIndex = reversed[i].toString().split("Condition")[1]
                        }
                        else cutReversed = reversed[i]!!
                        dependencyString = // why not use uuid of element? or its qualifiedName?
                                model[UUID.fromString(cutReversed)]?.name.toString() + "Condition" + condIndex + " = " + unit.toString()

                    }
                    catch (s: SemanticError) {
                        s.element = prop.feature
                        model.report(s)
                        continue@innerloop
                    }
                }
                dependencyString?.let {
                    val newRelatedProperty =
                        createRelatedValueFeature(dependencyString, unit.toString(), i, relatedProperty)
                    if (relatedValueFeatures.contains(newRelatedProperty)) {
                        val registered = registerOrUpdateProperty(newRelatedProperty, relatedProperty)
                        relatedValueFeatures.add(registered as RelatedValueFeature)
                        newProperties.add(registered)
                    } else {
                        val registered = registerOrUpdateProperty(newRelatedProperty, relatedProperty)
                        relatedValueFeatures.add(registered as RelatedValueFeature)
                        newProperties.add(registered)
                    }
                }
            }
        }
        return newProperties
    }

    /**
     * Introduces new Related Properties through (simple) inference. Returns List of newly created Properties
     */

    private fun introduceGuardValueFeatures(infeasibilityMap: InfeasibilityMap): MutableList<Variable> {
        val introducedGuards = mutableListOf<Variable>()
        val reversedConditions = builder.conds.indexes.entries.associate { (k, v) -> v to k }
        for (i in infeasibilityMap.entries) {
                    //Construct dependency from each hashmap => those props must not be fulfilled
            var  pathCounter = 0
            for (hm in i.value) { //hashSet
                var dependencyString = ""
                for (index in hm.entries) {
                    val valueFeatureName = reversedConditions[index.key]
                    dependencyString += if (index.value)
                        "$valueFeatureName and "
                    else
                        "not($valueFeatureName) and "
                }
                dependencyString = dependencyString.dropLast(4) //remove the last " and"
                val guardianProperty = createGuardValueFeature(dependencyString, hm, pathCounter++, i.key)
                guardianProperty.vectorQuantity = Quantity(builder.False)
                //FIXME: Both cases the same?
                if (introducedGuards.contains(guardianProperty)) {
                    val registered = registerOrUpdateProperty(guardianProperty, i.key)
                    introducedGuards.add(registered)
                    guards.add(registered as GuardValueFeature)
                }
                else {
                    val registered = registerOrUpdateProperty(guardianProperty, i.key)
                    introducedGuards.add(registered)
                    guards.add(registered as GuardValueFeature)
                }
            }
        }
        return introducedGuards
    }

    private fun createRelatedValueFeature(dependencyString: String?, valueSpecString: String, varIndex: Int, createdBy: Variable): RelatedValueFeature {
        dependencyString?:model.report(createdBy.feature, "Erroneous dependency string (index = null)")

        val dependency = if (dependencyString!!.contains("True")) "true" else "false"
        val introducedProperty = RelatedValueFeature(
            name = createdBy.elementId.toString()+".RelatedValueFeatureFor:"+dependencyString,
            dependency = dependency,//dependencyString?:"Error",
            valueSpecs = mutableListOf(XBool.valueOf(valueSpecString)),
            relatedIndex = varIndex,
            createdBy = createdBy
        )
        introducedProperty.vectorQuantity = if (dependency == "true") Quantity(builder.True) else Quantity(builder.False)//Quantity(builder.conds.getCondition(varIndex) as BDD)
        introducedProperty.oldVectorQuantity = introducedProperty.vectorQuantity
        introducedProperty.updated = true

        return introducedProperty
    }

    private fun createGuardValueFeature(dependencyString: String, path: Solution, pathNr: Int, createdBy: Variable): GuardValueFeature {
        val introducedProperty = GuardValueFeature(
            name = createdBy.elementId.toString()+".GuardValueFeature"+pathNr,
            dependency = dependencyString,
            valueSpecs = mutableListOf(XBool.valueOf("False")),
            createdBy = createdBy,
            paths = path.map
        )
        introducedProperty.vectorQuantity = Quantity(builder.False)
        return introducedProperty
    }

    private fun registerOrUpdateProperty(property: Variable, owningProperty: Variable): Variable {
        schedule.forEach {
            if (it.name == property.name) {
                it.feature.expression = property.dependency
                it.valueSpecs = property.valueSpecs
                return it
            }
        }
        schedule.add(property)
        return property
    }

    private fun isInequation(property: Variable): Boolean {
        return (property.dependency.contains(">") || property.dependency.contains("<"))
    }

    /**
     * Returns true if property contains at least one inequation.
     * For future updates: Maybe more efficient to just check dependencies for dd-subtype?
     */
    private fun containsInequation(property: Variable): Boolean {
        val dependecies = property.ast?.root?.getDependencies()
        if (!dependecies.isNullOrEmpty())  {
            for (dependency in dependecies) {
                if (dependency.ast != null) {
                    if (containsInequation(dependency)) return true
                }
                if (isInequation(dependency)) return true
            }
        }
        return isInequation(property) //iif no inequation in dependenciey, maybe it itself is one
    }

    private fun updateEvaluations(updatedProperty: Variable) {
        evaluations[updatedProperty] = if (updatedProperty.ast != null) updatedProperty.ast!!.dependency.dd.evaluate() else updatedProperty.vectorQuantity.value.evaluate()
    }

    private fun updateAnalyzer(updatedProperty: Variable) {
        ddAnalyzer.updateProperty(updatedProperty)
        semanticAnalyzer.updateProperty(updatedProperty)
    }

    //Update function called after conditions are set...
    private fun updateNoGoods() {
        noGoods.keys.forEach  {noGoods[it] = noGoods[it]!!.evaluate()}
    }

    override fun returnToLastState() {
        //decreaseStep(currentStepCount--)
    }

    class DiscreteConflictDetectedException(msg: String): Exception(msg)
    class DiscreteSolverInvalidStepOperationException(msg: String): Exception(msg)

}

fun Session.getVariable(uuid: UUID): Variable =
    when(val found = get(uuid)) {
        is Variable -> found
        is Feature ->  found.variable!!
        else -> throw SysMDInternalError("Internal error: should be not reachable")
    }
