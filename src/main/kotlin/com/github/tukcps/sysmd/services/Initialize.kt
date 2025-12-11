package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ReferenceSubsettingImplementation
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.check.checkConsistencyOfInheritance
import com.github.tukcps.sysmd.services.check.checkNameResolutionSuccessful
import com.github.tukcps.sysmd.services.inheritance.addInheritedToSubtypes
import com.github.tukcps.sysmd.services.inheritance.checkForCycles
import com.github.tukcps.sysmd.services.inheritance.checkIsNotTypedByOwner
import com.github.tukcps.sysmd.services.resolve.resolveFeatureChain
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.getAllOfClass

private fun Session.fillCache() {
    // Cache frequently used types for use in semantic checks
    repo.numberType = global.resolve("ScalarValues::Number")?.member<DataType>()
    repo.scalarType = global.resolve("ScalarValues::ScalarValue")?.member<DataType>()
    repo.realType = global.resolve("ScalarValues::Real")?.member<DataType>()
    repo.integerType = global.resolve("ScalarValues::Integer")?.member<DataType>()
    repo.naturalType = global.resolve("ScalarValues::Natural")?.member<DataType>()
    repo.booleanType = global.resolve("ScalarValues::Boolean")?.member<DataType>()
    repo.stringType = global.resolve("ScalarValues::String")?.memberElement as Type?
    repo.inRangeType = global.resolve("Ranges::InRange")?.memberElement as Type?
    repo.occurrence = global.resolve("Occurrences::Occurrence")?.memberElement as Type?
    repo.links      = global.resolve("Links::Link")?.memberElement as Association?
    repo.quantity   = global.resolve("Ranges::QuantityInRange")?.memberElement as Type?
    repo.range      = global.resolve("Ranges::InRange")?.memberElement as Type?
}

private fun Session.giveUUID5(){
    get().filter { it.isLibraryElement || it.isStandard }. forEach {
        val old = it.elementId
        it.generateUUID()
        if (old != it.elementId) {
            repo.elements.remove(old)
            repo.elements[it.elementId!!] = it
        }
    }
}


/**
 * For Connectors, the end features are references to the source and target.
 * This method gets all Connectors and adds to each end feature a reference to source resp. targets.
 */
private fun Session.addEndFeatureReferences() {
    get().filterIsInstance<Connector>().forEach { connector ->
        val ends = connector.ownedMembership.filter { it.memberElement is Feature && (it.memberElement as Feature).isEnd  }.map { it.memberElement }
        // TODO: consider multiplicity.
        if (connector.source.isNotEmpty() && connector.target.isNotEmpty() && ends.size >= 2) {
            val sourceEnd = ends[0] as Feature
            val targetEnd = ends[1] as Feature
            val ref = ReferenceSubsettingImplementation(sourceEnd, connector.source.first() as Feature)
            addOwnedRelationship(ref, sourceEnd)
            val refT = ReferenceSubsettingImplementation(targetEnd, connector.target.first() as Feature)
            addOwnedRelationship(refT, targetEnd)
        }
    }
}

/**
 * Schedules and initialize the properties and the elements.
 * This shall be done before calling propagate().
 */
fun Session.initialize(level: Int = 100) {
    if (settings.initialize) {
        try {
            if (level > 0) { // Ownership and Type definitions
                resolveAllNames()
                fillCache()
                get().asSequence().filterIsInstance<Type>().forEach { type -> type.checkForCycles() }
                get().asSequence().filterIsInstance<Specialization>().forEach { it.general.subtypes.add(it.specific) }
            }
            if (level > 1) {  // Inheritance and redefinition
                anything.addInheritedToSubtypes() // Calls 'initialize' of types that will add inherited properties.
                anything.addInheritedToSubtypes()
            }
            if (level > 2) { // Feature chains considering inherited features
                addEndFeatureReferences()
                resolveAllNames()
                resolveAllFeatureChains()
                giveUUID5()
            }

            if (level > 3)
                checkNameResolutionSuccessful()

            if (level > 4) {
                // We do static semantic checks ...
                getAllOfClass<Type>().asSequence().forEach { type ->
                    type.checkForCycles()
                }

                getAllOfClass<Feature>().asSequence().forEach { feature -> feature.checkIsNotTypedByOwner() }
            }

            if (level > 5)
                solver.initVariables()

            // Now, we only do checking and reporting of issues to the Agenda.
            if (level > 7) get().asSequence().filterIsInstance<Type>().forEach {
                checkConsistencyOfInheritance(it)
            }
        } catch (error: Exception) {
            if (error is SysMDException)
                status.error(message = error.message, cause = error)
            else
                status.error(message = "Semantic analysis failed (${error}) ", cause = SysMDException("Initialization failed", cause = error))
        }
    }
}


