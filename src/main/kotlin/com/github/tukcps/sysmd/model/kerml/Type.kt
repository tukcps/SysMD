package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.exceptions.Issue.Kind


/**
 * Interface of a Type, and implementation of its functions on this interface.
 */
interface Type: Namespace {

    var isAbstract: Boolean
    var isSufficient: Boolean

    val isConjugated: Boolean
        get() = getOwnedElementsOfType<Conjugation>().isNotEmpty()

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
                model?.status?.error("Cyclic dependency in inheritance of $supertype ", kind=Kind.ERROR_CYCLIC_DEPENDENCY, element = this)
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
        val supertypes = generalization.toMutableList()

        if (this in supertypes || this in visited) {
            model?.status?.error("Cyclic dependency in definition of type ${this.qualifiedName}", kind = Kind.ERROR_CYCLIC_DEPENDENCY, element = this)
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

    /** @return All features of this type */
    fun features(): List<Feature> = ownedElement.filterIsInstance<Feature>()

    /** The owned multiplicity element of this type. */
    fun multiplicity(): Multiplicity? = ownedElement.filterIsInstance<Multiplicity>().firstOrNull()

    /**
     * @return owned end-features of direction in
     */
    fun input(): List<Feature> =
        ownedElement.filterIsInstance<Feature>().filter { it.direction==Feature.FeatureDirectionKind.IN && it.isEnd}

    /**
     * @return owned end-features of direction out
     */
    fun output(): List<Feature> =
        ownedElement.filterIsInstance<Feature>().filter { it.direction==Feature.FeatureDirectionKind.OUT && it.isEnd}

    /**
     * Checks if the supertype has a cycle.
     * @return true, if there is a cycle.
     */
    fun isCyclic(visited: MutableSet<Element> = mutableSetOf()) : Boolean {
        visited += this
        generalization.forEach {
            return when (it) {
                is Anything -> false
                in visited  -> true
                else -> it.isCyclic(visited)
            }
        }
        return false
    }
}