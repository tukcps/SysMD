package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.DD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.functions.ite
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDInternalError
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

/**
 * Computes the range of a property given as parameter in all subclasses.
 * I.e. if subclasses have the values 1,2,3, an AADD containing 1, 2, 3
 * will be built.
 */
class AstBySubclasses(model: Session, namespace: Namespace, args: ArrayList<AstNode>) :
    AstFunction("bySubclasses", model, 1, args) {

    private val inNameSpace: Namespace = namespace
    private val propertyName: QualifiedName = (getParam(0) as AstLeaf).qualifiedName!!

    /**
     * Searches in all subclasses for the respective property and sets it in this element to the ITE-combination of
     * all found properties in subclasses.
     */
    override fun evalUp() {
        var subclasses = model.getSubclasses(inNameSpace)
        if (subclasses.isNotEmpty()) {
            val firstSubclass = subclasses.first()
            subclasses = subclasses.drop(1)
            val quantity = firstSubclass.resolveVar(propertyName)!!.vectorQuantity
            var result: DD = quantity.values[0].clone()
            for (subclass in subclasses) {
                // TODO: generate a property for it!
                val chooser = model.builder.variable("choose_+${subclass.qualifiedName}", inNameSpace.qualifiedName+"::"+propertyName, true)
                val newSubclassProperty = subclass.resolveVar(propertyName)
                    ?: throw SemanticError("Missing value $propertyName in ${subclass.qualifiedName}")
                if (newSubclassProperty.vectorQuantity.unit != quantity.unit)
                    model.report(inNameSpace, "different units in different subclasses")
                result = chooser.ite(result, newSubclassProperty.vectorQuantity.values[0])
            }
            this.upQuantity = VectorQuantity(result, quantity.unit.clone())
        }
    }

    /**
     * Sets the result data type for static type checking; as the subclasses might
     * not yet be known, the function just uses the declared type the value.
     * NO EVAL-UP is done; this function shall only be used alone.
     */
    override fun initialize() {
        //TODO Add Vectors to bySubclasses
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("BySubclasses is not supported with Vectors")
        val general = inNameSpace.resolve<Feature>(propertyName)?.type?.mapNotNull { it.ref }

        general?.forEach { type ->
            if (type.model?.builder != model.builder)
                throw SysMDInternalError("Internal error -- different builders; mix of two models?", null, null, null)

            upQuantity = when {
                type.specializes(model.repo.booleanType) -> Quantity(model.builder.Bool)
                type.specializes(model.repo.integerType) -> Quantity(model.builder.Integers)
                type.specializes(model.repo.realType) -> Quantity(model.builder.Reals, "?")
                else -> throw Exception("Unknown data type in function bySubclasses: '${type.qualifiedName}'")
            }
        }
        downQuantity = upQuantity.clone()
    }

    /**
     * Does a depth-first traversal and applies the lambda parameter block.
     * @param receiver the ast node that is visited
     * @param block the lambda that is applied depth-first
     */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R {
        val subclasses = model.getSubclasses(inNameSpace)
        for (subclass in subclasses) {
            val ast = subclass.resolveVar(propertyName)?.ast
            if (ast != null) withDepthFirst(ast, block)
        }
        return block()
    }

    override fun evalDown() {
        if (downQuantity.value is AADD || downQuantity.value is IDD) {
            val subclasses = model.getSubclasses(inNameSpace)
            if (subclasses.isNotEmpty()) {
                for (subclass in subclasses) {
                    val partProperty = subclass.resolveVar(propertyName)
                        ?: throw SemanticError("Missing value $propertyName in ${subclass.qualifiedName}")
                    partProperty.vectorQuantity = partProperty.vectorQuantity.constrain(downQuantity)
                }
            }
        }
    }

    override fun evalDownRec() {
        evalDown()
    }

    override fun toExpressionString() = "bySubclasses($propertyName)"
}
