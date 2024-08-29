package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.expression.InvariantActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


/**
 * For each production rule of the KerML grammar, there is a semantic action that does something.
 * This allows us to create different implementations for different purposes of the parser.
 */
interface SemanticActions: ActionsContext {
    fun namespaceActions(): NamespaceActions?
    fun packageActions(): PackageActions?
    fun commentActions(): CommentActions?
    fun classActions(): ClassActions?
    fun dependencyActions(): DependencyActions?
    fun documentationActions(): DocumentationActions?
    fun textualRepresentationActions(): TextualRepresentationActions?
    fun typeActions(): TypeActions?
    fun connectorActions(): ConnectorActions?
    fun datatypeActions(): DataTypeActions?
    fun importActions(): ImportActions?
    fun featureActions(): FeatureActions?
    fun functionActions(): FunctionActions?
    fun associationActions(): AssociationActions?
    fun constraintActions(): FeatureActions?
    fun attributeActions(): FeatureActions?
    fun conditionalExpressionActions(): ConditionalExpressionActions?
    fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode): ConditionalExpressionActions?
    fun invariantActions(): InvariantActions?
    fun calculationActions(): CalculationActions?

    fun directionFromPrefixes(): Feature.FeatureDirectionKind
}

class SemanticActionsImplementation(
    model: Session,
    textualRepresentation: TextualRepresentation,
    owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack of Identifications of all nested elements
    generateAnnotations: Boolean = false
): SemanticActions, ActionsContextImplementation(
    model=model, textualRepresentation=textualRepresentation, owners = owners, generateAnnotations = generateAnnotations
) {
    override fun namespaceActions() = NamespaceActions(this)
    override fun packageActions() = PackageActions(this)
    override fun commentActions() =  CommentActions(this)
    override fun classActions() = ClassActions(this)
    override fun dependencyActions() = DependencyActions(this)
    override fun documentationActions() = DocumentationActions(this)
    override fun textualRepresentationActions() = TextualRepresentationActions(this)
    override fun datatypeActions() = DataTypeActions(this)
    override fun typeActions() = TypeActions(this)
    override fun connectorActions() = ConnectorActions(this)
    override fun importActions() = ImportActions(this)
    override fun featureActions() = FeatureActions(this)
    override fun functionActions() = FunctionActions(this)
    override fun associationActions() = AssociationActions(this)
    override fun constraintActions() = FeatureActions(this, type = mutableListOf("ScalarValues::Boolean"))
    override fun conditionalExpressionActions() = ConditionalExpressionActions(this)
    override fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode) = ConditionalExpressionActions(this, i, t, e)
    override fun invariantActions() = InvariantActions(this)
    override fun attributeActions() = FeatureActions(this)
    override fun calculationActions() = CalculationActions(this)
    override fun directionFromPrefixes(): Feature.FeatureDirectionKind = when {
        Token.Kind.IN in prefixes -> Feature.FeatureDirectionKind.IN
        Token.Kind.OUT in prefixes -> Feature.FeatureDirectionKind.OUT
        else -> Feature.FeatureDirectionKind.INOUT
    }
}


class SemanticActionsThatDoNothing(
    model: Session,
    textualRepresentation: TextualRepresentation,
    owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack of Identifications of all nested elements
    generateAnnotations: Boolean = false
): SemanticActions, ActionsContextImplementation(
    model=model, textualRepresentation=textualRepresentation, owners = owners, generateAnnotations = generateAnnotations
) {
    override fun namespaceActions(): NamespaceActions? = null
    override fun packageActions(): PackageActions? = null
    override fun commentActions(): CommentActions? = null
    override fun classActions(): ClassActions? = null
    override fun dependencyActions(): DependencyActions? = null
    override fun documentationActions(): DocumentationActions? = null
    override fun textualRepresentationActions(): TextualRepresentationActions? = null
    override fun typeActions(): TypeActions? = null
    override fun connectorActions(): ConnectorActions? = null
    override fun datatypeActions(): DataTypeActions? = null
    override fun importActions(): ImportActions? = null
    override fun featureActions(): FeatureActions? = null
    override fun functionActions(): FunctionActions? = null
    override fun associationActions(): AssociationActions? = null
    override fun constraintActions(): FeatureActions? = null
    override fun attributeActions(): FeatureActions? = null
    override fun conditionalExpressionActions(): ConditionalExpressionActions? = null
    override fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode): ConditionalExpressionActions? = null
    override fun calculationActions(): CalculationActions? = null
    override fun invariantActions():InvariantActions? = null

    override fun directionFromPrefixes(): Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT

}