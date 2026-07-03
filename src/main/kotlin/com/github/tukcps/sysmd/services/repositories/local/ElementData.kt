package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.CaseDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.*
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.Identified
import java.util.*


/**
 * A simple plain old java object (POJO) that implements the Element Data Abstraction (DAO) interface
 * for storing element data in the local repository.
 */
data class ElementData(
    //used to parse JSON and map it to ElementDAO
    override var elementId: UUID, // --> elementId
    override var type: String,       // mandatory type of the metamodel as annotation

    override var aliasIds: MutableList<String> = mutableListOf(),
    override var name: String? = null,
    override var shortName: String? = null,
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,

    override var ownedElement: MutableList<Identified> = mutableListOf(),
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    override var owningRelationship: Identified? = null,

    // For type = Feature
    override var direction: String? = null,

    // For type = Import
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,

    // For type = ParameterMembership
    var parameterIndex : Int = -1,

    // For type = InstantiationExpression
    var functionName : String? = null, // TODO: add to interface

	// For type = OperatorExpression
    var operator : String? = null, // TODO: add to interface

    // For type = FeatureReferenceExpression
    var featureIdentifier : String? = null,

    // For type = FeatureChainExpression
    var targetFeature : String? = null,

    // for type : LiteralExpression
    var literalString : String? = null,
    var literalInteger : Long? = null,
    var literalRational : Double? = null,
    var literalBoolean : Boolean? = null,

    // For type AnnotationElement, Expression:
    override var language: String? = null,  // language, e.g. SysMD, SysML
    override var body: String? = null,      // The code in e.g., SysMD or SysML v2 textual

    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var isStandard: Boolean? = false,
    override var isLibraryElement: Boolean? = false,
    override var visibility: String? = null,
    override var isEnd: Boolean? = false,
    override var isDerived: Boolean? = false,
    override var isAbstract: Boolean? = false,
    override var isComposite: Boolean? = false,
    override var isConjugated: Boolean? = false,
    override var isOrdered: Boolean? = false,
    override var isReadOnly: Boolean? = false,
    override var isSufficient: Boolean? = false,
    override var isUnique: Boolean? = false,
    var isDefaultValue: Boolean? = false,
    var isInitialValue: Boolean? = false,

    override var textualRepresentation: MutableList<Identified>? = mutableListOf(),
    override var documentation: Identified? = null,
    override var ownedRelationship: MutableList<Identified> = mutableListOf(),

    // For Relationship and subtypes thereof:
    override var source: MutableList<Identified>? = mutableListOf(),     // list of id or null (i.e., global, anything)
    override var target: MutableList<Identified>? = mutableListOf(),    // list of id or null (i.e., global, anything)
): ElementDAO {
    override fun toString(): String = "$type '${declaredName?:declaredShortName?:""}'"
}

/**
 * Extension methods; kept separately from ElementDAO to avoid any problem with
 * Spring ...
 */
