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
    elementType: String = "Type",
): Type, NamespaceImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
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

    fun inheritedMemberships(
        excluded: Set<Namespace>,
        isRecursive: Boolean,
        includeAll: Boolean,
        filter: Membership.() -> Boolean
    ): Set<Membership> {
        val members = mutableSetOf<Membership>()
        supertypes(false).forEach {
            members += it.visibleMemberships(excluded+this, isRecursive, includeAll, filter)
        }
        return members
    }

     fun visibleMembershipsNew(
        excluded: Set<Namespace>,
        isRecursive: Boolean,
        includeAll: Boolean,
        filter: Membership.() -> Boolean
    ): List<Membership> {
        return super.visibleMemberships(excluded, isRecursive, includeAll, filter) +
                inheritedMemberships(excluded+this, isRecursive, includeAll, filter)
    }

    /**
     * If this Type is conjugated, then return just the originalType of the Conjugation.
     * Otherwise, return the general Types from all ownedSpecializations of this type, and:
     * @param excludeImplied if excludeImplied = false, no, or all nonimplied ownedSpecializations, if excludeImplied = true
     */
    override fun supertypes(excludeImplied: Boolean) : List<Type> {
        if (isConjugated) { // conjugator of this / original type
            TODO()
        } else {
            return ownedSpecialization.map { it.general }
        }
    }

    /**
     * Return the public, protected and inherited Memberships of this Type.
     * @param excludedNamespaces excludes the given set of excludedNamespaces.
     * @param excludedTypes excludes Types in the given set of excludedTypes.
     * @param excludeImplied if true, then also exclude any supertypes from implied Specializations.
     */
    override fun nonPrivateMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ) : List<Membership> {
        val nonPrivate = ownedMembership.filter { it.visibility != Import.VisibilityKind.Private }
        return nonPrivate
    }

    /**
     * Returns all the non-private Memberships of all the supertypes of this Type,
     * @param excludedNamespaces excludes a set of namespaces from the search
     * @param excludedTypes excludes any supertypes that are this Type or are in the given set of excludedTypes.
     * @param excludeImplied If excludeImplied = true, then also transitively exclude any supertypes from implied Specializations.
     *     body: let excludingSelf : Set(Type) = excludedType->including(self) in
     *     supertypes(excludeImplied)->reject(t | excludingSelf->includes(t)).
     *     nonPrivateMemberships(excludedNamespaces, excludingSelf, excludeImplied)
     */
    override fun inheritableMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ): List<Membership> {
        val supertypesExcluded = supertypes(excludeImplied).toSet()-excludedTypes-this
        val result = nonPrivateMemberships(excludedNamespaces, excludedTypes+this, excludeImplied)
        return result
    }

    /**
     * Return the Memberships inheritable from supertypes of this Type with redefined Features removed.
     * When computing inheritable Memberships, exclude Imports of excludedNamespaces,
     * Specializations of excludedTypes.
     * @param excludedNamespaces to exclude imports
     * @param excludedTypes to exclude types to prevent cyclic recursion
     * @param excludeImplied excludes, if true, all implied Specializations.
     */
    override fun inheritedMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ): List<Membership> {
        TODO("Not yet implemented")
    }

    /**
     * For caching subtypes; will be set during initialization after name resolution,
     * but before inheritance
     */
    override val subtypes: MutableSet<Type> = mutableSetOf()
}
