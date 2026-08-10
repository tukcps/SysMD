package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD

/**
 * @class AstLeaf
 * A leaf of the abstract syntax tree with the following properties:
 * - its main information is in a property data type
 * - it is entered in the symbol table
 *   (unless it is a number/bool literal)
 */
class AstLeaf private constructor (
    model: Session,
    var literalVal: VectorQuantity?,         // The value, if a literal
    var qualifiedName: QualifiedName?,       // (relative) qualified name
    var namespace: Namespace = model.global, // Owning namespace
) : AstNode(model) {

    /**
     * Fully qualified name of the respective feature.
     * It can be used as a key to a variable.
     */
    var resolvedName: String? = null

    /**
     * Resolves the qualified name in namespace and returns the path of the element found.
     * Requires specific variant of resolve once there are no clones.
     */
    private fun resolveToPath(): String =
        namespace.resolveVar(qualifiedName!!)?.path
            ?: throw ElementNotFoundException(namespace, "Could not resolve name '$qualifiedName'")

    val variable: Variable?
        get() = if (resolvedName != null) {
                model.solver.getVariable(resolvedName!!)
            } else null

    // Ugly fix; only for user-defined function until a better solution
    var node: AstNode? = null

    /**
     * If the leaf is a literal, we can initialize the values now.
     */
    init {
        if (literalVal != null) {
            upQuantity = literalVal!!.clone()
            downQuantity = literalVal!!.clone()
        }
    }

    /**
     * Identifies, if a reference, the respective feature and it's variable.
     * The feature is not necessarily reified;
     * it might only exist if inheritance is done by cloning.
     */
    override fun initialize() {
        if (qualifiedName != null) {
            resolvedName = resolveToPath()
            upQuantity = variable!!.vectorQuantity
            downQuantity = variable!!.vectorQuantity
        } else {
            // No variable, hence we have a literal.
            if(literalVal!=null) {
                upQuantity = literalVal!!
                downQuantity = literalVal!!
            }
        }
        if (node != null) {
            upQuantity = node!!.upQuantity
            downQuantity = node!!.downQuantity
        }
        evalUpRec()
    }

    /**
     * A constructor that creates a leaf for a literal value
     * @param model the model
     * @param quantity the value of the literal
     */
    constructor(model: Session, quantity: VectorQuantity): this(model, quantity, null)

    /**
     * Hotfix ... dirty. To help user defined functions.
     */
    constructor(model: Session, node: AstNode): this(model, null, null) {
        this.node = node
    }

    /**
     * A constructor that saves the scope information from parsing for later initialization
     * after the complete file has been read. The constructor saves element id and name of the
     * property, and creates the property entries value etc. later during lateInit().
     * @param namespace the element in which the property is queried.
     * @param qualifiedName the path of the property, relative from the element.
     */
    constructor(namespace: Namespace, qualifiedName: QualifiedName, model: Session)
            : this(model, null, qualifiedName, namespace) {
        require(qualifiedName != "")
        this.namespace = namespace
        this.qualifiedName = qualifiedName
    }

    /**
     * A constructor that is directly invoked with a variable.
     */
    constructor(model: Session, variable: Variable)
            : this(model, null, null) {
        this.qualifiedName = variable.path
    }


    // Eval down stops recursion.
    override fun evalDownRec() {
        evalDown()
        node?.evalDownRec()
    }

    /**
     * Eval down computes intersection with specified range as value.
     * Before computing intersection with the range specification, both down value and
     * value must be converted to the same unit (SI Unit). (is now checked at the beginning)
     * At leaves, the evalDown computes the INTERSECTION between the down-propagated value and the
     * already constrained value.
     */
    override fun evalDown() {
        if (node?.upQuantity != null ) {
            upQuantity = node!!.upQuantity
        }

        if (variable != null) {
            if (variable?.baseType == Variable.BaseType.Real) {
                // TODO: check is only hot fix ... (?)
                if (! (downQuantity.value.asAadd().maxIsInf && downQuantity.value.asAadd().minIsInf) ) {
                    variable!!.vectorQuantity = downQuantity.constrain(
                        variable!!.vectorQuantity,
                        variable!!.rangeSpecs,
                        variable!!.unitSpec
                    )
                }
                if (variable!!.satisfyAll) {
                    if (variable!!.rangeSpecs.size != downQuantity.values.size && variable!!.rangeSpecs.size != 1)
                        throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match Constraint size of ${variable!!.rangeSpecs.size}")
                    if (variable!!.rangeSpecs.size == downQuantity.values.size)
                        if (variable!!.rangeSpecs.indices.any { variable!!.rangeSpecs[it] !in (downQuantity.values[it] as AADD).getRange() })
                            model.status.warn(Issue.Kind.WARN_INCONSISTENCY,"Cannot be satisfied for all values for ${variable!!.path}")
                }
                variable!!.checkEvent()
            }
            if (variable?.baseType == Variable.BaseType.Int) {
                variable!!.vectorQuantity = downQuantity.constrain(variable!!.vectorQuantity).clone()
                if (variable!!.satisfyAll) {
                    if (variable!!.intSpecs.size != downQuantity.values.size && variable!!.intSpecs.size != 1)
                        throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match Constraint size of ${variable!!.intSpecs.size}")
                    if (variable!!.intSpecs.size == downQuantity.values.size)
                        if (variable!!.intSpecs.indices.any { variable!!.intSpecs[it] !in (downQuantity.values[it] as IDD).getRange() })
                            model.status.warn(
                                kind = Issue.Kind.WARN_INCONSISTENCY,
                                message = "Cannot be satisfied for all values in ${variable!!.path}",
                            )
                }
                variable!!.checkEvent()
            }
            if(isString){
                variable!!.vectorQuantity = downQuantity.constrainString(variable!!.vectorQuantity)
                variable!!.checkEvent()
            }
        }
    }


    /**
     *  EvalUp just stops recursion as we are at a leaf.
     */
    override fun evalUpRec() {
        if (node != null)
            node!!.evalUpRec()
        evalUp()
    }

    /**
     * Propagates the variable or literal value up.
     * The value is initialized whenever a user specifies a value;
     * constraint propagation only restricts this value to an intersection of it.
     * All values are in the unit in SI representation.
     */
    override fun evalUp() {
        if (variable != null) {
            upQuantity = variable!!.vectorQuantity
        }
        if (node?.upQuantity != null)
            upQuantity = node!!.upQuantity
    }


    /** Executes a block of statements on each AstNode in an Ast */
    override fun <R> runDepthFirst(block: AstNode.() -> R): R =
        this.run(block)

    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R =
        receiver.block()


    /** Used in Aggregation functions */
    override fun clone(): AstLeaf {
        return AstLeaf(model, literalVal, qualifiedName, namespace).also {
            it.resolvedName = resolvedName
            // If it is initialized, the value feature is not null, or literalval is not null.
            if (it.literalVal != null) {
                it.upQuantity = upQuantity.clone()
                it.downQuantity = downQuantity.clone()
            }
            if (it.resolvedName != null) {
                it.upQuantity = upQuantity.clone()
                it.downQuantity = downQuantity.clone()
            }
        }
    }

    // Returns AST as simple expression string
    override fun toExpressionString(): String {
        return if (qualifiedName == null) { // A literal ...
            if (literalVal!!.values[0] is AADD) {
                var valStr = literalVal!!.values.toString()
                if (valStr.split(".").size == 1) valStr += ".0"
                var unitStr = literalVal!!.unit.toString()
                unitStr = if (unitStr != "1") unitStr else ""
                "$valStr $unitStr"
            } else
                literalVal.toString()
        } else
            qualifiedName!!
    }


    override fun toString(): String {
        return try {
            // Literal?
            if (literalVal != null) "AstLeaf($upQuantity)"
            else {
                // Property/variable, defined by identification & ElementId.
                // After initialization, we also have upQuantity initialized with value.
                if (qualifiedName == null) "AstLeaf($upQuantity)"
                else "AstLeaf($qualifiedName = $upQuantity)"
            }
        } catch (_: Exception) {
            if (literalVal != null)
                "AstLeaf($literalVal)"
            else
                "AstLeaf(${namespace.qualifiedName} : ${qualifiedName})"
        }
    }
}
