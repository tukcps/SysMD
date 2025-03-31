package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.kerml.Type
import java.util.*

/**
 * An in-memory model repository that allows us
 * - lookup of element references by id
 * - caching of pre-defined type references
 * - fast tracking of relationships by reference
 * - buffering a computed schedule for constraint propagation
 */
class Repository {

    /** The in-memory representation uses hash-maps for efficient search. */
    val elements: HashMap<UUID, Element> = hashMapOf()

    /** Map that permits finding incoming relationships of an element */
    val targetOfRelationship: HashMap<Element, MutableSet<Relationship>> = hashMapOf()

    /** Map that permits finding outgoing relationships of an element */
    val sourceOfRelationship: HashMap<Element, MutableSet<Relationship>> = hashMapOf()

    /** List that stores the features that are part of constraint propagation */
    val schedule: MutableList<Variable> = mutableListOf()

    /** Caches of important types */
    var scalarType: Type? = null
    var numberType: Type? = null
    var realType: Type? = null
    var booleanType: Type? = null
    var integerType: Type? = null
    var naturalType: Type? = null
    var stringType: Type? = null
    var inRangeType: Type? = null
    var occurrence: Type? = null
    var links: Type? = null


    /** Map for elements with the not-yet identified owner */
    var unownedElements: MutableList<Session.UnresolvedElement> = mutableListOf()

    /** Projects that have been loaded into the session; as of now identified by name, not ID (!!!) */
    val loadedProjects: MutableSet<String> = mutableSetOf()

    fun reset() {
        elements.clear()
        unownedElements.clear()
        targetOfRelationship.clear()
        sourceOfRelationship.clear()
        schedule.clear()
        loadedProjects.clear()
        scalarType = null
        numberType = null
        realType = null
        booleanType = null
        integerType = null
        stringType = null
        inRangeType = null
        occurrence = null
        links= null
    }
}