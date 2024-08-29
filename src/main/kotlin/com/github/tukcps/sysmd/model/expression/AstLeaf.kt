package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.reportInfo
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session


/**
 * @class AstLeaf
 * A leaf of the abstract syntax tree with the following properties:
 * - its main information is in a property data type
 * - it is entered in the symbol table
 *   (unless it is a number/bool literal)
 */
class AstLeaf private constructor (
    model: Session,
    var literalVal: VectorQuantity?,  // The value, if a literal
    var namespace: Namespace?,        // Owning namespace
    var qualifiedName: QualifiedName?
) : AstNode(model) {

    // After name resolution:
    var feature: Feature? = null                  // Reference to feature after initialization.
    val variable: Variable?
        get() = feature as? Variable ?: feature?.variable

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

    override fun initialize() {
        if (qualifiedName != null) {
            feature = if(namespace==null) {
                feature?.resolve<Feature>(qualifiedName!!)
                    ?: throw ElementNotFoundException(feature, qualifiedName!!)
            } else {
                namespace!!.resolve<Feature>(qualifiedName!!)
                    ?: throw ElementNotFoundException(namespace, qualifiedName!!)
            }
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
    constructor(model: Session, quantity: VectorQuantity): this(model, quantity, null, null)

    /**
     * Hotfix ... dirty. To help user defined functions.
     */
    constructor(model: Session, node: AstNode): this(model, null, null, null) {
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
            : this(model, null, namespace,qualifiedName) {
        require(qualifiedName != "")
        this.namespace = namespace
        this.qualifiedName = qualifiedName
    }

    /**
     * A constructor that is directly invoked with a variable.
     */
    constructor(model: Session, variable: Variable)
            : this(model, null, null, null) {
        this.feature = (variable as VariableImplementation).feature
        this.qualifiedName = variable.feature.qualifiedName
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
            if (isReal) {
                // TODO: check is only hot fix ... (?)
                if (! (downQuantity.value.asAadd().maxIsInf && downQuantity.value.asAadd().minIsInf) ) {
                    variable!!.vectorQuantity = downQuantity.constrain(
                        variable!!.vectorQuantity,
                        variable!!.rangeSpecs,
                        variable!!.unitSpec
                    )
                }
                if (variable!!.feature.direction == Feature.FeatureDirectionKind.OUT) {
                    if (variable!!.rangeSpecs.size != downQuantity.values.size && variable!!.rangeSpecs.size != 1)
                        throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match Constraint size of ${variable!!.rangeSpecs.size}")
                    if (variable!!.rangeSpecs.size == downQuantity.values.size)
                        if (variable!!.rangeSpecs.indices.any { variable!!.rangeSpecs[it] !in (downQuantity.values[it] as AADD).getRange() })
                            model.reportInfo(variable!!.feature, "Cannot be satisfied for all values.")
                }
                variable!!.checkEvent()
            }
            if (isInt) {
                variable!!.vectorQuantity = downQuantity.constrain(variable!!.vectorQuantity)
                if (variable!!.feature.direction == Feature.FeatureDirectionKind.OUT) {
                    if (variable!!.intSpecs.size != downQuantity.values.size && variable!!.rangeSpecs.size != 1)
                        throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match Constraint size of ${variable!!.rangeSpecs.size}")
                    if (variable!!.intSpecs.size == downQuantity.values.size)
                        if (variable!!.intSpecs.indices.any { variable!!.intSpecs[it] !in (downQuantity.values[it] as IDD).getRange() })
                            model.reportInfo(variable!!.feature, "Cannot be satisfied for all values.")
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


    override fun clone(): AstLeaf {
        return AstLeaf(model, literalVal, namespace, qualifiedName).also {
            it.feature = feature
            // If it is initialized, the value feature is not null, or literalval is not null.
            if (it.literalVal != null) {
                it.upQuantity = upQuantity.clone()
                it.downQuantity = downQuantity.clone()
            }
            if (it.feature != null) {
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
        } catch (e: Exception) {
            if (literalVal != null)
                "AstLeaf($literalVal)"
            else
                "AstLeaf(${namespace?.qualifiedName} : ${qualifiedName})"
        }
    }
}
