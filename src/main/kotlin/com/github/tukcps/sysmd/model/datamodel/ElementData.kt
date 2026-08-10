package com.github.tukcps.sysmd.model.datamodel

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.LiteralBooleanImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralIntegerImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralRationalImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralStringImplementation
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.generated.createElement
import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Import.VisibilityKind
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.model.util.UnresolvedOwningMembership
import com.github.tukcps.sysmd.model.util.UnresolvedRelationship
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.services.session.Session
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A simple plain old java object (POJO) that implements the Element Data Abstraction (DAO) interface
 * for storing element data in the local repository.
 */
@Serializable
data class ElementData(
    //used to parse JSON and map it to ElementDAO
    @SerialName("@id")
    override var elementId: Uuid, // --> elementId
    @SerialName("@type")
    override var type: ElementType = ElementType.Element,

    override var aliasIds: MutableList<String> = mutableListOf(),
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,

    override var ownedElement: MutableList<Identified> = mutableListOf(),
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null, // TODO: remove, is derived from owningRelationship
    override var owningNamespace: Identified? = null,
    override var owningRelationship: Identified? = null,

    // For type = Feature
    override var direction: Feature.FeatureDirectionKind? = null,

    // For type = Import
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,

    // For type = ParameterMembership
    var parameterIndex : Int = -1,

    // For type = InstantiationExpression
    var functionName : String? = null, // TODO: get rid of

	// For type = OperatorExpression
    override var operator : String? = null,

    // For type = FeatureReferenceExpression
    var featureIdentifier : String? = null,

    // For type = FeatureChainExpression
    var targetFeature : String? = null,

    /** SysMD extension for [LiteralString] */
    var isNameLiteral : Boolean? = null,

    // For type AnnotationElement, Expression:
    override var language: String? = null,  // language, e.g. SysMD, SysML
    override var body: String? = null,      // The code in e.g., SysMD or SysML v2 textual

    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var isStandard: Boolean? = false,
    override var visibility: VisibilityKind? = null,
    override var isEnd: Boolean? = false,
    override var isDerived: Boolean? = false,
    override var isAbstract: Boolean? = false,
    override var isComposite: Boolean? = false,
    override var isConjugated: Boolean? = false,
    override var isOrdered: Boolean? = false,
    override var isSufficient: Boolean? = false,
    override var isUnique: Boolean? = false,
    override var isPortion: Boolean? = false,
    override var isDefault: Boolean? = false,
    override var isInitial: Boolean? = false,
    override var isNegated: Boolean? = false,
    override var isVariable: Boolean? = null,
    override var isConstant: Boolean? = null,
    override var isRecursive: Boolean? = null,
    override var isImportAll: Boolean? = null,

    // For Relationship and subtypes thereof:
    override var source: MutableList<Identified> = mutableListOf(),     // list of id or null (i.e., global, anything)
    override var target: MutableList<Identified> = mutableListOf(),    // list of id or null (i.e., global, anything)
    override var ownedRelatedElement: MutableList<Identified> = mutableListOf(),
    override var owningRelatedElement: Identified? = null,

    override var ownedRelationship: MutableList<Identified> = mutableListOf(),

    @Deprecated("Is represented by Type, only computed property.")
    var isLibraryElement: Boolean? = null,
    override var locale: String? = null,
    override var memberShortName: String? = null,
    override var memberName: String? = null,
    override var isVariation: Boolean? = null,
    override var requirementConstraintMembershipKind: RequirementConstraintMembership.RequirementConstraintKind? = null,
    override var transitionFeatureMembershipKind: TransitionFeatureMembership.TransitionFeatureKind? = null,
    override var stateSubactionMembershipKind: StateSubactionMembership.StateSubactionKind? = null,
    override var isIndividual: Boolean? = null,
    override var portionKind: OccurrenceUsage.PortionKind? = null,
    override var literalStringValue: String? = null,
    override var literalIntegerValue: Long? = null,
    override var literalRationalValue: Double? = null,
    override var literalBooleanValue: Boolean? = null,
    override var isParallel: Boolean? = null,
    override var reqId: String? = null,
    override var triggerInvocationExpressionKind: TriggerInvocationExpression.TriggerKind? = null,

    // From Compiler.
    override var input: String? = null,
    @Serializable(with = IntRangeSerializer::class)
    override var indices: IntRange? = null

): ElementDataIF {

    override fun toString(): String =
        "[${type.name}] '${declaredName?:declaredShortName?:""}'"
}

/**
 * Extension methods; kept separately from ElementDAO to avoid any problem with
 * Spring ...
 */