/**
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveAllNames() {

    fun relationsWithUnresolvedReferences() =
        get().asSequence().filterIsInstance<Relationship>()
        .filter { rel -> rel !is Redefinition && (rel.source.any { it is Unresolved } || rel.target.any { it is Unresolved }) }
        .toSet()

    /**
     * Replaces an unresolved element by a element from the model.
     * The element of the model is resolved from a given namespace with a given qualified name.
     * @param namespace - the namespace where name resolution starts.
     * @param elements - a mutable list of elements in which unresolved elements are replaced by model elements.
     */
    fun resolveQualifiedName(namespace: Namespace, elements: MutableList<Element>) {
        for (index in elements.indices) {
            if (elements[index] is Unresolved) {
                if ((elements[index] as Unresolved).relativeName == null)
                    status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = namespace)
                else {
                    val unresolved = elements[index] as Unresolved
                    // Try resolving all unresolved names except redefinitions that are done later
                    val resolved = namespace.resolve(unresolved.relativeName!!)
                    if (resolved != null)
                        when (unresolved) {
                            is Feature if (resolved.memberElement !is Feature)
                                -> status.error("Expecting a kind of feature", kind = Issue.Kind.ERROR_TYPE_WRONG, element = namespace)

                            is Type if (resolved.memberElement !is Type)
                                -> status.error("Expecting a kind of type", kind = Issue.Kind.ERROR_TYPE_WRONG, element = namespace)

                            is Namespace if (resolved.memberElement !is Namespace)
                                -> status.error("Expecting a kind of namespace", kind = Issue.Kind.ERROR_TYPE_WRONG, element = namespace)

                            else -> if (unresolved is Membership)
                                elements[index] = resolved
                            else
                                elements[index] = resolved.memberElement
                        }
                }
            }
        }
    }

    var relationshipsWithUnresolvedReferences = relationsWithUnresolvedReferences().filter { it !is Redefinition }.toSet()
    var progress = true

    while (progress) {
        relationshipsWithUnresolvedReferences.forEach {
            resolveQualifiedName(it.owningNamespace!!, it.source)
            resolveQualifiedName(it.owningNamespace!!, it.target)
        }
        val new = relationsWithUnresolvedReferences()
        progress = (relationshipsWithUnresolvedReferences.size - new.size) > 0
        relationshipsWithUnresolvedReferences = new
    }
}



/**
 * Calls the 'initialize' function of all elements except Expressions in any order.
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveAllFeatureChains() {

    initializeAllAssociations()

    fun resolveFeatureChain(namespace: Namespace, elements: MutableList<Element>) {
        for (index in elements.indices) {
            if (elements[index] is UnresolvedFeatureChain) {
                if ( (elements[index] as Unresolved).relativeName == null)
                    status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved feature chain with no name", element = elements[index].owningNamespace)
                else {
                    val unresolvedFeature = elements[index] as Unresolved
                    val resolvedFeature = namespace.resolveFeatureChain(unresolvedFeature.relativeName!!)
                    if (resolvedFeature != null)
                        elements[index] = resolvedFeature
                }
            }
        }
    }

    get().asSequence().filterIsInstance<Relationship>().forEach {
        resolveFeatureChain(it.owningNamespace!!, it.source)
        resolveFeatureChain(it.owningNamespace!!, it.target)
    }
}

/**
 * Sets the Association source and target types to the respective end features.
 */
internal fun Session.initializeAllAssociations() {
    get().asSequence().filterIsInstance<Association>().forEach { association ->
        // --> to initialize after inheritance!
        val endFeature = association.ownedElement
            .asSequence().filterIsInstance<Feature>().filter { feature -> feature.isEnd }.toList()

        if (endFeature.size >= 2) {
            association.sourceType = endFeature[0]
            association.targetType = mutableListOf(endFeature[1])
            for(i in 2..<endFeature.size) {
                association.targetType = mutableListOf(endFeature[i])
            }
        }
    }
}

/**
 *
 */
fun getVarNames(namespace: QualifiedName, membership: Membership): List<String> {
    val element = membership.memberElement
    val elementName = (if (namespace.isNotEmpty()) "$namespace::" else "") + element.escapedName()
    val result = mutableListOf<String>()

    if (element is Type && element.specializes(element.model!!.repo.scalarType)) {
        result.add(elementName)
    }

    if (element is Namespace) {
        element.ownedMembership.forEach {
            val vars = getVarNames(elementName, it)
            result.addAll(vars)
        }
    }
    return result
}
