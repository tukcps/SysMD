package com.github.tukcps.sysmd.services.check

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.session.Session
import java.util.*

fun Session.getUnresolvedElements(): List<Unresolved> {
    val elements = repo.elements.values.filterIsInstance<Unresolved>()
    val links: MutableList<Unresolved> = mutableListOf()

    repo.elements.values.filterIsInstance<Relationship>().forEach { relationship ->
        relationship.source.forEach { if (it is Unresolved) links.add(it) }
        relationship.target.forEach { if (it is Unresolved) links.add(it) }
    }

    return links + elements
}



/**
 * Checks invariants for debugging and robustness.
 * In particular,
 * - the consistency of owner/owned relationships,
 * - the use of the right UUID (v4? v5?)
 * Normally, this should not be needed ... but who knows?
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
        if ( it.elementId != null && (it.elementId !in map.keys) && it != it.model?.global) {
            println("  ---> Owner name: ${it.owner?.qualifiedName}")
            println("  ---> Owner UUID5 by name: ${Generators.nameBasedGenerator().generate(it.owner?.qualifiedName)}")
            println("  ---> Owner id owner.ref:  ${it.owner?.elementId} (ID in model: ${it.owner?.elementId in map.keys})")
            println("  ---> Owner id owner.id:   ${it.owningRelationship?.elementId} (ID in model: ${it.owningRelationship?.elementId in map.keys})")
            throw Exception("($info) Inconsistency in model: element '${it.qualifiedName}' has unknown owner id ${it.owningRelationship?.elementId}.")
        }
        if ( it is Specialization && !it.isLibraryElement ) {
            if (it.general.elementId != null && map[it.general.elementId] == null)
                throw  SysMDException("($info) Type '${it.qualifiedName}' has unknown superclass id.")
            if (it.specific.elementId != null && map[it.specific.elementId] == null)
                throw  SysMDException("($info) Type '${it.qualifiedName}' has unknown subclass id.")
        }
    }
}


/**
 * Checks for debugging and robustness.
 * This checks that all element ids are in the model's repository.
 */
fun checkConsistency(elements: HashMap<UUID, Element>, global: UUID, info: String?= "") {
    if (elements[global] == null)
        throw SysMDException("($info) Inconsistent elements: no Global.")
    elements.forEach {
        if (it.value.elementId !in elements.keys)
            throw SysMDException("($info) Inconsistent element detected (owner not in elements): $it")
    }
}


/**
 * Function that does the semantic checks on the KerML model data.
 * Errors and issues are reported to the agenda.
 */
fun Session.checkNameResolutionSuccessful() {

    // Checks whether superclass was resolved (e.g., x isA y, with unknown y).
    // Also checks Feature types.
    val elements = get()
    elements.filterIsInstance<Relationship>().forEach { relationship ->
        relationship.source.filter { it is Unresolved }.forEach {
            status.warn(kind = Issue.Kind.ERROR_UNRESOLVED_NAME, message = "The name '${(it as Unresolved).relativeName}' could not be resolved", element = relationship)
        }
        relationship.target.filter { it is Unresolved }.forEach {
            status.warn(
                kind = Issue.Kind.ERROR_UNRESOLVED_NAME,
                message = "The target '${(it as Unresolved).relativeName}' could not be resolved",
                element = relationship
            )
        }
    }

    // Checks whether all elements without owner could be merged
    get().filter { it.owningRelationship is Unresolved }.forEach {
        status.warn(
            kind = Issue.Kind.WARN_UNRESOLVED_OWNER,
            message = "Could not resolve owner '${(it as Unresolved).relativeName}}' for adding ${it.escapedName()?:it.elementType} ",
            element = it,
            cause = SysMDException("Could not resolve owning package or element"),
        )
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
                if(it.model != this)
                    throw Exception("Inconsistent model: reference in targets of ${it}.")
            }
            element.source.forEach {
                if (it.model != this)
                    throw Exception("Inconsistent model: reference in sources of ${it}.")
            }
        }
    }
}

/**
 * Checks whether ownership and owned fields are consistent.
 * - id refers to referenced element
 * - id of 'owner' and owner.ownedElements are consistent
 */
fun Session.checkOwnership() {
    get().forEach {  element ->
        if (element == global) return@forEach
        if (element is Relationship && element !is Namespace && element !is Dependency) {
            if (element !in element.owningRelatedElement.ownedRelationship)
                status.fatal("Inconsistent ownership: ${element.path()} owns ${element.owningRelatedElement.path()}" , element = element)
        } else {
            if (element !in element.owningNamespace?.ownedElement!!)
                status.fatal("Inconsistent ownership detected", element = element)
        }
    }
}

/**
 * Checks that all library- or standard elements have a UUID 5, not UUID 4.
 * And that the UUID 5 is generated from the right name.
 */
internal fun Session.checkLibraryElementIds() {
    get().forEach { element ->
        if ( (element.isLibraryElement || element.isStandard )
            && !element.isTransient)
        {
            val path = element.path()
            val uuid5 = Generators.nameBasedGenerator().generate(path)
            if (element.elementId != uuid5)
                status.warn(Issue.Kind.WARN,"Library element with path $path for ${element.elementType} ${element.escapedName()} does not have correct UUID5", element = element)
        }
    }
}
