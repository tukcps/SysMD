package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.implementation.*
import com.github.tukcps.sysmlv2.entities.ElementDAO
import com.github.tukcps.sysmlv2.entities.Identified
import com.github.tukcps.sysmlv2.entities.responseModels.ElementResponse
import java.util.*


/**
 * A simple plain old java object (POJO) that stores element data in the local repository.
 */
open class ElementData(
    //used to parse JSON and map it to ElementDAO
    override var elementId: UUID, // --> elementId
    override var type: String,       // mandatory type of the metamodel as annotation

    override var aliasIds: MutableList<String> = mutableListOf(),
    override var name: String? = null,
    override var shortName: String? = null,
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,

    override var ownedElements: MutableList<Identified> = mutableListOf(),
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
    override var body: String? = null,      // The code in e.g. SysMD or SysML v2 textual

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
    override fun toString(): String =
        "$type '${declaredName?:declaredShortName?:""}'"
}

/**
 * Extension methods; kept separately from ElementDAO to avoid any problem with
 * Spring ...
 */
fun ElementDAO.toElement(): Element {
    val element = when (type) {
        "AllocationUsage"   -> AllocationUsageImplementation(elementId=elementId)
        "AllocationDefinition" -> AllocationDefinitionImplementation(elementId=elementId)
        "AnnotatingElement" -> AnnotatingElementImplementation(elementId=elementId, body = body!!)
        "Annotation"        -> AnnotationImplementation(elementId=elementId)
        "Association"       -> AssociationImplementation(elementId=elementId)
        "CalculationDefinition" -> CalculationDefinitionImplementation(elementId=elementId)
        "Classifier"        -> ClassifierImplementation(elementId=elementId)
        "Class"             -> ClassImplementation(elementId=elementId)
        "Comment"           -> CommentImplementation(elementId=elementId, body = body!!)
        "Connector"         -> ConnectorImplementation(elementId=elementId)
        "ConnectionUsage"   -> ConnectionUsageImplementation(elementId=elementId)
        "ConnectionDefinition" -> ConnectionDefinitionImplementation(elementId=elementId)
        "DataType"          -> DataTypeImplementation(elementId=elementId)
        "Dependency"        -> DependencyImplementation(elementId=elementId)
        "Documentation"     -> DocumentationImplementation(elementId=elementId, body = body!!)
        "Element"           -> ElementImplementation(elementId=elementId)
        "FeatureTyping"     -> FeatureTypingImplementation(elementId=elementId, typedFeature= Resolved(id=source?.firstOrNull()?.id), type=Resolved(id = target?.firstOrNull()?.id))
        "Function"          -> FunctionImplementation(elementId=elementId)
        "InterfaceDefinition" -> InterfaceDefinitionImplementation(elementId=elementId)
        "InterfaceUsage"    -> InterfaceUsageImplementation(elementId=elementId)
        "NamespaceImport"   -> NamespaceImportImplementation(elementId=elementId, importedNamespace = Resolved(id=target?.firstOrNull()?.id, str=importedNamespace, ref=null))
        "MembershipImport"  -> MembershipImportImplementation(id=elementId, importedNamespace = Resolved(id= target?.firstOrNull()?.id, str=importedNamespace, ref=null), importedMemberName = Resolved(id=null, ref=null, str=importedMemberName))
        "Multiplicity"      -> MultiplicityImplementation(elementId=elementId, name=name)
        "Specialization"    -> SpecializationImplementation(elementId=elementId, specific= Resolved(id = source?.firstOrNull()?.id), general= Resolved(id = target?.firstOrNull()?.id))
        "Subsetting"        -> SubsettingImplementation(elementId=elementId, subsettingFeature= Resolved(id= source?.firstOrNull()?.id), subsettedFeature= Resolved(id= target?.firstOrNull()?.id))
        "Type"              -> TypeImplementation(elementId=elementId)
        "Feature"           -> FeatureImplementation(elementId =elementId, direction = enumValueOf<Feature.FeatureDirectionKind>(direction?:"IN"))
        "Invariant"         -> InvariantImplementation(elementId=elementId)
        "Package"           -> PackageImplementation(elementId=elementId, declaredName=declaredName, declaredShortName = declaredShortName, isLibraryElement = isLibraryElement == true, isStandard = isStandard == true)
        "PartUsage"         -> PartUsageImplementation(elementId=elementId)
        "PartDefinition"    -> PartDefinitionImplementation(elementId=elementId)
        "PortUsage"         -> PortUsageImplementation(elementId=elementId)
        "PortDefinition"    -> PortDefinitionImplementation(elementId=elementId)
        "Namespace"         -> NamespaceImplementation(elementId=elementId)
        "Redefinition"      -> RedefinitionImplementation(elementId=elementId)
        "Relationship"      -> RelationshipImplementation(elementId=elementId)
        "ReferenceSubsetting" -> ReferenceSubsettingImplementation(elementId=elementId)
        "RequirementUsage"   -> RequirementUsageImplementation(elementId=elementId)
        "RequirementDefinition" -> RequirementDefinitionImplementation(elementId=elementId)
        "TextualRepresentation" -> TextualRepresentationImplementation(elementId=elementId, body = body!!, language = language!!)
        else             -> throw Exception("Element with unknown type '$type' in response; must be valid entity type.")
    }
    element.declaredName = declaredName
    element.declaredShortName = declaredShortName
    element.isLibraryElement = isLibraryElement == true
    element.isStandard = isStandard == true
    element.owner = Resolved(str=null, id=owner?.id, ref=null)
    ownedElements.forEach {
        element.ownedElement.add(Resolved(str=null, id= it.id, ref=null))
    }
    if (element is Relationship && element !is Import) {
        source?.forEach { element.source.add(Resolved(it.id)) }
        target?.forEach { element.target.add(Resolved(it.id)) }
    }
    if (element is NamespaceImportImplementation) {
        element.importedNamespace.str = importedNamespace
        element.target.first().str = importedNamespace
        element.target.first().id = target?.first()?.id
        element.source.first().id = source?.first()?.id
        // element.importedMemberName = if (importedMemberName==null) null else Identity(str=importedMemberName!!)
    }
    if (element is Feature && body != null) {
        val bodydata = body?.split("##")
        if (bodydata?.size == 3) {
            element.typeConstraint = bodydata[0].split(",").toMutableList()
            element.typeConstraint.forEach {it.trim()}
            element.unitConstraint = bodydata[1].trim()
            element.expression = bodydata[2].trim()
        }
        element.isEnd = isEnd?:false
        element.isComposite = isComposite?:false
        element.isOrdered = isOrdered?:false
        element.isDerived = isDerived?:false
        element.isUnique = isUnique?:false
        element.isReadOnly = isReadOnly?:false
    }
    return element
}

/**
 * Maps an element of entities.Element to its respective DAO.
 * The DAO is used for (de)serialization.
 */
fun Element.toDAO(): ElementData {
    // require(model != null)
    val dao = ElementData(
        type = elementType,
        elementId = elementId,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        name = name,
        shortName = shortName,
        owner = Identified(if (owner.id != model?.global?.elementId) owner.id else null ),
        isLibraryElement = isLibraryElement,
        isStandard = isStandard
    )

    ownedElement.forEach {
        dao.ownedElements.add(Identified(it.id))
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


fun ElementResponse.toElementData() = ElementData(
    elementId = id,
    type = type,
    name = name,
    shortName = shortName,
    ownedElements = mutableListOf<Identified>().also { list -> ownedElements.forEach { list.add(Identified(it.id)) }},
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

