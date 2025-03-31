package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*


/**
 * A Type (KerML 7.3.3)
 */
open class TypeImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    override var isAbstract: Boolean = false,
    override var isSufficient: Boolean = false,
    override var isConjugated: Boolean = false,
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Type",
): Type, NamespaceImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    isLibraryElement = isLibraryElement,
    isStandard = isStandard,
    textualRepresentation=textualRepresentation,
    elementType = elementType
) {
    override fun clone(): Type {
        return TypeImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
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
                "imports='$imports')"
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

    /**
     * For caching subtypes; will be set during initialization after name resolution,
     * but before inheritance
     */
    override val subtypes: MutableSet<Type> = mutableSetOf()

}
