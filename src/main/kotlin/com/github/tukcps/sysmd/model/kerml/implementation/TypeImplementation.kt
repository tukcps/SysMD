package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import java.util.*


/**
 * A Type (KerML 7.3.3)
 */
open class TypeImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    override var isAbstract: Boolean = false,
    override var isSufficient: Boolean = false,
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Type"
): Type, NamespaceImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElement=ownedElement,
    owner=owner,
    isLibraryElement = isLibraryElement,
    isStandard = isStandard,
    textualRepresentation=textualRepresentation,
    elementType = elementType
) {
    override fun clone(): Type {
        return TypeImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner)
        ).also { klon ->
            klon.model = model
            klon.updated = updated
        }
    }

    override fun toString(): String {
        return "Type (" +
                "declaredName='$declaredName', " +
                (if (declaredShortName != null) "declaredShortName=$declaredShortName" else "") +
                "supertype='${getOwnedElementOfType<Specialization>()?.general}', " +
                "imports='$imports', " +
                "id='${elementId}...')"
    }

    /**
     * Does name resolution and, if successful, sets updated=true
     */
    override fun resolveNames(): Boolean {
        updated = super<Type>.resolveNames() or updated
        updated = super<NamespaceImplementation>.resolveNames() or updated
        return updated
    }

    override fun updateFrom(template: Element) {
        if (template is TypeImplementation) {
            super.updateFrom(template)
        }
    }

    override val isConjugated: Boolean
        get() = TODO()
}
