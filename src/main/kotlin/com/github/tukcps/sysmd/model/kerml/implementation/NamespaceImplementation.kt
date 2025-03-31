package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
open class NamespaceImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Namespace"
): Namespace, ElementImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    textualRepresentation = textualRepresentation,
    elementType = elementType
){
    init {
        this.isStandard = isStandard || (owner.ref?.isStandard == true)
        this.isLibraryElement = isLibraryElement || (owner.ref?.isStandard == true)
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
            isLibraryElement = isLibraryElement,
            isStandard = isStandard
        ).also { klon ->
            klon.model = model
            klon.isTransient = isTransient
        }
    }
}