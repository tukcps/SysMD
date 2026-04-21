package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PLUS
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.TIMES
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.IDD

/**
 * The sumOverParts function with parameter propertyAST.
 * The function takes a single parameter that is either name of a property of components
 * or a calculation with some properties. These properties must all be contained in the same subclasses;
 * otherwise it is not possible
 * The property is searched in each of its elements.
 * - if it is found in an element connected via hasA-parts, the value is used.
 * - if it is not found in an element connected via has-parts,
 *   it is applied to its elements recursively.
 */
internal class AstSumOverParts(
    model: Session,
    private val namespace: Namespace,
    private var propertyAst: List<AstNode>,
    private var transitive: Boolean
) :
    AstAggregationFunction("sumOverParts", model) {

    private var generatedAst: AstNode? = null

    /**
     * Initialization; starts from bottom-up
     */
    override fun initialize() {
        upQuantity = Quantity(model.builder.Reals, "?")
        if (propertyAst.size != 1)
            throw SemanticError("function 'sumOverParts' expects one parameter")
        generatedAst = model.initSumOverComposition(namespace, propertyAst.first(), transitive)
        generatedAst!!.runDepthFirst { initialize() } //initialize Real fkt in AST
        generatedAst!!.evalUpRec()
        evalUpRec()
        downQuantity = upQuantity.clone()
    }


    /**
     * Compute the AST as set up in the init section.
     * Still, no support for integers; that requires adding operator Real * Int on dD
     **/
    override fun evalUp() {
        generatedAst!!.evalUpRec()
        upQuantity = generatedAst!!.upQuantity
    }


    /**
     * Evaluate the properties of all owned elements.
     */
    override fun evalUpRec() {
        val ownedElements = namespace.visibleMemberships().mapNotNull { it.member<Feature>() }.filter { it.specializes( model.repo.scalarType) }
        for (elem in ownedElements) {
            try {
                if (elem is Variable) {
                    elem.ast?.evalUp()
                } else
                    elem.variable?.ast?.evalUp()
            } catch (_: Exception) { }
        }
        evalUp()
    }


    /** Compute the AST as set up in the init section.*/
    override fun evalDown() {
        val resultingSum = downQuantity
        //only do evalDown; if the value is ready (interval should not be empty)
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

    override fun getDependentPropertyStrings(): Set<String> {
        // If generatedAst is already built (after initialize()), use the resolved fully-qualified
        // leaf paths so the topological sort in Solver.initVariables() can detect the real
        // dependencies (e.g. "Wire::wireFrontCamera::pathLength") instead of the bare property
        // name (e.g. "pathLength") that getSubclassDependencyStrings() would return.
        val fromGeneratedAst = generatedAst?.getLeaves()
            ?.mapNotNull { it.resolvedName }
            ?.toSet()
        if (!fromGeneratedAst.isNullOrEmpty()) return fromGeneratedAst
        return getSubclassDependencyStrings(namespace, propertyAst.first())
    }

    override fun clone(): AstSumOverParts {
        return AstSumOverParts(model, namespace, listOf(propertyAst.first().clone()), transitive)
    }
}


/**
 * Function that generates an AST for a Sum over a composition.
 * The function considers all owned elements and searches in these elements for propertyName.
 * Then, it builds an AST that computes the sum.
 */
fun Session.initSumOverComposition(element: Namespace, propertyAST: AstNode, transitive: Boolean, isReal: Boolean = true): AstNode {
    var ast: AstNode? = null
    var isRealSum = isReal //indicates if the property is a real or an int
    for (elementIterator in element.visibleMemberships().mapNotNull { it.member<Feature>() }.filterNot { it.specializes(repo.scalarType) }) {
        var newAstNode: AstNode = propertyAST.clone()
        var astNodeUsed = false
        for (leaf in newAstNode.getLeaves().filter { it.qualifiedName != null }) {
            // Find property with propertyName owned by element ...
            val variable = elementIterator.resolveVar(leaf.qualifiedName as String)
            if (variable != null) {
                leaf.upQuantity = variable.vectorQuantity
                leaf.downQuantity = variable.vectorQuantity
                leaf.qualifiedName = variable.path
                leaf.resolvedName = variable.path
                if (leaf.upQuantity.values[0] is IDD) isRealSum = false
                astNodeUsed = true
            } else if (transitive ) { // Transitive: search property in parts (not for ValueFeatures)
                val elementRef = elementIterator.type.first()
                newAstNode = initSumOverComposition(elementRef as Namespace, propertyAST, true, isRealSum)
                astNodeUsed = true
                break   // if one property of a leaf is not included in the current Element, there is no need to search
                // for the properties of the other leafs, because all properties of one propertyAST must contain to the same element
                // without this break statement, subclasses would be added multiple times to the ast
            } else
                break // no further look in parts because transitive search is not enabled
        }
        if (astNodeUsed) {
            val multiplicity = elementIterator.multiplicity()
            val multiplicityLeaf = if (multiplicity?.variable != null)
                AstLeaf(this, multiplicity.variable!!) else null
            val multiplicityConverted = if (isRealSum)
                if (multiplicityLeaf != null) AstReal(
                    this,
                    arrayListOf(multiplicityLeaf)
                ) else null //convert int node to real
            else
                multiplicityLeaf // is int
            ast = if (ast == null) {
                if (multiplicityConverted != null) AstBinOp(
                    newAstNode,
                    TIMES,
                    multiplicityConverted
                ) else newAstNode
            } else {
                if (multiplicityConverted != null) AstBinOp(
                    AstBinOp(newAstNode, TIMES, multiplicityConverted),
                    PLUS,
                    ast
                )
                else AstBinOp(newAstNode, PLUS, ast)
            }
        }
    }
    return ast ?: if (isRealSum)
        AstLeaf(this, Quantity(builder.real(0.0), "?"))
    else
        AstLeaf(this, Quantity(builder.integer(0)))
}
