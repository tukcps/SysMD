package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
open class NamespaceImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Namespace"
): Namespace, ElementImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    textualRepresentation = textualRepresentation,
    elementType = elementType
){

    override fun clone(): Namespace {
        return NamespaceImplementation(
            declaredName=declaredName,
            declaredShortName = declaredShortName,
        ).also {
            it.updateFrom(this)
        }
    }
}

/**
 * Returns the owned element with a given name.
 * @param name A SimpleName that is searched for
 */
inline fun <reified T> Namespace.getOwned(name: SimpleName): T? {
    ownedElement.forEach {
        if (it.declaredName == name)
            return if (it is T) it as T else null
        if (it.declaredShortName == name)
            return if (it is T) it as T else null
    }
    return null
}

