
package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.XBool
import java.util.*

typealias DDLeaf=DD.Leaf<*>
typealias DDInternal=DD.Internal<*>

/**
 * This AST node holds information for the constraint propagation.
 * It holds the root of the AST for a given property that has to be computed.
 * @param model the overall data model as dependency injected via constructor parameter.
 * @param feature the feature - variable that is computed by the AST
 * @param dependency the AST that describes the dependency of the property from other properties.
 */
class AstRoot(
    model: Session,
    val feature: Feature,
    val dependency: AstNode
) : AstNode(model) {

    val variable: Variable
        get() = feature.variable!!

    // Leaves of the AST with this as a root
    internal var leaves: Collection<AstLeaf> = ArrayList()

    /** initialization */
    init {
        dependency.root = this // recursion in setter methods will set it down to leaves.
    }

    override fun initialize() {
        upQuantity = dependency.upQuantity.clone()
        downQuantity = dependency.upQuantity.clone()
        leaves = dependency.getLeaves()
        variable.ast = this
        if(variable.baseType == BaseType.Real && variable.vectorQuantity.unit.clone().toSI()!=upQuantity.unit.clone().toSI())
            model.status.inconsistency(element = variable.feature, message = "Unit of ${variable.feature.escapedName()} (${variable.vectorQuantity.unit}) does not match the unit of the dependency (${upQuantity.unit})")
        variable.vectorQuantity.values = upQuantity.values
        variable.vectorQuantity.unit = upQuantity.unit

    }

    /**
     *  Takes value and unit from the dependency.
     *  - Transforms the value/unit if possible to the SI unit
     */
    override fun evalUp() {
        when {
            this.dependency.isReal -> {
                if (variable.baseType == BaseType.Real && !variable.feature.isSufficient) {
                    val quantityWithTransformedUnit = dependency.upQuantity.clone()
                    upQuantity = VectorQuantity(
                        quantityWithTransformedUnit.values,
                        quantityWithTransformedUnit.unit,
                        variable.vectorQuantity.unitSpec
                    )
                    variable.vectorQuantity = upQuantity.constrain(variable.vectorQuantity, variable.rangeSpecs, variable.unitSpec)
                    if(variable.intSpecs.size!=dependency.upQuantity.values.size && variable.intSpecs.size!=1)
                        throw VectorDimensionError("Vector size of ${dependency.upQuantity.values.size} does not match constraint size of ${variable.rangeSpecs.size}")
                    if (variable.vectorQuantity.values.any { it == model.builder.Empty })
                        model.status.inconsistency(element = variable.feature, message = "dependency of ${variable.feature.escapedName()} is not satisfiable")
                } else if (variable.baseType == BaseType.Real && variable.feature.isSufficient) {
                    // Convert the rangeSpecs to a VectorQuantity
                    val values = mutableListOf<AADD>()
                    variable.rangeSpecs.forEach{values.add(model.builder.real(it))}
                    upQuantity = VectorQuantity(values, variable.unitSpec)
                    variable.vectorQuantity = upQuantity
                    if(variable.rangeSpecs.size!=dependency.upQuantity.values.size && variable.rangeSpecs.size!=1)
                        throw VectorDimensionError("Vector size of ${dependency.upQuantity.values.size} does not match constraint size of ${variable.rangeSpecs.size}")
                    if(variable.rangeSpecs.size == dependency.upQuantity.values.size)
                        if (variable.rangeSpecs.indices.any{variable.rangeSpecs[it] !in (dependency.upQuantity.values[it] as AADD).getRange()})
                            model.status.warn(Issue.Kind.WARN_INCONSISTENCY,"Dependency for ${variable.feature.escapedName()} cannot be satisfied for all values of range.", element = variable.feature)
                } else
                    throw SemanticError("${variable.name}: expect expression of type Real", variable.feature)
            }

            this.dependency.isBool -> {
                upQuantity = dependency.upQuantity.constrain(variable.boolSpecs, true)
                variable.vectorQuantity = upQuantity
            }

            this.dependency.isInt -> {
                if (variable.baseType == BaseType.Int && ! variable.feature.isSufficient) {
                    upQuantity = dependency.upQuantity
                    variable.vectorQuantity = upQuantity.constrain(variable.intSpecs).constrain(variable.vectorQuantity)
                } else if (variable.baseType == BaseType.Int && variable.feature.isSufficient) {
                    upQuantity = dependency.upQuantity
                    val values = mutableListOf<IDD>()
                    variable.intSpecs.forEach { values.add(model.builder.integer(it)) }
                    variable.vectorQuantity = VectorQuantity(values)
                    if(variable.rangeSpecs.size!=dependency.upQuantity.values.size && variable.rangeSpecs.size != 1)
                        throw VectorDimensionError("Vector size of ${dependency.upQuantity.values.size} does not match Constraint size of ${variable.rangeSpecs.size}")
                    if(variable.rangeSpecs.size == dependency.upQuantity.values.size)
                        if (variable.rangeSpecs.indices.any{variable.intSpecs[it] !in (dependency.upQuantity.values[it] as IDD).getRange()})
                            model.status.warn( Issue.Kind.WARN_INCONSISTENCY,"Dependency for ${variable.name} cannot be satisfied for all values of range.", element = variable.feature)
                } else
                    throw SemanticError("${variable.feature.qualifiedName}: expect expression of type Integer", variable.feature)
            }
            this.dependency.isString -> {
                upQuantity = dependency.upQuantity.constrainString(variable.stringSpecs)
                variable.vectorQuantity = upQuantity
            }
        }
    }


    /** This may be causing errors with the IntegerRange / IDD datatype(s) */
    override fun evalUpRec() {
        dependency.evalUpRec()
        //add predefined dimension to the unit
        if(feature.specializes(feature.model!!.repo.realType)&& feature.type.size==1) {
            val type =  feature.type[0].ref?.declaredName.toString()
            if(feature.type[0].ref?.generalization?.firstOrNull()?.ref?.declaredName=="Quantity")
                dependency.upQuantity.unit.unitDimension = type
        }
        evalUp()
    }


    override fun evalDown() {
        when {
            this.isReal -> {
                variable.vectorQuantity = variable.vectorQuantity.constrain(variable.vectorQuantity, variable.rangeSpecs, variable.unitSpec)
                downQuantity = variable.vectorQuantity.clone()
                dependency.downQuantity = downQuantity
            }

            this.isBool -> {
                variable.vectorQuantity = variable.vectorQuantity.constrain(variable.boolSpecs, true)
                downQuantity = variable.vectorQuantity.clone()
                dependency.downQuantity = downQuantity
            } // Nothing to be done for booleans

            // Units do not support the IDD datatype
            this.isInt -> {
                variable.vectorQuantity = variable.vectorQuantity.constrain(variable.intSpecs)
                downQuantity = variable.vectorQuantity.clone()
                dependency.downQuantity = downQuantity
            }
        }
    }

    override fun toExpressionString(): String = dependency.toExpressionString()

    override fun evalDownRec() {
        evalDown()
        dependency.evalDownRec()
        //add predefined dimension to the unit in the leaves (changed by evalDown)
        if(feature.specializes(feature.model!!.repo.realType)&& feature.type.size==1) {
            dependency.getLeaves().forEach {
                val type = it.upQuantity.unit.unitDimension
                it.variable?.vectorQuantity?.unit?.unitDimension = type
            }
        }
    }

    override fun toString() =
        if (variable.isVectorQuantityInitialized) "AstRoot: ${variable.vectorQuantity}"
        else "AstRoot: (uninitialized quantity)"

    @Deprecated("Will be phased out") //TODO not for Vectors implemented
    fun solveAst(): DD<*> {
        val conditions = if (this.isBool) this.bdd.evaluate() else if (this.isReal) this.aadd.evaluate() else this.idd.evaluate()

        if (conditions is DD.Leaf<*>) return conditions
        if (conditions.isInfeasible) return model.builder.Infeasible //TODO: InfeasibleB?

        //Find the shortest path!
        val path = findShortestPath(conditions, variable.boolSpecs[0])

        for (index in path) {
            if (model.builder.conds.getCondition(index.key) is AADD) continue
            val cond = if (index.value) model.builder.True else model.builder.False
            model.builder.conds.setVariable(index.key, cond) //TODO: What if cond != boolSpec?
        }

        return if (this.isBool) this.bdd.evaluate() else this.aadd.evaluate()
    }

    @Deprecated("Will be phased out") //TODO not for Vectors implemented
    fun solveAstWithAlternatives(): DD<*> {
        val conditions = if (this.isBool) this.bdd.evaluate() else if (this.isReal) this.aadd.evaluate() else this.idd.evaluate()

        if (conditions is DD.Leaf) return conditions
        if (conditions.isInfeasible) return model.builder.Infeasible //TODO: InfeasibleB?

        val paths = findAllPaths(conditions, variable.boolSpecs[0])
        var trialState = model.builder.conds.x.toMutableMap()
        var boolSpecOk = true //break condition


        paths@ for (path in paths) {
            indexes@ for (index in path) {

                if (model.builder.conds.getCondition(index.key) is AADD) continue

                val cond = if (index.value) model.builder.True else model.builder.False

                //Check against boolSpec!
                val spec = getConditionBoolSpec(index.key)
                if (!checkAgainstBoolSpec(index.value, spec[0])) {
                    //BREAK CASE!
                    trialState = model.builder.conds.x.toMutableMap() //reset to start point
                    paths.removeFirst()
                    boolSpecOk = false
                    break@indexes
                } else {
                    trialState[index.key] = cond
                }
            }
            if (boolSpecOk) {
                break@paths
            } else
                boolSpecOk = true
        }
        model.builder.conds.x = trialState as HashMap<Int, DD<*>>

        return if (isBool) bdd.evaluate() else aadd.evaluate()
    }

    private fun findShortestPath(dd: DD<*>, target: XBool = XBool.True, path: MutableMap<Int, Boolean> = mutableMapOf()): MutableMap<Int, Boolean> {
        val targetLeaf = when (target) {
            XBool.True, XBool.X -> model.builder.True //default case: if not specified => make it true
            XBool.False -> model.builder.False
            else -> {
                throw Exception("findShortestPath: XBool-case not yet implemented: $target")
            }
        }

        when (dd) {
            is DDLeaf -> {
                return if (dd === targetLeaf) path
                else mutableMapOf()
            }
            is DDInternal -> {
                val pt = path.toMutableMap()
                pt[dd.index] = true
                val t = findShortestPath(dd.T, target, pt)
                val pf = path.toMutableMap()
                pf[dd.index] = false

                val f = findShortestPath(dd.F, target, pf)

                val min = if (t.isNotEmpty() && f.isNotEmpty()) {
                    if (t.size > f.size) f
                    else t
                } else if (t.isNotEmpty()) t
                else if (f.isNotEmpty()) f
                else mutableMapOf()

                for (entry in min)
                    path[entry.key] = entry.value

                return path
            }
            else -> throw Exception("Must not be reached.")
        }
    }


    /*private*/ fun findAllPaths(
        dd: DD<*>,
        target: XBool = XBool.True,
        path: MutableMap<Int, Boolean> = mutableMapOf(),
        paths: MutableList<MutableMap<Int, Boolean>> = mutableListOf()
    ): MutableList<MutableMap<Int, Boolean>> {
        val targetLeaf = when (target) {
            XBool.True, XBool.X -> model.builder.True //default case: if not specified => make it true
            XBool.False -> model.builder.False
            else -> {
                throw Exception("findAllPaths: XBool-case not yet implemented: $target")
            }
        }

        if (dd is DD.Leaf) {
            if (dd === targetLeaf) {
                paths.add(path)
            }
        } else {
            val pt = path.toMutableMap()
            pt[dd.index] = true
            // val t = findAllPaths(dd.T!!, target, pt, paths)
            val pf = path.toMutableMap()
            pf[dd.index] = false
            // val f = findAllPaths(dd.F!!, target, pf, paths)
        }

        return paths
    }

    /** Executes a block of statements on each AstNode in an Ast */
    override fun <R> runDepthFirst(block: AstNode.() -> R): R {
        val result: R = dependency.runDepthFirst(block)
        this.run(block)
        return result
    }

    /** Executes a block of statements on each AstNode in an Ast */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R {
        return withDepthFirst(dependency, block)
    }

    /** Clone method, creates deep copy */
    override fun clone(): AstRoot =
        AstRoot(model, feature, dependency.clone())

    /**
     * Returns the (Boolean) Value from the jAADD builder that corresponds to a Boolean Expression
     * @param index the index in the BDD/AADD
     * @return the current set value (true, false, X)
     */
    private fun getConditionBoolSpec(index: Int): MutableList<XBool> {
        val id = model.builder.conds.indexes.keys.first { index == model.builder.conds.indexes[it] }
        return (model.get(elementId = UUID.fromString(id)) as Variable?)?.boolSpecs
            ?: throw SysMDError("Condition not found; internal issue in getConditionBoolSpec")
    }

    private fun checkAgainstBoolSpec(value: Boolean, spec: XBool): Boolean {
        return when (spec) {
            XBool.True -> value
            XBool.False -> value.not()
            XBool.X -> true
            else -> throw Exception("checkAgainstBoolSpec: NaB, AF")
        }
    }
}
