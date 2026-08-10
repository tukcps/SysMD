package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.model.kerml.*
import kotlin.uuid.Uuid

/**
 * An in-memory model repository that allows us
 * - lookup of element references by id
 * - caching of pre-defined type references
 * - fast tracking of relationships by reference
 * - buffering a computed schedule for constraint propagation
 */
class Repository {

    // Caches of other elements, filled during initialization
    var anything: Classifier? = null
    var scalarType: Type? = null
    var numberType: DataType? = null
    var realType: DataType? = null
    var booleanType: DataType? = null
    var integerType: Type? = null
    var naturalType: Type? = null
    var stringType: Type? = null
    var inRangeType: Type? = null
    var quantity: Type? = null
    var occurrence: Type? = null
    var links: Type? = null
    var range: Type? = null

    /** The in-memory representation uses hash-maps for efficient search. */
    private val elements: LinkedHashMap<Uuid, Element> = LinkedHashMap()

    /** Access to the elements via [] */
    operator fun get(uuid: Uuid?) = elements[uuid]

    /** Access to the elements via []; existing keys are not overwritten. */
    operator fun set(uuid: Uuid, element: Element): Element = elements.compute(uuid) { _, existing ->
        existing?.also {
            if (element is Namespace)
                existing.updateFrom(element)
            // handle e.g. changed specializations
            if(existing is Relationship && existing.ownedRelatedElement.isEmpty() && existing.ownedRelationship.isEmpty())
                existing.updateFrom(element)
        } ?: element
    }!!

    fun remove(uuid: Uuid?) {
        if (uuid != anything) elements.remove(uuid)
    }

    fun elements() = elements.values
    fun keys() = elements.keys

    /** Map that permits finding incoming relationships of an element */
    val targetOfRelationship: LinkedHashMap<Element, MutableSet<Relationship>> = LinkedHashMap()

    /** Map that permits finding outgoing relationships of an element */
    val sourceOfRelationship: LinkedHashMap<Element, MutableSet<Relationship>> = LinkedHashMap()

    /** Projects that have been loaded into the session; as of now identified by name, not ID (!!!) */
    val loadedProjects: MutableSet<String> = mutableSetOf()

}