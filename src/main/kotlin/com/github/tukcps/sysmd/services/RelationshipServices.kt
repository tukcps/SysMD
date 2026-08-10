package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session


/**
 * Gets all relationships from an element, filtered by name of relationship, and (for Associations) by class.
 * @param element The element that is the source of a relationship
 * @param name The expected name; "*" for ignoring name.
 * @param ofClass The type of Association; null for ignoring it.
 * @return A set of all matching relationships.
 */
fun Session.getRelationshipsFrom(element: Element, name: String, ofClass: Type? = null): Set<Relationship> {
    val sourceOfRelationship = mutableSetOf<Relationship>()
    repo.elements().filterIsInstance<Relationship>().forEach {
        if (element in it.source.map { if (it is Feature) it.referencedFeature?:it else it}) sourceOfRelationship.add(it)
    }
    val result: MutableSet<Relationship> = mutableSetOf()
    sourceOfRelationship.forEach {
        if ((it.declaredName == name)  || (it.declaredShortName == name) || (name == "*")) {
            if (ofClass == null)
                result.add(it)
            else if (it is Type && it.specializes(ofClass))
                result.add(it)
        }
    }
    return result
}


/**
 * Gets all relationships to an element, filtered by name of relationship, and (for Associations) by class.
 * @param element The element that is the target of a relationship
 * @param name The expected name; "*" for ignoring name.
 * @param ofClass The type of Association; null for ignoring it.
 * @return A set of all matching relationships.
 */
fun Session.getRelationshipsTo(element: Element, name: SimpleName, ofClass: Type? = null): Set<Relationship> {
    val targetOfRelationship = mutableSetOf<Relationship>()
    repo.elements().filterIsInstance<Relationship>().forEach {
        if (element in it.target.map { if (it is Feature) it.referencedFeature?:it else it }) targetOfRelationship.add(it)
    }
    val results: MutableSet<Relationship> = mutableSetOf()
    targetOfRelationship.forEach {
        val rel: Element = if (it is ReferenceSubsetting) it.referencedFeature else it
        if ((rel.declaredName == name) || (rel.declaredShortName == name) || (name == "*")) {
            if (ofClass == null)
                results.add(it)
            else if (it is Type && ofClass in it.allSupertypes()+it)
                results.add(it)
        }
    }
    return results
}


/**
 * Retrieves all relationships from an element, filtered by name of relationship, and (for Associations) by class.
 * Furthermore, it considers inheritance and adds all relationships of superclasses.
 * @param element The element that is the source of a relationship
 * @param name The expected name; "*" for ignoring name.
 * @param ofClass The type of Association; null for ignoring it.
 * @return A set of all matching relationships.
 */
fun Session.findRelationshipsFrom(element: Element, name: String, ofClass: Type? = null): Set<Relationship> {
    val result = getRelationshipsFrom(element, name, ofClass)
    return when (element) {
        is Classifier if element.generalization.isEmpty() -> emptySet()
        is Type -> result + findRelationshipsFrom(element.generalization.firstOrNull() as Namespace??:global, name)
        else -> result
    }
}


/**
 * Retrieves all relationships to an element, considering inheritance.
 * Furthermore, it considers inheritance and adds all relationships of superclasses.
 * @param element The element that is the target of a relationship
 * @param name The expected name; "*" for ignoring name.
 * @param ofClass The type of Association; null for ignoring it.
 * @return A set of all matching relationships.
 */
fun Session.findRelationshipsTo(element: Element, name: String, ofClass: Type? = null): Set<Relationship> {
    val result = getRelationshipsTo(element, name, ofClass)
    return when (element) {
        is Classifier if element.generalization.isEmpty() -> emptySet()
        is TypeImplementation -> result + findRelationshipsTo(element.generalization.firstOrNull()?:global, name)
        else -> result
    }
}
