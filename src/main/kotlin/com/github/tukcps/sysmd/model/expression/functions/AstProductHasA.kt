package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.TIMES
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.resolve.findAllOwnedElements
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

/**
 * The ProductHasA function with parameter propertyAST.
 * The function takes a single parameter that is either name of a property of components
 * or a calculation with some properties.
 * These properties must all be contained in the same subclasses,
 * otherwise it is not possible.
 * The property is searched in each of its elements.
 * - if it is found in an element connected via hasA-parts, the value is used.
 * - if it is not found in an element connected via has-parts,
 *   it is applied to its elements recursively.
 */
internal class AstProductHasA(
    model: Session,
    private val namespace: Namespace,
    private var propertyAst: List<AstNode>,
    private var transitive: Boolean
) :
    AstAggregationFunction("productOverParts", model) {

    private var generatedAst: AstNode? = null

    override var root: AstNode? = null
        set(value) {
            generatedAst?.root = value
            field = value
        }

    /**
     * Initialization; starts from bottom-up
     */
    override fun initialize() {
        upQuantity = Quantity(model.builder.Reals, "?")
        if (propertyAst.size != 1)
            throw SemanticError("function 'productOverParts' expects one parameter")
        generatedAst = model.initProductParts(namespace, propertyAst.first(), transitive)
        generatedAst!!.root = root
        generatedAst!!.runDepthFirst { initialize() } //initialize Real fkt in AST
        generatedAst!!.evalUpRec()
        evalUpRec()
        downQuantity = upQuantity.clone()
    }


    /**
     * Just computes the AST as set up in the init section.
     * Still, no support for integers requires adding operators Real * Int on dD
     **/
    override fun evalUp() {
        generatedAst!!.evalUpRec()
        upQuantity = generatedAst!!.upQuantity
    }


    /**
     * Evaluate the properties of all owned elements.
     */
    override fun evalUpRec() {
        val expressions = namespace.getOwnedElementsOfType<Feature>().mapNotNull { it.variable }
        for (elem in expressions) {
            try {
                elem.ast?.evalUp()
            } catch (ignore: Exception) {
            }
        }
        evalUp()
    }


    /** Just computes the AST as set up in the init section.*/
    override fun evalDown() {
        val resultingSum = downQuantity
        //only do evalDown, if the value is ready (interval should not be empty)
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
            //Iterate through all leafs of the generatedAST and update downQuantity of the associated ValueFeature
            for (leaf in generatedAst!!.getLeaves().filter { it.qualifiedName != null }) {
                val valueFeature = model.global.resolveVar(leaf.qualifiedName!!)
                if (valueFeature != null) {
                    when (leaf.downQuantity.values[0]) {
                        is AADD -> valueFeature.vectorQuantity =
                            valueFeature.vectorQuantity.constrain(leaf.downQuantity)

                        is IDD -> valueFeature.vectorQuantity = valueFeature.vectorQuantity.constrain(leaf.downQuantity)
                        else -> {}
                    }
                }
            }
        }
    }

    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        val elements = namespace.getOwnedElementsOfType<Element>()
        for (element in elements) {
//            try { model.getProperty(model.selfId, Identification(null, propertyName))!!.ast!!.runDepthFirst(block) }
//            catch (ignore: Exception){ } // No property found, we can do nothing or recurse.
        }
        return this.run(block)
    }

    override fun getDependentPropertyStrings(): Set<String> {
        return getSubclassDependencyStrings(namespace, propertyAst.first())
    }

    override fun clone(): AstProductHasA {
        return AstProductHasA(model, namespace, listOf(propertyAst.first().clone()), transitive)
    }
}


/**
 * Function that generates an AST for a Product over a composition.
 * The function considers all owned elements and searches in these elements for propertyName.
 * Then, it builds an AST that computes the Product.
 */
fun Session.initProductParts(
    element: Namespace,
    propertyAST: AstNode,
    transitive: Boolean,
    isReal: Boolean = true
): AstNode {
    var ast: AstNode? = null
    var isRealProduct = isReal //indicates if the property is a real or an int
    for (elementIterator in element.findAllOwnedElements().filterIsInstance<Feature>().filterNot { it is Variable || it.variable is Variable }) {
        var newAstNode: AstNode = propertyAST.clone()
        for (leaf in newAstNode.getLeaves().filter { it.qualifiedName != null }) {
            // Find property with propertyName owned by element ...
            val ownedProperty = (elementIterator as Namespace).resolve<Feature>(leaf.qualifiedName as String)
            val variable = if (ownedProperty is Variable) ownedProperty else ownedProperty?.variable
            if (variable != null) {
                leaf.upQuantity = variable.vectorQuantity
                leaf.downQuantity = variable.vectorQuantity
                leaf.qualifiedName = variable.name
                leaf.feature = ownedProperty
                if (leaf.upQuantity.values[0] is IDD) isRealProduct = false
            } else if (transitive && elementIterator !is Variable) { // Transitive: search property in parts ...
                val elementRef = elementIterator.type.firstOrNull()?.ref
                newAstNode = initProductParts(elementRef as Namespace, propertyAST, true, isRealProduct)
                break
            } else {
                break // no further look in parts because transitive search is not enabled
            }
        }
        if (newAstNode.toString() != propertyAST.toString()) {
            val multiplicity = elementIterator.multiplicityProperty
            val multiplicityLeaf = if (multiplicity?.variable != null) AstLeaf(this, multiplicity.variable!!) else null
            val multiplicityConverted = if (isRealProduct)
                if (multiplicityLeaf != null) AstReal(
                    this,
                    arrayListOf(multiplicityLeaf)
                ) else null //convert int node to real
            else
                multiplicityLeaf // is int
            ast = if (ast == null) {
                if (multiplicityConverted != null) AstPower(
                    this,
                    arrayListOf(newAstNode, multiplicityConverted)
                ) else newAstNode
            } else {
                if (multiplicityConverted != null) com.github.tukcps.sysmd.model.expression.AstBinOp(
                    AstPower(
                        this,
                        arrayListOf(newAstNode, multiplicityConverted)
                    ), TIMES, ast
                )
                else com.github.tukcps.sysmd.model.expression.AstBinOp(newAstNode, TIMES, ast)
            }
        }
    }
    return ast ?: if (isRealProduct)
        AstLeaf(this, Quantity(builder.scalar(1.0), "?"))
    else
        AstLeaf(this, Quantity(builder.scalar(1)))
}

fun getSubclassDependencyStrings(element: Namespace, propertyAST: AstNode): Set<String> {
    val result = mutableSetOf<String>()
    for (elementIterator in element.findAllOwnedElements()) {
        if (elementIterator is Feature && elementIterator !is Variable) {
            for (leaf in propertyAST.getLeaves().filter { it.qualifiedName != null }) {
                result.add(leaf.qualifiedName as String)
            }
        }
    }
    return result
}
