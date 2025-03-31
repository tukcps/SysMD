package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.exceptions.CyclicDependency
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.services.session.report


/**
 * Interface of a Type, and implementation of its functions on this interface.
 */
interface Type: Namespace {

    var isAbstract: Boolean
    var isSufficient: Boolean

    val isConjugated: Boolean
        get() = getOwnedElementsOfType<Conjugation>().isNotEmpty()

    val generalization: List<Resolved<Type>>
        get() = ownedSpecialization.map { it.general }

    /** Specialization object; nonsense? */
    val specialization: List<Resolved<Type>>
        get() = ownedSpecialization.filter { it !is Redefinition }.map { it.specific }

    val ownedSpecialization: List<Specialization>
        get() = getOwnedElementsOfType<Specialization>().filter { it !is Redefinition }

    override fun resolveNames(): Boolean {
        generalization.forEach {
            if ( it.resolveIdentity(owningNamespace!!) )
                updated = true
        }
        return updated
    }


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
                model?.report(SemanticError( "Cyclic dependency in inheritance of $supertype ", element = this))
            } else {

                if (it.ref == null) {
                    if (it.id != null)
                        it.ref = model?.get(it.id!!) as Type?
                    if (it.ref == null)
                        model?.report(SemanticError("Error trying to find generalization of ${this.qualifiedName}",this))
                    else
                        return (it.ref!!.specializes(supertype, depth+1))
                } else
                    if (it.ref!!.specializes(supertype, depth+1))
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
        val supertypes = generalization.mapNotNull { it.ref }.toMutableList()

        if (this in supertypes || this in visited) {
            model?.report(CyclicDependency(message = "Cyclic dependency in definition of type ${this.qualifiedName}", element = this))
            return listOf()
        }
        if (transitive) {
            generalization.forEach { general ->
                if (general.ref != null) {
                    supertypes += general.ref!!.allSupertypes(true, (visited + this).toMutableSet())
                }
            }
        }
        return supertypes
    }

    val subtypes: MutableSet<Type>

    fun features(): List<Feature> =
        ownedElement.filter { it.ref is Feature }.map { it.ref as Feature }

    /**
     * @return owned end-features of direction in
     */
    fun input(): List<Feature> =
        ownedElement.filter { it.ref is Feature && (it.ref as Feature).direction==Feature.FeatureDirectionKind.IN && (it.ref as Feature).isEnd}.map { it.ref as Feature }

    /**
     * @return owned end-features of direction out
     */
    fun output(): List<Feature> =
        ownedElement.filter { it.ref is Feature && (it.ref as Feature).direction==Feature.FeatureDirectionKind.OUT && (it.ref as Feature).isEnd}.map { it.ref as Feature }

    /**
     * Checks if the supertype has a cycle.
     * @return true, if there is a cycle.
     */
    fun isCyclic(visited: MutableSet<Element> = mutableSetOf()) : Boolean {
        visited += this
        generalization.forEach {
            return when {
                it.ref == null -> false
                it.ref is Anything -> false
                it.ref!! in visited -> true
                else -> it.ref!!.isCyclic(visited)
            }
        }
        return false
    }
}