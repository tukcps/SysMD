package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.compiler.semantics.expression.InvariantActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Comment
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.CommentImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.DependencyImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FunctionImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.sysml.CalculationDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


/**
 * For each production rule of the KerML grammar, there is a semantic action that does something.
 * This allows us to create different implementations for different purposes of the parser.
 */
interface SemanticActions: ActionsContext {
    fun namespaceActions(): NamespaceActions<NamespaceImplementation>
    fun packageActions(): NamespaceActions<PackageImplementation>
    fun classActions(): ClassActions<Class>
    fun commentActions(): CommentActions<Comment>
    fun dependencyActions(): DependencyActions<Dependency>
    fun documentationActions(): DocumentationActions
    fun annotatingElementActions(): AnnotatingElementActions<AnnotatingElement>
    fun textualRepresentationActions(): AnnotatingElementActions<TextualRepresentation>
    fun typeActions(): TypeActions<Type>
    fun connectorActions(): ConnectorActions<Connector>
    fun datatypeActions(): DataTypeActions<DataType>
    fun importActions(): ImportActions?
    fun featureActions(): FeatureActions<Feature>
    fun functionActions(): FunctionActions<Function>
    fun associationActions(): AssociationActions<Association>?
    fun constraintActions(): FeatureActions<Feature>
    fun attributeActions(): FeatureActions<Feature>
    fun conditionalExpressionActions(): ConditionalExpressionActions?
    fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode): ConditionalExpressionActions?
    fun invariantActions(): InvariantActions<InvariantImplementation>
    fun calculationActions(): CalculationDefinitionActions<CalculationDefinition>
}

class SemanticActionsImplementation(
    model: Session,
    owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack of all nested element's owners
    generateAnnotations: Boolean = false
): SemanticActions, ActionsContextImplementation(
    model=model,
    owners = owners,
    generateAnnotations = generateAnnotations
) {
    override fun namespaceActions() = NamespaceActions(this, ::NamespaceImplementation)
    override fun packageActions() = NamespaceActions(this, ::PackageImplementation)
    override fun classActions() = ClassActions<Class>(this, ::ClassImplementation)
    override fun commentActions(): CommentActions<Comment> = CommentActions(this, ::CommentImplementation)
    override fun dependencyActions() = DependencyActions<Dependency>(this, ::DependencyImplementation)
    override fun documentationActions() = DocumentationActions(this)
    override fun annotatingElementActions() = AnnotatingElementActions<AnnotatingElement>(this, ::AnnotatingElementImplementation)
    override fun textualRepresentationActions() = AnnotatingElementActions<TextualRepresentation>(this, ::TextualRepresentationImplementation)
    override fun datatypeActions() = DataTypeActions<DataType>(this, ::DataTypeImplementation)
    override fun typeActions() = TypeActions<Type>(this, ::TypeImplementation)
    override fun connectorActions() = ConnectorActions<Connector>(this, ::ConnectorImplementation)
    override fun importActions() = ImportActions(this)
    override fun featureActions() = FeatureActions<Feature>(this, ::FeatureImplementation, mutableListOf("Base::Anything"))
    override fun functionActions() = FunctionActions<Function>(this, ::FunctionImplementation)
    override fun associationActions() = AssociationActions<Association>(this, ::AssociationImplementation)
    override fun constraintActions() = FeatureActions<Feature>(this,creator = ::FeatureImplementation, mutableListOf("ScalarValues::Boolean"))
    override fun conditionalExpressionActions() = ConditionalExpressionActions(this)
    override fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode) = ConditionalExpressionActions(this, i, t, e)
    override fun invariantActions() = InvariantActions(this, ::InvariantImplementation)
    override fun attributeActions() = FeatureActions<Feature>(this, ::FeatureImplementation, mutableListOf("Attributes::Attribute"))
    override fun calculationActions() = CalculationDefinitionActions<CalculationDefinition>(this, ::CalculationDefinitionImplementation)
}