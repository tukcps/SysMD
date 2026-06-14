package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.expression.Expression
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
import com.github.tukcps.sysmd.services.session.implementation.getAllOfClass
import kotlinx.serialization.Serializable
import kotlin.math.min

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

private fun Session.solveExpressionTypes() {
    get().filterIsInstance<Expression>().forEach {
        it.initType()
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
 * Enumeration with all possible run-levels of the compiler and the solver.
 *
 * On the abstract representation:
 *  - NONE: Makes nothing.
 *  - NAMES_RESOLVED: Name resolution is done for Qualified Names; no checks
 *  - TYPES_INHERITED: Redefinitions are processed
 *  - FEATURE_CHAINES_RESOLVED: Feature chains are resolved
 *  - TYPES_CHECKED: Errors are reported on unresolved names, feature chains, etc.
 *
 *  By the solver:
 *  - VARIABLES_CREATED: Variable are identified for constraint propagation.
 *  - VARIANCE_CHECKED: Types and Co/Contra-Variance are checked.
 *  - SOLVED: Constraint propagation started and terminated.
 *  - ALL: Runs everything.
 */
@Serializable
enum class Runlevel {
    NONE,
    NAMES_RESOLVED,
    TYPES_INHERITED,
    FEATURE_CHAINS_RESOLVED,
    MODEL,
    VARIABLES,
    VARIANCE_CHECKED,
    SOLVED,
    ALL;

    companion object {
        fun toRunlevel(name: String): Runlevel? = entries.find { it.name == name }
        fun fromOrdinal(index: Int): Runlevel {
            return entries.getOrElse(index) {
                if (index >= ALL.ordinal) ALL else NONE
            }
        }

        fun minRunlevel(a: Runlevel, b: Runlevel): Runlevel {
            val min = min(a.ordinal, b.ordinal)
            return fromOrdinal(min)
        }
    }
}


/**
 * Schedules and initialize the properties and the elements.
 * This shall be done before calling propagate().
 */
fun Session.initialize(runlevel: Runlevel) {
    try {
        if (runlevel >= Runlevel.NAMES_RESOLVED) { // Ownership and Type definitions
            resolveAllNames()
            fillCache()

            get().asSequence().filterIsInstance<Type>().forEach { type -> type.checkForCycles() }
            get().asSequence().filterIsInstance<Specialization>().forEach { it.general.subtypes.add(it.specific) }

            solveExpressionTypes()
        }
        if (runlevel >= Runlevel.TYPES_INHERITED) {  // Inheritance and redefinition
            anything.addInheritedToSubtypes() // Calls 'initialize' of types that will add inherited properties.
            anything.addInheritedToSubtypes()
            resolveRedefinitions()
        }

        if (runlevel >= Runlevel.FEATURE_CHAINS_RESOLVED) { // Feature chains considering inherited features
            addEndFeatureReferences()
            resolveAllNames()
            resolveAllFeatureChains()
            giveUUID5()
        }

        if (runlevel >= Runlevel.MODEL) {
            checkNameResolutionSuccessful()
            // We do static semantic checks ...
            getAllOfClass<Type>().asSequence().forEach { type ->
                type.checkForCycles()
            }

            getAllOfClass<Feature>().asSequence().forEach { feature -> feature.checkIsNotTypedByOwner() }
        }

        if (runlevel >= Runlevel.VARIABLES)
            solver.initVariables()

        // Now, we only do checking and reporting of issues to the Board.
        if (runlevel >= Runlevel.VARIANCE_CHECKED) get().asSequence().filterIsInstance<Type>().forEach {
            checkConsistencyOfInheritance(it)
        }

        if (runlevel >= Runlevel.SOLVED)
            solver.propagate()

    } catch (error: Exception) {
        if (error is SysMDException)
            status.error(message = error.message, cause = error)
        else
            status.error(message = "Semantic analysis failed (${error}) ", cause = SysMDException("Initialization failed", cause = error))
    }
}

/**
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveAllNames() {
    /**
     * Replaces all unresolved elements in a list with elements from the model.
     * Elements are resolved by their relativeNames within the given namespace.
     * @param namespace - the namespace where name resolution starts.
     * @param elements - a mutable list of elements in which unresolved elements are replaced by model elements.
     * @return Whether any change was made to the list
     */
    fun resolveQualifiedName(namespace : Namespace, elements: MutableList<Element>) : Boolean
    {
        val iter = elements.listIterator()
        var delta = false

        while(iter.hasNext())
        {
            val unresolved = iter.next()
            if(unresolved !is Unresolved)
                continue
            val relativeName = unresolved.relativeName ?: run {
                status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = namespace)
                iter.remove()
                delta = true
                continue
            }
            // preserve reference to non-existent element to raise error later
            val resolved = namespace.resolve(relativeName) ?: continue
            val target = if(unresolved is UnresolvedMembership) resolved else resolved.memberElement

            if(checkType(unresolved, target, namespace))
            {
                iter.set(target)
                delta = true
            } // should we remove invalid references?
        }

        return delta
    }

    do {
        var delta = false

        get().asSequence().filterIsInstance<Relationship>().filter { it !is Redefinition }.forEach {
            val ns = it.owningNamespace!!
            val s = resolveQualifiedName(ns, it.source)
            val t = resolveQualifiedName(ns, it.target)
            delta = delta || s || t
        }
    } while(delta)
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

/**
 * Resolves names specifically for Redefinition relationships.
 * This is done after inheritance is established because redefinitions often reference
 * inherited features.
 */
internal fun Session.resolveRedefinitions() {
    val unresolvedRedefs = get().asSequence()
        .filterIsInstance<Redefinition>()
        .filter {
            it.source.any { source -> source is Unresolved }
                    || it.target.any { target -> target is Unresolved } }.toSet()

    fun resolveQualifiedNameForRedefinition(namespace: Namespace, elements: MutableList<Element>, redefinition: Redefinition) {
        elements.forEachIndexed { index, element ->
            if (element is Unresolved && element.relativeName != null) {
                namespace.resolve(element.relativeName!!)?.let { resolved ->
                    val resolvedElement = if (element is Membership) resolved else resolved.memberElement
                    
                    if (resolvedElement === redefinition.redefiningFeature) {
                        val hasIdenticalExpression = resolvedElement.expression?.trim() ==
                                                   redefinition.redefiningFeature.expression?.trim()
                        if (hasIdenticalExpression) elements[index] = resolvedElement
                        else status.error("Feature cannot redefine itself: ${element.relativeName}", element = redefinition)
                    } else if (resolvedElement === redefinition.owningRelatedElement) {
                        // Resolved to the owning element itself — this would create a self-referential cycle.
                        // This can happen for inherited Redefinition clones where the local namespace contains
                        // the redefining feature back under the same name.
                        // Instead, try resolving in the supertypes of the enclosing owner feature's owning namespace.
                        val enclosingOwner = (redefinition.owningRelatedElement as? Feature)?.owningNamespace as? Type
                        val altResolved = enclosingOwner?.generalization
                            ?.filterNot { it is Unresolved }?.firstNotNullOfOrNull { supertype ->
                                supertype.resolve(element.relativeName!!)?.let { r ->
                                    val re = if (element is Membership) r else r.memberElement
                                    if (re !== redefinition.owningRelatedElement) re else null
                                }
                            }
                        if (altResolved != null) elements[index] = altResolved
                        // else: leave as Unresolved; will be caught later if needed
                    } else {
                        elements[index] = resolvedElement
                    }
                } ?: status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = namespace)
            }
        }
    }

    var current = unresolvedRedefs
    var progress = true
    var iterations = 0

    while (progress && iterations < 100) {
        iterations++
        current.forEach {
            val ns = it.owningNamespace ?: return@forEach
            resolveQualifiedNameForRedefinition(ns, it.source, it)
            resolveQualifiedNameForRedefinition(ns, it.target, it)
        }
        val new = get().asSequence()
            .filterIsInstance<Redefinition>()
            .filter { it.source.any { source -> source is Unresolved }
                    || it.target.any { target -> target is Unresolved } }
            .toSet()
        progress = (current.size - new.size) > 0
        current = new
    }
    
    if (iterations >= 100) {
        status.warn(Issue.Kind.WARN_ITERATIONS_EXCEEDED, "resolveRedefinitions exceeded maximum iterations (100). There may be circular dependencies in redefinitions.")
    }
}
