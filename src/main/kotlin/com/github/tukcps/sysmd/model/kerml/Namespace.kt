package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImportImplementation
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.services.session.Session


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
interface Namespace: Element {

    val imports: List<Resolved<Namespace>>
        get() {
            val result: MutableList<Resolved<Namespace>> = mutableListOf()
            ownedElement.forEach {
                if (it.ref is Import) {
                    result.add( (it.ref as Import).importedNamespace )
                }
            }
            return result
        }

    fun importNamespace(model: Session, importedNamespace: QualifiedName, importedNamespaceRef: Namespace?=null) {
        val import = NamespaceImportImplementation(importedNamespace = Resolved(id=null, str=importedNamespace, ref=importedNamespaceRef), owner= Resolved(this))
        model.create(import, this)
    }

    fun visibleMemberships(): List<Resolved<Element>> = ownedElement
}
