package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.exceptions.Issue.Kind
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.model.datamodel.toElementData


/**
 * Interface of a Type, and implementation of its functions on this interface.
 */
interface Type: Namespace {

    var isAbstract: Boolean
    var isSufficient: Boolean
    val isConjugated: Boolean
        get() = getOwnedElementsOfType<Conjugation>().isNotEmpty()

    /** All features of this type */
    val feature: List<Feature> get() = member.filterIsInstance<Feature>()


    /** The features of this Type that have a non-null direction */
    val directedFeature get() = feature.filter { it.direction !== null }

    /**
     * If this Type is conjugated, then return just the originalType of the Conjugation.
     * Otherwise, return the general Types from all ownedSpecializations of this type, and:
     * @param excludeImplied if excludeImplied = false, no, or all nonimplied ownedSpecializations, if excludeImplied = true
     */
    fun supertypes(excludeImplied: Boolean=false) : List<Type>

    /**
     * Return the public, protected and inherited Memberships of this Type.
     * @param excludedNamespaces excludes the given set of excludedNamespaces.
     * @param excludedTypes excludes Types in the given set of excludedTypes.
     * @param excludeImplied if true, then also exclude any supertypes from implied Specializations.
     */
    fun nonPrivateMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean=false
    ) : List<Membership>

    /**
     * Returns all the non-private Memberships of all the supertypes of this Type,
     * excluding any supertypes that are this Type or are in the given set of excludedTypes.
     * @param excludeImplied If excludeImplied = true, then also transitively exclude any supertypes from implied Specializations.
     *     body: let excludingSelf : Set(Type) = excludedType->including(self) in
     *     supertypes(excludeImplied)->reject(t | excludingSelf->includes(t)).
     *     nonPrivateMemberships(excludedNamespaces, excludingSelf, excludeImplied)
     */
    fun inheritableMemberships(
        excludedNamespaces: Set<Namespace> = emptySet(),
        excludedTypes: Set<Type> = emptySet(),
        excludeImplied: Boolean)
    : List<Membership>

    /**
     * Return the Memberships inheritable from supertypes of this Type with redefined Features removed.
     * When computing inheritable Memberships, exclude Imports of excludedNamespaces,
     * Specializations of excludedTypes.
     * @param excludedNamespaces to exclude imports
     * @param excludedTypes to exclude types to prevent cyclic recursion
     * @param excludeImplied excludes, if true, all implied Specializations.
     */
    fun inheritedMemberships(excludedNamespaces: Set<Namespace> = emptySet(), excludedTypes: Set<Type> = emptySet(), excludeImplied: Boolean): List<Membership>

    val generalization: List<Type>
        get() = ownedSpecialization.map { it.general }

    /** Specialization object; nonsense? */
    val specialization: List<Type>
        get() = ownedSpecialization.map { it.specific }

    val ownedSpecialization: List<Specialization>
        get() = ownedRelationship.filterIsInstance<Specialization>() // .filter { it !is Redefinition }


    /**
     * Checks if a type given as parameter is a direct or indirect supertype of the parameter.
     * It considers itself as a specialization of it.
     * @param supertype the other type
     * @return true, if this is a subtype of the parameter, else false.
     */
    fun specializes(supertype: Type?, depth: Int = 0) : Boolean {
        if (supertype === this)
            return true
        generalization.forEach {
            if (depth > 200) {
                model.status.error("Cyclic dependency in inheritance of $supertype ", kind=Kind.ERROR_CYCLIC_DEPENDENCY, element = this.toElementData())
            } else {
                if (it.specializes(supertype, depth+1))
                    return true
            }
        }
        return false
    }


    /**
     * Returns a list of a Type's supertypes, including Anything
     * @param transitive whether to also add supertypes of supertypes recursively (default: off)
     * @param visited only for internal use; to find cyclic dependencies
     * @return A list of all its supertypes
     */
    fun allSupertypes(transitive: Boolean = false, visited: MutableSet<Type> = mutableSetOf()): List<Type> {
        val supertypes = supertypes().toMutableList()

        if (this in supertypes || this in visited) {
            model.status.error("Cyclic dependency in definition of type ${this.qualifiedName}", kind = Kind.ERROR_CYCLIC_DEPENDENCY, element = this.toElementData())
            return listOf()
        }
        if (transitive) {
            generalization.forEach { general ->
                supertypes += general.allSupertypes(true, (visited + this) as MutableSet<Type>)
            }
        }
        return supertypes
    }

    /** All subtypes of this type after initialization */
    val subtypes: MutableSet<Type>

    /**
     * The owned multiplicity element of this type.
     * Means the number of elements that have this type.
     * Default: 0 .. *
     */
    fun multiplicity(): Multiplicity? = member.filterIsInstance<Multiplicity>().firstOrNull()

    /**
     * @return owned end-features of direction in
     */
    fun input(): List<Feature> =
        member.filterIsInstance<Feature>().filter { it.direction==Feature.FeatureDirectionKind.IN && it.isEnd}

    /**
     * @return owned end-features of direction out
     */
    fun output(): List<Feature> =
        member.filterIsInstance<Feature>().filter { it.direction==Feature.FeatureDirectionKind.OUT && it.isEnd}

    /**
     * Checks if the supertype has a cycle.
     * @return true, if there is a cycle.
     */
    fun isCyclic(visited: MutableSet<Element> = mutableSetOf()) : Boolean {
        visited += this
        generalization.forEach {
            return when (it) {
                in visited  -> true
                else -> it.isCyclic(visited)
            }
        }
        return false
    }

    fun multiplicityRange(): MultiplicityRange
}