fun ElementDAO.toElement(): Element {
    val element = when (type) {
        "AllocationDefinition" -> AllocationDefinitionImplementation()
        "AllocationUsage"   -> AllocationUsageImplementation()
        "AnnotatingElement" -> AnnotatingElementImplementation(body = body!!)
        "Annotation"        -> AnnotationImplementation()
        "Association"       -> AssociationImplementation()
        "AttributeDefinition" -> AttributeDefinitionImplementation()
        "AttributeUsage"    -> AttributeUsageImplementation()
        "Behavior"          -> BehaviorImplementation()
        "BodyExpression" -> BodyExpressionImplementation()
        "CaseDefinition"    -> CaseDefinitionImplementation()
        "CaseUsage"         -> CaseUsageImplementation()
        "CalculationUsage"   -> CalculationUsageImplementation()
        "CalculationDefinition" -> CalculationDefinitionImplementation()
        "Class"             -> ClassImplementation()
        "Classifier"        -> ClassifierImplementation()
        "CollectExpression" -> CollectExpressionImplementation()
        "Comment"           -> CommentImplementation(body = body!!)
        "ConnectionDefinition" -> ConnectionDefinitionImplementation()
        "ConnectionUsage"   -> ConnectionUsageImplementation()
        "Connector"         -> ConnectorImplementation()
        "DataType"          -> DataTypeImplementation()
        "Dependency"        -> DependencyImplementation()
        "Disjoining"        -> DisjoiningImplementation()
        "Documentation"     -> DocumentationImplementation(body = body!!)
        "Element"           -> ElementImplementation()
        "EndFeatureMembership" -> EndFeatureMembershipImplementation()
        "Feature"           -> FeatureImplementation()
        "FeatureChainExpression" -> FeatureChainExpressionImplementation()
        "FeatureReferenceExpression" -> FeatureReferenceExpressionImplementation()
        "FeatureChaining"   -> FeatureChainingImplementation()
        "FeatureMembership" -> FeatureMembershipImplementation()
        "FeatureTyping"     -> FeatureTypingImplementation()
        "Function"          -> FunctionImplementation()
        "IndexExpression" -> IndexExpressionImplementation()
        "InterfaceDefinition" -> InterfaceDefinitionImplementation()
        "InterfaceUsage"    -> InterfaceUsageImplementation()
        "Invariant"         -> InvariantImplementation()
        "InvocationExpression" -> InvocationExpressionImplementation()
        "LiteralBoolean" -> LiteralBooleanImplementation()
        "LiteralInfinity" -> LiteralInfinityImplementation()
        "LiteralInteger" -> LiteralIntegerImplementation()
        "LiteralRational" -> LiteralRationalImplementation()
        "LiteralString" -> LiteralStringImplementation()
        "Membership"        -> MembershipImplementation()
        "MembershipImport"  -> MembershipImportImplementation()
        "Metaclass"         -> MetaclassImplementation()
        "MetadataAccessExpression" -> MetadataAccessExpressionImplementation()
        "MetadataFeature"   -> MetadataFeatureImplementation()
        "Multiplicity"      -> MultiplicityImplementation()
        "Namespace"         -> NamespaceImplementation()
        "NamespaceImport"   -> NamespaceImportImplementation()
        "NullExpression" -> NullExpressionImplementation()
        "OperatorExpression" -> OperatorExpressionImplementation()
        "OwningMembership"  -> OwningMembershipImplementation()
        "Package"           -> PackageImplementation()
        "ParameterMembership" -> ParameterMembershipImplementation()
        "PartDefinition"    -> PartDefinitionImplementation()
        "PartUsage"         -> PartUsageImplementation()
        "PortDefinition"    -> PortDefinitionImplementation()
        "PortUsage"         -> PortUsageImplementation()
        "RawNameExpression" -> RawNameExpressionImplementation()
        "Redefinition"      -> RedefinitionImplementation()
        "ReferenceSubsetting" -> ReferenceSubsettingImplementation()
        "RequirementDefinition" -> RequirementDefinitionImplementation()
        "RequirementUsage"   -> RequirementUsageImplementation()
        "ReturnParameterMembership" -> ReturnParameterMembershipImplementation()
        "SelectExpression" -> SelectExpressionImplementation()
        "Specialization"    -> SpecializationImplementation()
        "Subclassification" -> SubclassificationImplementation()
        "Subsetting"        -> SubsettingImplementation()
        "TextualRepresentation" -> TextualRepresentationImplementation(body = body!!, language = language!!)
        "Type"              -> TypeImplementation()
        "VerificationCaseDefinition" -> VerificationCaseDefinitionImplementation()
        "VerificationCaseUsage" -> VerificationCaseUsageImplementation()
        else             -> {
            logger.error("Element with unknown type '$type' in Element DAO; must be valid entity type.")
            throw Exception("Element with unknown type '$type' in Element DAO; must be valid entity type.")
        }
    }
    element.elementId = elementId
    element.declaredName = declaredName
    element.declaredShortName = declaredShortName
    element.isLibraryElement = isLibraryElement == true
    element.isStandard = isStandard == true

    if (element is Relationship) {
        element.source = mutableListOf()
        element.target = mutableListOf()
        source?.forEach { element.source.add(UnresolvedElement(id=it.id)) }
        target?.forEach { element.target.add(UnresolvedElement(id=it.id)) }
    }

    // TODO: add these to ElementDAO interface
    if(this is ElementData) when(element) {
        is FeatureChainExpression -> element.targetFeature = targetFeature
        is OperatorExpression -> element.operator = operator
        is InstantiationExpression -> element.functionName = functionName

        is LiteralStringImplementation -> element.value = literalString
        is LiteralBooleanImplementation -> element.value = literalBoolean
        is LiteralIntegerImplementation -> element.value = literalInteger
        is LiteralRationalImplementation -> element.value = literalRational

        is RawNameExpressionImplementation -> element.rawName = literalString

        is ParameterMembership -> element.parameterIndex = parameterIndex
    }

    if (element is Feature) {
        if (body != null) {
            val bodydata = body?.split("##")
            if (bodydata?.size == 3) {
                element.typeConstraint = bodydata[0].split(",").toMutableList()
                element.typeConstraint.forEach { it.trim() }
                element.expression = bodydata[2].trim()
            }
        }
        element.direction = direction?.let(::enumValueOf)
        element.isEnd = isEnd == true
        element.isComposite = isComposite == true
        element.isOrdered = isOrdered == true
        element.isDerived = isDerived == true
        element.isUnique = isUnique == true
        if (this is ElementData) {
            element.isDefaultValue = this.isDefaultValue == true
            element.isInitialValue = this.isInitialValue == true
        }
    }
    return element
}

