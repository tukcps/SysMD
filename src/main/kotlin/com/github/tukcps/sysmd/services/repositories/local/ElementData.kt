package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
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

    // For type AnnotationElement, Expression:
    override var language: String? = null,  // language, e.g. SysMD, SysML
    override var body: String? = null,      // The code in e.g., SysMD or SysML v2 textual

    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var isStandard: Boolean? = false,
    override var isLibraryElement: Boolean? = false,
    override var isEnd: Boolean? = false,
    override var isDerived: Boolean? = false,
    override var isAbstract: Boolean? = false,
    override var isComposite: Boolean? = false,
    override var isConjugated: Boolean? = false,
    override var isOrdered: Boolean? = false,
    override var isReadOnly: Boolean? = false,
    override var isSufficient: Boolean? = false,
    override var isUnique: Boolean? = false,

    override var textualRepresentation: MutableList<Identified>? = mutableListOf(),
    override var documentation: Identified? = null,

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
        "AllocationUsage"   -> AllocationUsageImplementation()
        "AllocationDefinition" -> AllocationDefinitionImplementation()
        "AnnotatingElement" -> AnnotatingElementImplementation(body = body!!)
        "Annotation"        -> AnnotationImplementation()
        "AttributeDefinition" -> AttributeDefinitionImplementation()
        "AttributeUsage"    -> AttributeUsageImplementation()
        "Association"       -> AssociationImplementation()
        "CalculationDefinition" -> CalculationDefinitionImplementation()
        "Classifier"        -> ClassifierImplementation()
        "Class"             -> ClassImplementation()
        "Comment"           -> CommentImplementation(body = body!!)
        "Connector"         -> ConnectorImplementation()
        "ConnectionUsage"   -> ConnectionUsageImplementation()
        "ConnectionDefinition" -> ConnectionDefinitionImplementation()
        "DataType"          -> DataTypeImplementation()
        "Dependency"        -> DependencyImplementation()
        "Documentation"     -> DocumentationImplementation(body = body!!)
        "Element"           -> ElementImplementation()
        "FeatureTyping"     -> FeatureTypingImplementation(typedFeature= Resolved(id=source?.firstOrNull()?.id), type=Resolved(id = target?.firstOrNull()?.id))
        "Function"          -> FunctionImplementation()
        "InterfaceDefinition" -> InterfaceDefinitionImplementation()
        "InterfaceUsage"    -> InterfaceUsageImplementation()
        "NamespaceImport"   -> NamespaceImportImplementation(importedNamespace = Resolved(id=target?.firstOrNull()?.id, str=importedNamespace, ref=null))
        "MembershipImport"  -> MembershipImportImplementation(importedNamespace = Resolved(id= target?.firstOrNull()?.id, str=importedNamespace, ref=null), importedMemberName = Resolved(id=null, ref=null, str=importedMemberName))
        "Metaclass"         -> MetaclassImplementation()
        "MetadataFeature"   -> MetadataFeatureImplementation()
        "Multiplicity"      -> MultiplicityImplementation()
        "Namespace"         -> NamespaceImplementation()
        "Specialization"    -> SpecializationImplementation(specific= Resolved(id = source?.firstOrNull()?.id), general= Resolved(id = target?.firstOrNull()?.id))
        "Subsetting"        -> SubsettingImplementation(subsettingFeature= Resolved(id= source?.firstOrNull()?.id), subsettedFeature= Resolved(id= target?.firstOrNull()?.id))
        "Type"              -> TypeImplementation()
        "Feature"           -> FeatureImplementation(direction = enumValueOf<Feature.FeatureDirectionKind>(direction?:"IN"))
        "Invariant"         -> InvariantImplementation()
        "Package"           -> PackageImplementation(declaredName=declaredName, declaredShortName = declaredShortName, isLibraryElement = isLibraryElement == true, isStandard = isStandard == true)
        "PartUsage"         -> PartUsageImplementation()
        "PartDefinition"    -> PartDefinitionImplementation()
        "PortUsage"         -> PortUsageImplementation()
        "PortDefinition"    -> PortDefinitionImplementation()
        "Redefinition"      -> RedefinitionImplementation()
        "Relationship"      -> RelationshipImplementation()
        "ReferenceSubsetting" -> ReferenceSubsettingImplementation()
        "RequirementUsage"   -> RequirementUsageImplementation()
        "RequirementDefinition" -> RequirementDefinitionImplementation()
        "Subclassification" -> SubclassifierImplementation()
        "TextualRepresentation" -> TextualRepresentationImplementation(body = body!!, language = language!!)
        else             -> throw Exception("Element with unknown type '$type' in response; must be valid entity type.")
    }
    element.elementId = elementId
    element.declaredName = declaredName
    element.declaredShortName = declaredShortName
    element.isLibraryElement = isLibraryElement == true
    element.isStandard = isStandard == true
    element.owner = Resolved(str=null, id=owner?.id, ref=null)
    ownedElement.forEach {
        element.ownedElement.add(Resolved(str=null, id= it.id, ref=null))
    }
    if (element is Relationship && element !is Import) {
        source?.forEach { element.source.add(Resolved(it.id)) }
        target?.forEach { element.target.add(Resolved(it.id)) }
    }
    if (element is NamespaceImportImplementation) {
        element.importedNamespace.str = importedNamespace
        element.target.first().str = importedNamespace
        element.target.first().id = target?.firstOrNull()?.id
        element.source.first().id = source?.firstOrNull()?.id
        // element.importedMemberName = if (importedMemberName==null) null else Identity (str=importedMemberName!!)
    }
    if (element is Feature) {
        if (body != null) {
            val bodydata = body?.split("##")
            if (bodydata?.size == 3) {
                element.typeConstraint = bodydata[0].split(",").toMutableList()
                element.typeConstraint.forEach { it.trim() }
                element.unitConstraint = bodydata[1].trim()
                element.expression = bodydata[2].trim()
            }
        }
        element.isEnd = isEnd == true
        element.isComposite = isComposite == true
        element.isOrdered = isOrdered == true
        element.isDerived = isDerived == true
        element.isUnique = isUnique == true
        element.isReadOnly = isReadOnly == true
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
        owner = Identified(if (owner.id != model?.global?.elementId) owner.id else null ),
        isLibraryElement = isLibraryElement,
        isStandard = isStandard
    )
    dao.isLibraryElement = isLibraryElement
    dao.isStandard = isStandard

    ownedElement.forEach {
        dao.ownedElement.add(Identified(it.id))
    }

    if (this is Relationship) {
        source.forEach { if (it.id != null) dao.source?.add(Identified(it.id)) }
        target.forEach { if (it.id != null) dao.target?.add(Identified(it.id)) }
    }

    when(this) {
        is Multiplicity -> { dao.body = toBody() }
        is Feature -> {
            dao.direction = direction.toString()
            dao.body = toBody()
            dao.isEnd = isEnd
            dao.isComposite = isComposite
            dao.isOrdered = isOrdered
            dao.isReadOnly = isReadOnly
            dao.isDerived = isDerived
        }
        is TextualRepresentation -> { dao.body = body; dao.language = language }
        is Comment -> { dao.body = body }
        is AnnotatingElement -> { dao.body = body }
        is NamespaceImport -> { dao.importedNamespace = importedNamespace.str }
        is MembershipImport -> {
            dao.importedNamespace = importedNamespace.str
            dao.importedMemberName = importedMemberName.str
        }
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

