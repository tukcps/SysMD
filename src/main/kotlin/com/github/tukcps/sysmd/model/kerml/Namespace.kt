package com.github.tukcps.sysmd.model.kerml


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
interface Namespace: Element {

    val imports: List<Namespace>
        get() {
            val result: MutableList<Namespace> = mutableListOf()
            ownedElement.forEach {
                if (it is NamespaceImport) {
                    result.add( it.importedNamespace)
                }
            }
            return result
        }

    fun visibleMemberships(): List<Membership> = ownedRelationship.filterIsInstance<Membership>()
}