fun ElementDataIF.toElement(model : Session): Element {
    val element = createElement(type, model, elementId)

    // Generally for all kind of Element.
    element.declaredName = declaredName
    element.declaredShortName = declaredShortName
    element.isStandard = isStandard == true

    if (this is ElementData) {
        element.indices = this.indices
        element.input = this.input
    }

    // Process source + target for all kind of Relationship
    if (element is Relationship) {
        element.owningRelatedElement = owningRelatedElement.let {
            if(it is IdentifiedByName)
                UnresolvedElement(model, relativeName = it.name)
            else
                UnresolvedElement(model, id = owner?.id)
        }

        ownedRelatedElement.mapTo(element.ownedRelatedElement) {
            UnresolvedElement(model, id = it.id)
        }

        fun List<Identified>.toElements() = mapTo(mutableListOf<Element>()) {
            if(it is IdentifiedByName)
                it.toUnresolved(model) // can we handle every Unresolved* construction like this?
            else
                UnresolvedElement(model, id = it.id)
        }

        element.source = source.toElements()
        element.target = target.toElements()

        if (this is ElementData)
            if (element is NamespaceImport && target.firstOrNull() is IdentifiedByName) {
                element.isRecursive = isRecursive == true
                element.isImportAll = isImportAll == true
            }

        if (element is OwningMembership)
            element.visibility = visibility ?: VisibilityKind.Public
    }

    // Process elements that are not used as Relationships.
    if (element is Namespace || element is AnnotatingElement || element is Dependency) {
        // No relationship, with hierarchy of owned relationships/elements
        element.owningRelationship = UnresolvedOwningMembership(model, id = owningRelationship?.id)

        ownedRelationship.filterIsInstance<IdentifiedByName>().mapTo(element.ownedRelationship) {
            UnresolvedRelationship(
                model,
                relativeName = it.name,
                id = it.id
            )
        }

        if (element is AnnotatingElement) {
            element.body = body ?: ""
        }

        if (element is TextualRepresentation) {
            element.language = language?:""
        }

        if (element is Type) {
            if (isAbstract == true) element.isAbstract = true
        }
    }

    // TODO: add these to ElementDAO interface
    if(this is ElementData) when(element) {
        is FeatureChainExpression -> element.targetFeature = targetFeature
        is OperatorExpression -> element.operator = operator
        is InstantiationExpression -> element.functionName = functionName

        is LiteralStringImplementation -> {
            element.value = literalStringValue
            element.isNameLiteral = isNameLiteral ?: false
        }
        is LiteralBooleanImplementation -> element.value = literalBooleanValue
        is LiteralIntegerImplementation -> element.value = literalIntegerValue
        is LiteralRationalImplementation -> element.value = literalRationalValue

        is ParameterMembership -> element.parameterIndex = parameterIndex
    }

    if (element is Feature) {
        // Mapping of non-standard body-field to non-standard feature properties
        element.expression = body
        element.direction = direction
        element.isEnd = isEnd == true
        element.isComposite = isComposite == true
        if (this is ElementData)
            element.isPortion = isPortion == true
        element.isOrdered = isOrdered == true
        element.isDerived = isDerived == true
        element.isUnique = isUnique == true
        element.isSufficient = isSufficient == true
        if (this is ElementData) {
            element.isDefaultValue = this.isDefault == true
            element.isInitialValue = this.isInitial == true
            element.isVariable = this.isVariable == true
            element.isConstant = this.isConstant == true
        }
        if (element is Invariant && this is ElementData)
            element.isNegated = this.isNegated == true
    }

    if (element is Association)
        element.owningRelationship = owningRelationship?.let { UnresolvedOwningMembership(model, id = it.id) }

    return element
}

/**
 * Maps an element of entities.Element to its respective DAO.
 * The DAO is used for (de)serialization.
 */
fun Element.toElementData(): ElementData {
    /** Constructs a reference to a different element */
    fun Element.toReference() : Identified = IdentifiedImplementation(if(this === model.global) null else elementId)
    fun List<Element>.toReferences() = mapTo(mutableListOf()) { it.toReference() }

    val dao = ElementData(
        type = elementType(),
        elementId = elementId,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        isStandard = isStandard,
        indices = this.indices,
        input = this.input?.toString(),

        owner = (owner ?: model.global).toReference(),
        owningNamespace = (owningNamespace ?: model.global).toReference(),

        ownedElement = ownedElement.toReferences(),
        ownedRelationship = ownedRelationship.toReferences(),
        owningRelationship = owningRelationship?.toReference()
    )

    if(owningRelationship is OwningMembership) // We don't distinguish these?
        dao.owningMembership = dao.owningRelationship

    if (this is Relationship) {
        dao.owningRelatedElement = owningRelatedElement.toReference()
        dao.ownedRelatedElement = ownedRelatedElement.toReferences()
        dao.source = source.toReferences()
        dao.target = target.toReferences()
    }

    if(this is InstantiationExpression)
        dao.functionName = functionName
	if(this is OperatorExpression)
		dao.operator = operator
    if(this is FeatureChainExpression)
        dao.targetFeature = targetFeature
    if(this is LiteralString)
    {
        dao.literalStringValue = value
        dao.isNameLiteral = isNameLiteral
    }
    if(this is LiteralBoolean)
        dao.literalBooleanValue = value
    if(this is LiteralInteger)
        dao.literalIntegerValue = value
    if(this is LiteralRational)
        dao.literalRationalValue = value
    if(this is ParameterMembership)
        dao.parameterIndex = parameterIndex

    if(this is Feature) {
        dao.direction = direction
        dao.body = expression
        dao.isEnd = isEnd
        dao.isComposite = isComposite
        dao.isOrdered = isOrdered
        dao.isConstant = isReadOnly
        dao.isDerived = isDerived
        dao.isUnique = isUnique
        dao.isDefault = isDefaultValue
        dao.isInitial = isInitialValue
    }

    if(this is TextualRepresentation) {
        dao.body = body
        dao.language = language
    }

    if(this is AnnotatingElement)
        dao.body = body

    return dao
}
