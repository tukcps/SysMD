package com.github.tukcps.sysmd.rest.entities.requests

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.tukcps.aadd.values.*
import com.github.tukcps.sysmd.rest.exceptions.RequestInvalidException
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import java.util.*


/**
 * The payload model for a request of a client to the Agila backend.
 * The value of _type_ is mandatory.
 * All other fields are optional, depending on the type.
 */
class ElementRequest {
    @JsonProperty("@id")   // elementId?
    var id: UUID = UUID.randomUUID() // elementId?

    @JsonProperty("@type")
    val type: String? = null              // mandatory type of the metamodel as annotation

    // The following are required for every Element
    val name: String? = null
    val shortName: String? = null
    val ownedElements: List<UUID>? = null // The IDs of the owned elements.
    val owner: UUID? = null                 // The ID of the owner; if null, the node is the root of an ownership tree

    // For type Feature :
    val direction: Feature.FeatureDirectionKind? = null

    // For type ValueProperty; will be moved to owned elements within v2.13
    val valueSpecs = mutableListOf<Any?>() // Owned element of type Value?
    val unitSpec: String? = null   // Owned element of type Value/Unit?
    val dependency: String? = null // Owned element of type ExpressionValue?

    val importedMemberName: String? = null
    val importedNamespace: String? = null

    // For type AnnotationElement and subclasses
    val language: String? = null   // language, e.g. SysMD, SysML
    val body: String? = null       // The code in e.g. SysMD or SysML v2 textual

    // For Relationship and subtypes thereof:
    val source: List<UUID>? = null     // qualified names of sources
    val target: List<UUID>? = null     // qualified names of targets


    /**
     * The method converts an ElementRequest object to an Element object of one of the
     * Element subclasses.
     */
    fun toElement(): Element {
        val element = when (type) {
            "NamespaceImport"-> NamespaceImportImplementation(
                importedNamespace = Resolved(id=target?.firstOrNull(), str=importedNamespace, ref=null))
            "MembershipImport" -> MembershipImportImplementation(
                importedMemberName = Resolved(id=null, ref=null, str=importedMemberName))
            "Multiplicity" -> MultiplicityImplementation(
                multiplicity = if (valueSpecs.isNotEmpty()) IntegerRange(valueSpecs[0] as String).toString() else IntegerRange(1, 1).toString()
            )

            "Specialization" -> SpecializationImplementation(
                specific = Resolved<Type>(id = source?.get(0)!!, null, null),
                general = Resolved<Type>(id = target?.get(0)!!, null, null)
            )

            "Classifier",           // Classifier is deprecated and only needed for modeling purposes
            "Class" -> ClassImplementation(declaredName = name, declaredShortName = shortName)

            "Feature" -> FeatureImplementation(
                declaredName = name,
                declaredShortName = shortName,
                direction = direction ?: Feature.FeatureDirectionKind.IN
            )

            "Package" -> PackageImplementation(declaredName = name, declaredShortName = shortName)
            "Documentation" -> DocumentationImplementation(declaredName = name, declaredShortName = name, body = body!!)
            "AnnotatingElement" -> AnnotatingElementImplementation(
                declaredName = name,
                declaredShortName = name,
                body = body!!
            )

            "TextualRepresentation" -> TextualRepresentationImplementation(
                declaredName = name,
                declaredShortName = shortName,
                body = body!!,
                language = language!!
            )

            "Comment" -> CommentImplementation(declaredName = name, declaredShortName = shortName, body = body!!)
            "Relationship" -> RelationshipImplementation(
                declaredName = name,
                declaredShortName = shortName,
                source = source?.toIdentityList() ?: mutableListOf(),
                target = target?.toIdentityList() ?: mutableListOf()
            )

            "Association" -> AssociationImplementation(
                declaredName = name,
                declaredShortName = shortName,
                sources = source?.toIdentityList() ?: mutableListOf(),
                targets = target?.toIdentityList() ?: mutableListOf()
            )

            else -> throw RequestInvalidException("Invalid type in request: '$type'")
        }
        element.elementId = id
        element.owner = Resolved(owner)
        return element
    }
}



fun ElementRequest.update(element: Element) : Element {
    // identification, description, ... are common fields.
    element.declaredName = name?:element.declaredName
    element.declaredShortName = shortName?:element.declaredShortName

    // common method for elements with owned elements
    fun updateOwnedElements(element: Element) {
        element.ownedElement = if (ownedElements != null ) {
            mutableListOf<Resolved<Element>>() .also { list -> ownedElements.forEach { list.add(Resolved(it)) } }
        } else element.ownedElement
    }

    when(element) {
        is Multiplicity -> {
            val range = if (valueSpecs.isNotEmpty()) IntegerRange(valueSpecs[0] as String) else IntegerRange(1, 1)
            element.typeConstraint = mutableListOf(range.toString())
        }

        is Import -> {
            element.target = this.target?.toIdentityList()!!
        }

        is Package -> {
            if (type != "Package")
                throw RequestInvalidException("Update does not allow change of existing type")
            updateOwnedElements(element)
        }

        is Feature -> {
            if (type != "Feature")
                throw RequestInvalidException("Update does not allow change of type")
            updateOwnedElements(element)
        }

        is Classifier -> {
            if (type != "Classifier")
                throw RequestInvalidException("Update does not allow change of existing type")
            updateOwnedElements(element)
        }

        is TextualRepresentation -> {
            if (body != null) element.body = body
            if (language != null) element.language = language
        }

        is Comment -> {
            if (body != null) element.body = body
        }

        is AnnotatingElement -> {
            if (body != null) element.body = body
        }

        else ->
            throw RequestInvalidException("Unknown type; expected Classifier, Package, Property, Feature")
    }
    return element
}
