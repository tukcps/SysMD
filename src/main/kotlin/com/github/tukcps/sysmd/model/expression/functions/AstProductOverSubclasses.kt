package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.TIMES
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD

/**
 * The productOverSubclasses function with parameter propertyAST.
 * The function takes a single parameter that is either the name of a property of components
 * or a calculation with some properties. These properties must all be contained in the same subclasses;
 * otherwise it is not possible
 * The property is searched in each of its elements.
 * - if it is found in an element connected via isA-parts, the value is used.
 * - if it is not found in an element connected via has-parts,
 *   it is applied to its elements recursively.
 */
internal class AstProductOverSubclasses(
    model: Session,
    private val namespace: Namespace,
    private var propertyAst: List<AstNode>,
    private var transitive: Boolean
) :
    AstAggregationFunction("productOverSubclasses", model) {

    private var generatedAst: AstNode? = null

    /**
     * Initialization; starts from bottom-up
     */
    override fun initialize() {
        upQuantity = Quantity(model.builder.Reals, "?")
        downQuantity = upQuantity
        if (propertyAst.size != 1)
            model.status.error(message = "function 'productOverSubclasses' expects one parameter", kind = Issue.Kind.ERROR_SEMANTIC, element = namespace)
        if (namespace is Type)
            generatedAst = model.initProductSubclasses(namespace, propertyAst.first(), transitive)
        else {
            generatedAst = null
            model.status.error("function 'productOverSubclasses' must be called from type", kind = Issue.Kind.ERROR_SEMANTIC, element = namespace)
        }
        generatedAst?.evalUpRec()
        evalUpRec()
        downQuantity = upQuantity.clone()
    }


    /**
     * Just compute the AST as set up in the init section.
     * Still, no support for integers, requires adding operators Real * Int on dD
     **/
    override fun evalUp() {
        generatedAst!!.evalUpRec()
        upQuantity = generatedAst!!.upQuantity
    }


    /**
     * Evaluate the properties of all owned elements.
     */
    override fun evalUpRec() {
        val ownedElements = namespace.getOwnedElementsOfType<Feature>().mapNotNull { it.variable }
        for (elem in ownedElements) {
            try {
                elem.ast?.evalUp()
            } catch (_: Exception) {
            }
        }
        evalUp()
    }


    /** Just compute the AST as set up in the init section.*/
    override fun evalDown() {
        val resultingSum = downQuantity
        //only do evalDown, if value is ready (interval should not be empty)
        val resultIsReady = when (resultingSum.values[0]) {
            is AADD -> !resultingSum.values.any { it.asAadd().getRange().isEmpty() }
            is IDD -> !resultingSum.values.any { it.asIdd().getRange().isEmpty() }
            else -> false
        }
        if (resultIsReady) {
            // Set downQuantity to the root of the generatedAST
            generatedAst!!.upQuantity = resultingSum
            generatedAst!!.downQuantity = resultingSum
            generatedAst!!.evalDownRec()
            // Iterate through all leafs of the generatedAST and update downQuantity of the associated ValueFeature
            for (leaf in generatedAst!!.getLeaves().filter { it.qualifiedName != null }) {
                val valueFeature = model.global.resolveVar(leaf.qualifiedName!!)
                when (leaf.downQuantity.values[0]) {
                    is AADD -> valueFeature!!.vectorQuantity = valueFeature.vectorQuantity.constrain(leaf.downQuantity)
                    is IDD -> valueFeature!!.vectorQuantity = valueFeature.vectorQuantity.constrain(leaf.downQuantity)
                    else -> {}
                }
            }
        }
    }

    override fun getDependentPropertyStrings(): Set<String> {
        return getPartDependencies(namespace as Type, propertyAst.first())
    }

    override fun clone(): AstProductOverSubclasses {
        return AstProductOverSubclasses(model, namespace, listOf(propertyAst.first().clone()), transitive)
    }
}


/**
 * Function that generates an AST for a Product over a composition.
 * The function considers all owned elements and searches in these elements for propertyName.
 * Then, it builds an AST that computes the Product.
 */
fun Session.initProductSubclasses(
    element: Type,
    propertyAST: AstNode,
    transitive: Boolean,
    isReal: Boolean = true
): AstNode {
    var ast: AstNode? = null
    var isRealProduct = isReal //indicates if the property is a real or an int
    for (subclass in element.subtypes) {
        //iterate through all leafs of the propertyAST (which do not include only a number) to find the value for the properties.
        var newAstNode: AstNode = propertyAST.clone()
        for (leaf in newAstNode.getLeaves().filter { it.qualifiedName != null }) {
            // Find property with propertyName owned by element ...
            //TODO Could cause problems with inheritance or imports
            val variable = global.resolveVar(subclass.qualifiedName + "::" + leaf.qualifiedName)
            if (variable != null) {
                leaf.upQuantity = variable.vectorQuantity
                leaf.downQuantity = variable.vectorQuantity
                leaf.qualifiedName = variable.name
                leaf.resolvedName = variable.name
                if (leaf.upQuantity.values[0] is IDD) isRealProduct = false
            } else if (transitive) { // Transitive: search property in parts
                newAstNode = this.initProductSubclasses(subclass, propertyAST, true, isRealProduct)
                break   // if one property of a leaf is not included in the current Element, there is no need to search
                // for the properties of the other leaves, because all properties of one propertyAST must contain to the same element
                // without this break statement, subclasses would be added multiple times to the ast
            } else
                break // no further look in subclasses because transitive search is not enabled
        }
        if (newAstNode.toString() != propertyAST.toString())
            ast = if (ast == null) newAstNode else com.github.tukcps.sysmd.model.expression.AstBinOp(newAstNode, TIMES, ast)
    }

    return ast ?: if (isRealProduct)
        AstLeaf(this, Quantity(builder.real(1.0), "?"))
    else
        AstLeaf(this, Quantity(builder.integer(1)))
}

fun getPartDependencies(element: Type, propertyAST: AstNode): Set<String> {
    val result = mutableSetOf<String>()
    element.subtypes.forEach { subclass ->
        for (leaf in propertyAST.getLeaves().filter { it.qualifiedName != null }) {
            result.add(leaf.qualifiedName as String)
        }
    }
    return result
}
