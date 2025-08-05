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
    textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "Type",
): Type, NamespaceImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    textualRepresentation=textualRepresentation,
    elementType = elementType
) {
    override fun clone(): Type {
        return TypeImplementation().also { klon -> klon.updateFrom(this) }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is TypeImplementation) {
            isConjugated = template.isConjugated
            isSufficient = template.isSufficient
            isAbstract = template.isAbstract
        }
    }

    /**
     * For caching subtypes; will be set during initialization after name resolution,
     * but before inheritance
     */
    override val subtypes: MutableSet<Type> = mutableSetOf()
}
