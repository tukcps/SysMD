package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.kerml.implementation.findRecursive
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.qualification
import com.github.tukcps.sysmd.model.util.unqualifiedName


/**
 * KerML foresees a namespace that is an element.
 * "Namespaces can assign unique names to Namespace members, but support multiple aliases per Element.
 * They also support Import of Elements from other Namespaces, enabling an Element to have
 * a different name when imported."
 */
interface Namespace: Element {

    val ownedMembership: List<Membership>
        get() = ownedRelationship.filterIsInstance<Membership>()

    val ownedMember: List<Element>
        get() = ownedMembership.map { it.memberElement }

    val membership: List<Membership>
        get() = ownedMembership + importedMemberships()

    val member: List<Element>
        get() = ownedMembership.map { it.memberElement }

    val ownedImport: List<Import>
        get() = ownedRelationship.filterIsInstance<Import>()

    /**
     * Recursive search for the visible Memberships starting in this namespace.
     * A filter SAM permits to search for a particular membership.
     * @param excluded set of already searched namespaces, to prevent circular search
     * @param isRecursive
     * @param includeAll
     * @param filter lambda that allows filtering; by default true
     * @return Memberships of this namespace, filtered with filter.
     */
    fun visibleMemberships(
        excluded: Set<Namespace> = emptySet(),
        isRecursive: Boolean = false,
        includeAll: Boolean = false,
        filter: Membership.() -> Boolean = { true }
    ): List<Membership>

    /**
     * Returns the imported memberships of this namespace.
     * A filter SAM permits to search for particular memberships, e.g., with a certain name.
     * @param excluded set of already searched namespaces, to prevent circular search
     * @param filter - lambda that allows filtering of the result; by default true
     * @return All memberships that are imported.
     */
    fun importedMemberships(
        excluded: Set<Namespace> = emptySet(),
        filter: Membership.() -> Boolean = { true }
    ): List<Membership>

    /**
     * Returns the membership of owned memberships of owned and imported memberships of a given kind.
     * @param visibility Kind of visibility, all owned and imported if null.
     * @param excluded For recursion, allows excluding visited namespaces from recursion.
     */
    fun membershipsOfVisibility(
        visibility: Import.VisibilityKind?,
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean = {true}
    ): List<Membership>

    /**
     * Resolves a qualified name to the respective membership following the name resolution method.
     * Resolution can start locally or global.
     * @param qualifiedName the qualified name
     * @return the membership of an element to which the parameter resolves
     */
    fun resolve(qualifiedName: QualifiedName): Membership? {
        val found = findRecursive(qualifiedName, emptySet(), emptySet())
        return (found?.memberElement as? Feature)?.referencedFeature?.let { return it.owningRelationship } ?: found
    }

    /**
     * Searches in the local scope for a name
     * @param name A simple name
     */
    fun resolveVisible(name: SimpleName): Membership? {
        return visibleMemberships(isRecursive = false, includeAll = false)
             { this.memberName == name || this.memberShortName == name }.firstOrNull()
    }

    /**
     * Resolves a qualified name to the respective membership, starting at the root namespace.
     * @param qualifiedName the qualified name
     * @return the membership of an element to which the parameter resolves
     */
    fun resolveGlobal(qualifiedName: QualifiedName): Membership?

    /**
     * Resolves a simple name to a membership, searching only in the local scope.
     * @param name a name that is resolved locally
     * @return the membership of an element to which the parameter resolves
     */
    fun resolveLocal(name: SimpleName): Membership?


    /**
     * Following standard. Requires work in visible memberships of
     * associations and redefinitions.
     */
    fun resolveNew(qualifiedName: QualifiedName): Membership? {
        val name = qualifiedName.unqualifiedName()
        val qualification = qualifiedName.qualification()
        return when {
            qualification.isNullOrEmpty() ->  resolveLocal(name)
            qualification == "$"          ->  resolveGlobal(name)
            else -> (resolveNew(qualification)?.memberElement as? Namespace)?.resolveVisible(name)
        }
    }
}
