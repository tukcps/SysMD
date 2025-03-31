package com.github.tukcps.sysmd.model.expression.functions

import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.functions.ite
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session

/**
 * Computes the range of a property given as parameter in all parts.
 * I.e. if parts have the values 1,2,3, an AADD containing 1, 2, 3
 * will be built.
 */
class  AstByParts(model: Session, namespace: Namespace, args: ArrayList<AstNode>) :
    AstFunction("byParts", model, 1, args) {

    private val inNameSpace: Namespace = namespace
    private val propertyName: QualifiedName = (getParam(0) as AstLeaf).qualifiedName!!

    /**
     * Searches in all parts for the respective property and sets it in this element to the ITE-combination of
     * all found properties in subclasses.
     */
    override fun evalUp() {
        var ownedElements = inNameSpace.getOwnedElementsOfType<Feature>().filter { it.variable == null }
        if (ownedElements.isNotEmpty()) {
            val firstOwnedElement = ownedElements.first()
            ownedElements = ownedElements.drop(1)
            val quantity = firstOwnedElement.resolve<Feature>(propertyName)?.variable!!.vectorQuantity
            var result: DD<*> = quantity.values[0].clone()
            for (part in ownedElements) {
                // TODO: generate a variable for it!
                val chooser = model.builder.variable("choose_+${part.qualifiedName}", inNameSpace.qualifiedName+"::"+propertyName, true)
                val newPartProperty = part.resolveVar(propertyName)
                    ?: throw SemanticError("Missing value $propertyName in ${part.qualifiedName}")
                if (newPartProperty.vectorQuantity.unit != quantity.unit)
                    model.report(inNameSpace, "Different units in different subclasses")
                result = chooser.ite(result.clone(), newPartProperty.vectorQuantity.values[0])
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
        //TODO Add Vectors to byParts
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("BySubclasses is not possible with Vectors")
        val type = inNameSpace.resolve<Feature>(propertyName)?.type?.firstOrNull()?.ref

        if (type != null && type.model?.builder != model.builder)
            throw Exception("Internal error -- Mix of two models?")

        upQuantity = when {
            type == null -> throw SemanticError("Null type in function bySubclasses")
            type.specializes(model.repo.booleanType) -> Quantity(model.builder.Bool)
            type.specializes(model.repo.integerType) -> Quantity(model.builder.Integers)
            type.specializes(model.repo.realType) -> Quantity(model.builder.Reals, "?")
            else -> throw Exception("Unknown data type in function bySubclasses: '${type.qualifiedName}'")
        }
        downQuantity = upQuantity.clone()
    }

    /**
     * Does a depth-first traversal and applies the lambda parameter block.
     * @param receiver the ast node that is visited
     * @param block the lambda that is applied depth-first
     */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R {
        val ownedElements = inNameSpace.getOwnedElementsOfType<Feature>()
        for (part in ownedElements) {
            val ast = part.resolveVar(propertyName)?.ast
            if (ast != null) withDepthFirst(ast, block)
        }
        return block()
    }

    override fun evalDown() {
        val ownedElements = inNameSpace.getOwnedElementsOfType<Feature>()
        if (ownedElements.isNotEmpty()) {
            for (part in ownedElements) {
                val partProperty = part.resolveVar(propertyName)
                    ?: throw SemanticError("Missing value $propertyName in ${part.qualifiedName}")
                partProperty.vectorQuantity = partProperty.vectorQuantity.constrain(downQuantity)
            }
        }
    }

    override fun evalDownRec() {
        evalDown()
    }
}
