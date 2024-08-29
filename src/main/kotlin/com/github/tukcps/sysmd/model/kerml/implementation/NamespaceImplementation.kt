package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import java.util.*


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
open class NamespaceImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Namespace"
): Namespace, ElementImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    owner=owner,
    textualRepresentation = textualRepresentation,
    elementType = elementType
){
    init {
        this.isStandard = isStandard
        this.isLibraryElement = isLibraryElement
    }
    override fun toString(): String {
        return "$elementType {" +
                (if(declaredName != null) "name='$declaredName', " else "") +
                (if(declaredShortName != null) "shortName='$declaredShortName', " else "") +
                "owner='${owner.ref?.qualifiedName}', " +
                "#owned=${ownedElement.size}, " +
                "#imports=${imports.size}, +" +
                "id='${elementId}')"
    }

    override fun resolveNames(): Boolean {
        updated = super.resolveNames()
        return updated
    }

    override fun clone(): Namespace {
        return NamespaceImplementation(
            declaredName=declaredName,
            declaredShortName = declaredShortName,
            owner = Resolved(owner),
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            isLibraryElement = isLibraryElement,
            isStandard = isStandard
        ).also { klon ->
            klon.model = model
            klon.isTransient = isTransient
        }
    }
}