/**
 * Maps an element of entities.Element to its respective DAO.
 * The DAO is used for (de)serialization.
 */
fun Element.toDAO(): ElementData {
    val dao = ElementData(
        type = elementType,
        elementId = elementId!!,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        name = name,
        shortName = shortName,
        isLibraryElement = isLibraryElement,
        isStandard = isStandard,
        owner = Identified(owner?.elementId),
    )
    dao.isLibraryElement = isLibraryElement
    dao.isStandard = isStandard

    ownedElement.forEach {
        dao.ownedElement.add(Identified(it.elementId))
    }

    if (this is Relationship) {
        dao.owningNamespace = Identified( if (owningRelatedElement == model?.global) null else owningRelatedElement.elementId)
        source.forEach { if (it.elementId != null) dao.source?.add(Identified(if (it == model?.global) null else it.elementId)) }
        target.forEach { if (it.elementId != null) dao.target?.add(Identified(if (it == model?.global) null else it.elementId)) }
    } else {
        dao.owningRelationship = Identified(owningRelationship?.elementId)
    }

    if (this is Namespace || this is AnnotatingElement || this !is Relationship) {
        dao.owningRelationship = Identified(owningRelationship?.elementId)
        ownedRelationship.forEach {  dao.ownedRelationship.add(Identified(it.elementId)) }
    } else {
        dao.owningNamespace = Identified(owningRelatedElement.elementId)
        ownedElement.forEach { dao.owningNamespace = Identified(it.elementId) }
    }

    if(this is InstantiationExpression)
        dao.functionName = functionName
	if(this is OperatorExpression)
		dao.operator = operator
    if(this is FeatureChainExpression)
        dao.targetFeature = targetFeature
    if(this is LiteralString)
        dao.literalString = value
    if(this is LiteralBoolean)
        dao.literalBoolean = value
    if(this is LiteralInteger)
        dao.literalInteger = value
    if(this is LiteralRational)
        dao.literalRational = value
    if(this is RawNameExpression)
        dao.literalString = rawName
    if(this is ParameterMembership)
        dao.parameterIndex = parameterIndex

    when(this) {
        is Multiplicity -> { dao.body = toBody() }
        is Feature -> {
            dao.direction = direction?.toString()
            dao.body = toBody()
            dao.isEnd = isEnd
            dao.isComposite = isComposite
            dao.isOrdered = isOrdered
            dao.isReadOnly = isReadOnly
            dao.isDerived = isDerived
            dao.isUnique = isUnique
            dao.isDefaultValue = isDefaultValue
            dao.isInitialValue = isInitialValue
        }
        is TextualRepresentation -> { dao.body = body; dao.language = language }
        is AnnotatingElement -> { dao.body = body }
    }
    return dao
}


fun ElementDAO.toElementData() = ElementData(
    elementId = elementId,
    type = type,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    name = name,
    shortName = shortName,
    ownedElement = mutableListOf<Identified>().also { list -> ownedElement.forEach { list.add(Identified(it.id)) }},
    owner = Identified(owner?.id),
    direction = direction.toString(),
    importedMemberName = importedMemberName,
    importedNamespace = importedNamespace,
    language = language,
    body = body,
    isDefaultValue = if (this is ElementData) this.isDefaultValue else false,
    isInitialValue = if (this is ElementData) this.isInitialValue else false,
//    source = source.map { if ((it != "null")&&(it!=null)) UUID.fromString(it) else null }.toMutableList(),
//    target = target.map { if ((it != "null")&&(it!=null)) UUID.fromString(it) else null }.toMutableList()
)

fun Feature.toBody(): String {
    var typeConstraint4body = ""
    typeConstraint.forEach {
        typeConstraint4body += "$it ,"
    }
    typeConstraint4body = typeConstraint4body.trimEnd(',')
    typeConstraint4body = typeConstraint4body.trim()
    return "$typeConstraint4body ## ${unitConstraint?:""} ## ${expression?:""}"
}

