package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.functions.ite

/**
 * Computes the range of a property given as a parameter in all subclasses.
 * I.e., if subclasses have the values 1,2,3, an AADD containing 1, 2, 3
 * will be built.
 */
class AstBySpecializations(model: Session, namespace: Namespace, args: ArrayList<AstNode>) :
    AstFunction("bySpecializations", model, 1, args) {

    private val inNameSpace: Type = if (namespace is Type) namespace else {
        model.status.error("bySpecializations not applicable in namespace that is not a type", element = namespace, kind = Issue.Kind.ERROR_SEMANTIC)
        model.anything
    }
    private val propertyName: QualifiedName = (getParam(0) as AstLeaf).qualifiedName!!

    /**
     * Searches in all subclasses for the respective property and sets it in this element to the ITE-combination of
     * all found properties in subclasses.
     */
    override fun evalUp() {
        var specializations = inNameSpace.subtypes.toMutableList()
        if (specializations.isNotEmpty()) {
            val firstSubclass = specializations.first()
            specializations = specializations.drop(1).toMutableList()
            val quantity = firstSubclass.resolveVar(propertyName)!!.vectorQuantity
            var result: DD<*> = quantity.values[0].clone()
            for (subclass in specializations) {
                // TODO: generate a property for it!
                val chooser = model.builder.variable("choose_+${subclass.qualifiedName}", inNameSpace.qualifiedName+"::"+propertyName, true)
                val newSubclassProperty = subclass.resolveVar(propertyName)
                    ?: throw SemanticError("Missing value $propertyName in ${subclass.qualifiedName}")
                if (newSubclassProperty.vectorQuantity.unit != quantity.unit)
                    model.status.error("different units in different subclasses", element = inNameSpace)
                result = chooser.ite(result, newSubclassProperty.vectorQuantity.values[0])
            }
            this.upQuantity = VectorQuantity(result, quantity.unit.clone())
        }
    }

    /**
     * Sets the result data type for static type checking; as the subclasses might
     * not yet be known, the function just uses the declared type of the value.
     * NO EVAL-UP is done; this function shall only be used alone.
     */
    override fun initialize() {
        //TODO Add Vectors to bySubclasses
        if (getParam(0).upQuantity.values.size != 1)
            throw VectorDimensionError("BySubclasses is not supported with Vectors")
        val variable = inNameSpace.resolveVar(propertyName)

        upQuantity = when(variable?.baseType) {
            Variable.BaseType.Bool -> Quantity(model.builder.Bool)
            Variable.BaseType.Int -> Quantity(model.builder.Integers)
            Variable.BaseType.Real -> Quantity(model.builder.Reals, "?")
            else -> throw SemanticError("Undefined type in function bySubclasses: '${variable?.name}'", variable?.feature)
        }
        downQuantity = upQuantity.clone()
    }

    /**
     * Does a depth-first traversal and applies the lambda parameter block.
     * @param receiver the ast node that is visited
     * @param block the lambda that is applied depth-first
     */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R {
        for (subclass in inNameSpace.subtypes) {
            val ast = subclass.resolveVar(propertyName)?.ast
            if (ast != null) withDepthFirst(ast, block)
        }
        return block()
    }

    override fun evalDown() {
        if (downQuantity.value is AADD || downQuantity.value is IDD) {
            inNameSpace.subtypes.forEach { subtype ->
                val partProperty = subtype.resolveVar(propertyName)
                    ?: throw SemanticError("Missing value $propertyName in ${subtype.qualifiedName}", inNameSpace)
                partProperty.vectorQuantity = partProperty.vectorQuantity.constrain(downQuantity)
            }
        }
    }

    override fun evalDownRec() {
        evalDown()
    }
}
