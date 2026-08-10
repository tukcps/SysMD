package com.github.tukcps.sysmd.services.check

import com.github.tukcps.sysmd.compiler.semantics.*
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.datamodel.*
import com.github.tukcps.sysmd.model.generated.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.*
import com.github.tukcps.sysmd.services.session.*
import com.github.tukcps.sysmd.services.session.implementation.*
import kotlin.reflect.*
import kotlin.uuid.*

fun Session.getUnresolvedElements(): List<Unresolved> {
    val elements = repo.elements().filterIsInstance<Unresolved>()
    val links: MutableList<Unresolved> = mutableListOf()

    repo.elements().filterIsInstance<Relationship>().forEach { relationship ->
        relationship.source.forEach { if (it is Unresolved) links.add(it) }
        relationship.target.forEach { if (it is Unresolved) links.add(it) }
    }

    return links + elements
}



/**
 * Checks invariants for debugging and robustness.
 * In particular,
 * - the consistency of owner/owned relationships,
 * - the use of the right Uuid (v4? v5?)
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
    val errors = mutableListOf<Pair<Element,String>>()

    fun fmt(e : Element?) = if(e === null) "<null>" else "$e (${e.elementId})" // + path?

    /** Checks that e.g. ownedRelationship corresponds to e.g. owningRelatedElement */
    fun<E : Element, R : Element> checkReciprocity(e : E, owned : KProperty1<E, List<R>>, owning : KProperty1<R,E?>)
    {
        for(r in owned.get(e))
        {
            if(r is Unresolved)
                continue

            val actual = owning.get(r)

            if(actual === e)
                continue

            errors += r to "${fmt(r)} should be owned by ${fmt(e)}, but its ${owning.name} is ${fmt(r)} instead"
        }
    }

    fun<E : Element> checkNoDuplicates(e : E, field : KProperty1<E, List<Element>>)
    {
        val list = field.get(e)

        list.groupingBy { it }.eachCount().forEach { dup, n ->
            if(n > 1)
                errors += e to "The ${field.name} of ${fmt(e)} contains ${fmt(dup)} $n times"
            if(dup !is Unresolved)
            {
                val actual = map[dup.elementId]

                if(actual === dup)
                    return@forEach

                errors += dup to "${fmt(dup)} is referenced via ${field.name} in ${fmt(e)}" + when {
                    actual === null -> " but is not present in the model"
                    else -> " but its UUID is shadowed by ${fmt(actual)}"
                }

            }
        }
    }

    map.values.forEach { element ->
        // No transient element shall be persisted.
        if (element.isTransient && checkForNoTransients)
            errors += element to "($info) Attempt to persist element that is transient: $element.id"

        // Every element with an id is in the map.
        if ((element.owner?.elementId !in map.keys) && element != element.model.global && !element.isLibraryElement) {
            val error = buildString {
                append(
                    "(", info, ") Inconsistency in model: ", element, " has unknown owner id ",
                    element.owningRelationship?.elementId,
                    "."
                ).appendLine()
                append("  ---> Owner name: ", element.owner?.qualifiedName).appendLine()
                val uuid = when(val qn = element.owner?.qualifiedName) {
                    null -> "???"
                    else if element.isStandard -> UuidPolicies.libraryUuid5(qn)
                    else -> UuidPolicies.notebookUuid5(qn)
                }
                append("  ---> Owner Uuid5 by name: ", uuid).appendLine()
                append("  ---> Owner id owner.ref:  ", element.owner?.elementId," (ID in model: ",element.owner?.elementId in map.keys,")").appendLine()
                append("  ---> Owner id owner.id:   ",element.owningRelationship?.elementId," (ID in model: ",element.owningRelationship?.elementId in map.keys,")")
            }

            errors += element to error
        }

        checkReciprocity(element, Element::ownedRelationship, Relationship::owningRelatedElement)
        checkNoDuplicates(element, Element::ownedRelationship)
        checkNoDuplicates(element, Element::ownedElement)

        if(element is Relationship)
        {
            checkReciprocity(element, Relationship::ownedRelatedElement, Element::owningRelationship)
            checkNoDuplicates(element, Relationship::ownedRelatedElement)
        }

        if(element is Relationship)
        {
            for((ix, re) in element.relatedElements.withIndex())
            {
                if(re !is Unresolved && re.elementId !in map)
                    errors += element to "($info) The relationship ${fmt(element)} has invalid related element #${ix + 1} ${fmt(re)}"
            }
        }
    }

    when(errors.size)
    {
        0 -> return
        1 -> errors.single().run { throw InternalError(second, element = first) }
        // fixme: ugly. These should be separate log entries
        else -> throw InternalError("Multiple inconsistencies:\n${errors.joinToString("\n") { it.second }}", errors.first().first)
    }
}


