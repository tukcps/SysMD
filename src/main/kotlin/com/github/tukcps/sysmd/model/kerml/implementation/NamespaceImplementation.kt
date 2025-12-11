package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.*


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
open class NamespaceImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Namespace"
): Namespace, ElementImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType = elementType
){

    /**
     * Returns the visible memberships in this namespace
     * @param excluded set of already searched namespaces, to prevent circular search
     * @param isRecursive whether to search recursively
     * @param includeAll whether to include all or only public memberships
     * @param filter a lambda for filtering
     */
    override fun visibleMemberships(
        excluded: Set<Namespace>,
        isRecursive: Boolean,
        includeAll: Boolean,
        filter: Membership.() -> Boolean
    ) : List<Membership> =
        if (this in excluded) emptyList()
        else (ownedMembership + importedMemberships(excluded, filter)).filter {
            it.visibility == Import.VisibilityKind.Public && it.filter() }

    /**
     * Returns the imported memberships of this namespace
     * @param excluded set of already searched namespaces, to prevent circular search
     * @return All memberships that are imported.
     */
    override fun importedMemberships(
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean)
    : List<Membership> =
        if (this in excluded) emptyList()
        else ownedImport.mapNotNull { it.importedMemberships(excluded + this, filter).firstOrNull() }

    /**
     * Returns the membership of owned memberships of owned and imported memberships of a given kind.
     * @param visibility Kind of visibility, all owned and imported if null.
     * @param excluded For recursion, allows excluding visited namespaces from recursion.
     */
    override fun membershipsOfVisibility(
        visibility: Import.VisibilityKind?,
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> =
        ownedMembership + importedMemberships(excluded + this, filter).filter { it.visibility == visibility && it.filter()}

    /**
     * Resolves a qualified name to the respective membership, starting at the root namespace.
     * @param qualifiedName the qualified name
     * @return the membership of an element to which the parameter resolves
     */
    override fun resolveGlobal(qualifiedName: QualifiedName): Membership? {
        return model?.global?.findRecursive(qualifiedName, emptySet(), emptySet())
    }

    /**
     * Resolves a simple name to a membership, searching in the local scope, and then in the owning scopes
     * until the root namespace is reached.
     * @param name a name that is resolved locally
     * @return the membership of an element to which the parameter resolves
     */
    override fun resolveLocal(name: SimpleName): Membership? {
        return visibleMemberships(isRecursive = false, includeAll = false) {
           memberName == name || memberShortName == name
        }.firstOrNull()
    }

    override fun clone(): Namespace =
        NamespaceImplementation().also { it.updateFrom(this) }

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


/**
 * Searches recursively for a qualified name, starting from the namespace.
 * @param qualifiedName the qualified name that is searched
 * @param excludeNamespace optionally, a set of Namespaces that have already been searched; needed to detect cyclic includes.
 * @param excludeType optionally, the set of searched superclasses; needed to detect cyclic definitions
 * @param searchInOwner optionally, whether the given qualified name is initially given as a simple name, or just
 * became a simple name via recursion that cuts qualified name down.
 * @param searchInSuperClass optionally, whether to recurse into superclasses.
 * @return An element of or null, if the name cannot be resolved.
 */
fun Namespace.findRecursive(
    qualifiedName: QualifiedName,
    excludeNamespace: Set<Namespace>,
    excludeType: Set<Type>,
    searchInOwner: Boolean = true,
    searchInSuperClass: Boolean = true,
): Membership? {

    if (this in excludeNamespace) return null
    if (qualifiedName == "self") return this.owningRelationship
    if (qualifiedName == "that") return owner?.owningRelationship

    val qualification = qualifiedName.qualification()
    val name = qualifiedName.unqualifiedName()

    // If we have a simple name, we can search in owned elements that are identified by simple names.
    if (qualification.isNullOrEmpty()) {
        val found = resolveLocal(name) 
        if (found != null) return found
    } else if (qualification == "$")
        return resolveGlobal(qualifiedName)
    else {
        // Search downwards in matching owned namespaces, shortening the qualified name by 1st name.
        val matchingNamespace = resolveLocal(qualifiedName.firstName())?.memberElement
        if (matchingNamespace is Namespace) {
            val newName: QualifiedName = qualifiedName.dropFirstName()
            val found = matchingNamespace.findRecursive(newName, emptySet(), emptySet(),false, searchInSuperClass)
            if (found != null) return found
        }
    }

    // Search in supertypes
    if (this is Type && searchInSuperClass) {
        this.allSupertypes().forEach {
            val found = it.findRecursive(qualifiedName, excludeNamespace+this, excludeType+this, searchInOwner=false, searchInSuperClass=true)
            if (found != null) return found
        }
    }

    // Search in owning namespace
    return if (searchInOwner)
        owningNamespace?.findRecursive(qualifiedName, excludeNamespace, excludeType, true)
    else
        null
}
