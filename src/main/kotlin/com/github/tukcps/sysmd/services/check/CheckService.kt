package com.github.tukcps.sysmd.services.check

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.reportInfo
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


/**
 * Checks invariants for debugging and robustness.
 * @param elements Collection of all elements
 * @param info Info string for error message
 */
fun checkConsistency(
    elements: Collection<Element>,
    info: String?="",
    checkForNoTransients: Boolean = true
) {
    // val roots = elements.filter { it.owner.id == null }
    val map = elements.associateBy( {it.elementId}, {it} )

    map.values.forEach {

        // No transient element shall be persisted.
        if (it.isTransient && checkForNoTransients)
            throw Exception("($info) Attempt to persist element that is transient: $it.id")

        // Every element with an id is in the map.
        if ( it.owner.id != null && (it.owner.id !in map.keys) && it.declaredName != "Global") {
            throw Exception("($info) Inconsistency in model: element '${it.qualifiedName}' has unknown owner id.")
        }
        if ( it is Specialization && !it.isLibraryElement ) {
            if (it.general.id != null && map[it.general.id] == null)
                throw  Exception("($info) Type '${it.qualifiedName}' has unknown superclass id.")
            if (it.specific.id != null && map[it.specific.id] == null)
                throw  Exception("($info) Type '${it.qualifiedName}' has unknown subclass id.")
        }
    }
}


/**
 * Checks for debugging and robustness.
 */
fun checkConsistency(elements: HashMap<UUID, Element>, global: UUID, info: String?= "") {
    if (elements[global] == null)
        throw Exception("($info) Inconsistent elements: no Global.")
    elements.forEach {
        if (it.value.elementId != global && it.value.owner.ref?.elementId !in elements.keys)
            throw Exception("($info) Inconsistent element detected (owner not in elements): $it")
    }
}


/**
 * Function that does the semantic checks on the KerML model data.
 * Errors and issues are reported to the agenda.
 */
fun Session.checkIdentifications() {

    // Checks whether superclass was resolved (e.g., x isA y, with unknown y).
    // Also checks Feature types.
    val elements = get()
    elements.forEach {
        if (it is Type) {
            if (! it.isLibraryElement && !( it is Feature && it.referencedFeature != null))
                it.generalization.forEach { supertype ->
                    if (supertype.ref == null)
                        reportInfo(it, "The type '${supertype.str}' is not defined -- give a definition!")
                }
        }
    }

    // Checks whether definitions were resolved
    getUnownedElements().forEach {
        report(global,
            "Could not resolve owner '${it.path}:${it.startOfPath.qualifiedName}' or ${it.element.escapedName()}",
            ElementNotFoundException(it.element, "Could not resolve owner '${it.path}:${it.startOfPath.qualifiedName}' or ${it.element.escapedName()}"))
    }
}


fun Session.checkConsistencyOfBuilders() {
    get().forEach {element ->
        if (element.model?.builder != this.builder)
            throw Exception("Inconsistent model: reference to other model.")
        if (element.model?.builder != this.builder)
            throw Exception("Inconsistent model: reference to other builder.")
        if (element is Relationship) {
            element.target.forEach {
                if(it.ref != null && it.ref?.model != this)
                    throw Exception("Inconsistent model: reference in targets of ${it}.")
            }
            element.source.forEach {
                if (it.ref != null && it.ref?.model != this)
                    throw Exception("Inconsistent model: reference in sources of ${it}.")
            }
        }
        if (element is Specialization)
            if (element.general.ref != null && element.general.ref?.model?.builder != builder)
                report(element, "Inconsistent builder: in Specialization $element")
    }
}

/**
 * Checks whether ownership and owned fields are consistent.
 * - id refers to referenced element
 * - id of 'owner' and owner.ownedElements are consistent
 */
fun Session.checkOwnership() {
    get().forEach {  element ->
        if (element.owner.ref != null) {
            val ownedByOwner = element.owner.ref?.ownedElement?.associateBy { it.id }?.keys
            if (element.owner.ref != null && element.owner.id != null && element.owner.ref != get(element.owner.id!!)) {
                report(element, "owner id and ref not consistent")
            }
            if (ownedByOwner != null && element.elementId !in ownedByOwner)
                report(element, "owner '${element.owner.ref?.escapedName()?:element.owner.ref?.elementType}' does not refer correctly to owned element '${element.escapedName()?:element.elementType}'")
        }
    }
}

/**
 * Checks that all library- or standard elements have a UUID 5, not UUID 4.
 */
internal fun Session.checkLibraryElementIds() {
    get().forEach { element ->
        if ( (element.isLibraryElement || element.isStandard ) && !element.isTransient){
            val uuid5 = Generators.nameBasedGenerator().generate(element.qualifiedName)
            if (element.elementId != uuid5)
                report(element, "Library element ${element.qualifiedName} does not have correct UUID5")
        }
    }
}