/**
 * Checks for debugging and robustness.
 * This checks that all element ids are in the model's repository.
 */
fun checkConsistency(repo: Repository, global: Uuid, info: String?= "") {
    if (repo[global] == null)
        throw SysMDException("($info) Inconsistent elements: no Global.")

    repo.elements().forEach {
        if (it.elementId !in repo.keys())
            throw SysMDException("($info) Inconsistent element detected (owner not in elements): $it")
    }
}

fun Session.reportErrorElementsExist() {
    val namespaceImports = getAllOfClass<NamespaceImport>()
    namespaceImports.forEach { namespaceImport ->
        if (namespaceImport.target.firstOrNull() !is Namespace)
            status.error("Expecting namespace for '${namespaceImport.target.firstOrNull()}'.")
    }

    val specializations = getAllOfClass<Specialization>()
    specializations.forEach { specialization ->
        if (specialization.target.firstOrNull() !is Namespace)
            status.error("Expecting namespace for '${specialization.target.firstOrNull()}'.")
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
        relationship.source.filterIsInstance<Unresolved>().forEach {
            status.warn(
                kind = Issue.Kind.ERROR_UNRESOLVED_NAME,
                message = "The name '${it.relativeName}' could not be resolved",
                element = if (relationship.escapedName() != null) relationship.toElementData() else relationship.owningNamespace?.toElementData()
            )
        }
        relationship.target.filterIsInstance<Unresolved>().forEach {
            status.warn(
                kind = Issue.Kind.ERROR_UNRESOLVED_NAME,
                message = "The name of target '${it.relativeName}' of ${relationship.escapedName()?:relationship.elementType().name} could not be resolved",
                element = if (relationship.escapedName() != null) relationship.toElementData() else relationship.owningNamespace?.toElementData()
            )
        }
    }

    // Checks whether all elements without owner could be merged
    /*
    get().filter { it.owningRelationship is Unresolved }.forEach {
        status.warn(
            kind = Issue.Kind.WARN_UNRESOLVED_OWNER,
            message = "Could not resolve owner '${(it as Unresolved).relativeName}}' for adding ${it.escapedName()?:it.elementType} ",
            element = it.toDAO(),
            cause = SysMDException("Could not resolve owning package or element"),
        )
    } */
}


fun Session.checkConsistencyOfBuilders() {
    get().forEach {element ->
        if (element.model.builder != this.builder)
            throw Exception("Inconsistent model: reference to other model.")
        if (element.model.builder != this.builder)
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
    val elements = get().toList()
    elements.forEachIndexed { index, element ->
        if (element == global) return
        if (element is Relationship && element !is Namespace && element !is Dependency) {
            val owner = element.owningRelatedElement
            if (element !in owner.ownedRelationship)
                status.fatal("Inconsistent ownership: ${element.path()} (${element.elementId}) owned by ${owner.path()} (${owner.elementId}) but missing from ownedRelationship" , element = element)
        } else {
            val owner = element.owningNamespace!!
            if (element !in owner.ownedElement)
                status.fatal("Inconsistent ownership: ${element.path()} (${element.elementId}) owned by ${owner.path()} (${owner.elementId}}) but missing from ownedElements", element = element)
        }
        
        if (index > 1000000) {
            status.warn(Issue.Kind.WARN_ITERATIONS_EXCEEDED, "checkOwnership exceeded safety limit. There may be circular ownership references.")
            return
        }
    }
}

/**
 * Checks that all library- or standard elements have a Uuid 5, not Uuid 4.
 * And that the Uuid 5 is generated from the right name.
 */
internal fun Session.checkLibraryElementIds() {

    return

    // TODO Below code is not suitable for new UuidPolicy.
    // - path must be the same
    // - Uuid policy must be set properly

    val elements = get().toList()
    elements.forEachIndexed { index, element ->
        if ((element.isLibraryElement || element.isStandard) && !element.isTransient) {
            val path = element.path()
            val uuid5 = UuidPolicies.uuid5(path)
            if (element.elementId != uuid5) {
                val name = if (element is Membership) element.memberName else element.escapedName()
                status.warn(message =
                    "Library element with path $path for ${element.elementType().name} $name does not have Uuid5 $uuid5, but instead ${element.elementId}",
                    element = element.toElementData()
                )
            }
        }
        
        if (index > 1000000) {
            status.warn(Issue.Kind.WARN_ITERATIONS_EXCEEDED, "checkLibraryElementIds exceeded limit. There may be circular references.")
            return
        }
    }
}
