package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.services.session.Session
import java.util.*

/**
 * The semantic actions are called within the parsing process and create the abstract model
 * from the textual representation that is parsed.
 *
 * - model is the model that is created
 * - parser is the parser that is creating it; needed eventually for error reporting
 * - textualRepresentation is the textual representation that is parsed
 * - prefixes is a set in which the parser adds the prefixes of an element that is generated
 * - owners is a stack that holds the nested ownership of an element. On the stack we have "Resolved" classes
 *   that can be either simple names, qualified names, or a reference to an already known owner.
 *   This is a bit more tricky that in common SysML v2 as we allow arbitrary order of elements and adding something
 *   to existing (or, as order is arbitrary also not-yet-existing) packages or namespaces.
 */
interface ActionsContext {
    val model: Session
    val textualRepresentation: TextualRepresentation
    var visibilityKind: Token.Kind
    val prefixes: MutableSet<Token.Kind>
    val owners: Stack<Resolved<Element>>

    fun pushOwner(owner: Resolved<Element>)
    fun popOwner(): Resolved<Element>

    /** returns the fully qualified name of the owner in the current parse run, derived from the owner's stack */
    fun ownerName(): QualifiedName

    /** creates a fully qualified name considering the current scope */
    fun toEffectiveName(name: QualifiedName): QualifiedName

    /**
     * Adds a ReferenceSubsetting relationship.
     * @param owner name of the referencing feature's owner
     * @param pathFromOwnerToReferencingFeature path to the referencing feature's owner, can be combined with owner
     * @param referencedFeature qualified name of the referenced feature
     */
    fun addReferenceSubsetting(owner: Element, pathFromOwnerToReferencingFeature: QualifiedName?, referencedFeature: QualifiedName)
    fun addFeatureTyping(owner: Feature, type: QualifiedName): FeatureTyping
    fun addMultiplicity(owner: Feature, integerRange: IntegerRange): Multiplicity
    fun initOwners()
    fun addRedefinition(owner: Feature, pathFromOwnerToRedefinedFeature: QualifiedName?, redefinedFeature: QualifiedName)
    fun addFeature(owner: Element, feature: Feature, type: QualifiedName, rangeOfMultiplicity: IntegerRange)
    fun addSpecialization(owner: Type, type: QualifiedName): Specialization
    fun addAnnotation(owner: AnnotatingElement, annotatedElement: Element): Annotation
    fun handleFunctionCall(function: QualifiedName, param: ArrayList<AstNode>, semantics: SemanticActions): AstNode

    // Context if string of an expression is re-evaluated
    var namespace: Namespace          // owning namespace of expression
    var expression: Feature?          // owning feature of an expression

    val generateAnnotations: Boolean
}