package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

/**
 * For a type, iterates overall subtypes, and adds clones of the own owned features to it.
 * Then, recursively the same for all subtypes.
 * @param calls used to recognize loops
 */
fun Type.addInheritedToSubtypes(calls: Int = 0) {
    addInheritedFeaturesFromGeneral()
    subtypes.forEach { subtype ->
        subtype.addInheritedToSubtypes(calls + 1)
    }
}

/**
 * For inherited properties that are computed, computation can -- depending on the scope --
 * lead to different results. Hence, we create an inherited feature that is a clone of
 * the superclass' property.
 */
private fun Type.addInheritedFeaturesFromGeneral() {
    // "Clone" features of superclass iff not there!
    // Type element must be part of the model
    require(model != null)

    // Nothing to do for anything
    if (this is Anything) return

    // Nothing to do for references
    if (this is Feature && (referencedFeature != null || isDerived)) return

    val redefinitions = getOwnedElementsOfType<Feature>().filter { it.redefining != null }
    val redefined = getOwnedElementsOfType<Feature>().filter { it.redefining != null }.map { it.redefining!! }
    val redefinedNames = redefined.map { it.escapedName() }
    val toBeCloned    = mutableListOf<Feature>()

    // For each supertype, determine the features that are not re-defined; they are cloned.
    generalization.forEach { supertype ->
        val features = supertype.ownedElement.filterIsInstance<Feature>()
        features.forEach { feature ->
            if (feature.escapedName() !in redefinedNames)
                toBeCloned.add(feature)
        }
    }

    // For redefinitions, replace type, multiplicity, constraints from redefinition
    redefinitions.forEach { feature ->
        // If redefining feature is unresolve, resolve it first
        if(feature.redefining is Unresolved) {
            var found: Feature? = null
            generalization.forEach { supertype ->
                found = supertype.resolve<Feature>((feature.redefining as Unresolved).relativeName!!)
            }
            if (found != null) {
                feature.ownedRelationship.filterIsInstance<Redefinition>().firstOrNull()?.redefinedFeature = found
            }
        }

        // Clone the features from redefined class, except multiplicity and ValueDomain
        // Clone stops cloning if in the model a feature already exists.
        feature.redefining!!.features().filter { it !is Multiplicity }.forEach {
            it.deepCloneWithInheritedFeature(feature)
        }

        // get Type from redefining feature iff not given.
        if (feature.typing.isEmpty())
            feature.redefining?.type?.forEach { type ->
                // Add all types of refined property
                val typing = FeatureTypingImplementation(
                    typedFeature = feature,
                    type = type
                ).also {
                    it.isImplied
                }
                model!!.addOwnedRelationship(typing, feature)
            }

        // get Multiplicity from redefining feature iff not defined
        if (feature.redefining?.multiplicity() != null && feature.multiplicity() == null)
            model?.addOwnedMember(feature.redefining!!.multiplicity()!!.clone(), feature)

        if (feature.redefining?.getOwnedElement("range") != null && feature.getOwnedElement("range") == null) {
            model?.addOwnedMember(feature.redefining!!.getOwnedElement("range")!!.clone(), feature)
        }

        if (feature.redefining?.getOwnedElement("unit") != null && feature.getOwnedElement("unit") != null) {
            model?.addOwnedMember(feature.redefining!!.getOwnedElement("unit")!!.clone(), feature)
        }


        // add prefixes, constraints
        feature.isEnd = feature.redefining!!.isEnd
        feature.isComposite = feature.redefining!!.isComposite
        feature.isPortion = feature.redefining!!.isPortion
        feature.isDerived = feature.redefining!!.isDerived
        feature.isOrdered = feature.redefining!!.isOrdered
        feature.isSufficient = feature.redefining!!.isSufficient

        // Below here is "hack".
        if (feature.name == "range" || feature.name == "spec") { //range for Integer, Real, spec for Boolean
            if (feature.owner is Feature) {
                // remove """ and " " from the string
                val specString = feature.expression!!.trim('"', ' ')
                // set the type constraint to the list of specs split by "," (vectors)
                (feature.owner as Feature).typeConstraint.clear()
                (feature.owner as Feature).typeConstraint.addAll(specString.split(","))
            }
        } else if (feature.name == "unit") {
            if (feature.owner is Feature)
                (feature.owner as Feature).unitConstraint = feature.expression!!.replace("\"", "")
        }
    }

    // get all features of general type if that is already resolved
    val supertypeFeatures: MutableList<Feature> = mutableListOf()
    generalization.forEach { general ->
        supertypeFeatures += general.getOwnedElementsOfType<Feature>()
    }
    val existingFeatures = getOwnedElementsOfType<Feature>().associateBy { Identification(it) }

    // For Supertype-Features that are not re-defined, create a clone
    toBeCloned.forEach { superTypeFeature ->

        // Cyclic dependencies are ok, but we must not clone them recursively ...
        if (this.owner in superTypeFeature.allSupertypes(true))
            return

        // If feature with same identification does not exist, add a clone from general
        val existingWithSameName = existingFeatures[Identification(superTypeFeature)]
        if(existingWithSameName == null) {
            // Simple inheritance  -- we just clone it
            superTypeFeature.deepCloneWithInheritedFeature(this)
        }  // else -- needs to differentiate between features that were cloned by previous run (e.g. by reading library) and that were specified by compiler ...
           // model?.status?.error("Attempt to overload feature ${existingWithSameName.escapedName()} of supertype '${superTypeFeature.qualifiedName}'; use redefinition.")
    }
}


/**
 * finds all features in a namespace, while considering inheritance.
 * @param type the type in which features will be searched
 * @return list of all features in a namespace including inherited.
 */
fun Session.getAllInheritedFeatures(type: Type): Collection<Feature> =
    when (type) {
        is Anything -> emptyList()
        is Feature -> {
            val result = mutableListOf<Feature>()
            type.allSupertypes().forEach { typing ->
                result += mergeFeatures(
                    type.getOwnedElementsOfType<Feature>(),
                    getAllInheritedFeatures(typing)
                )
            }
            result
        }
        else -> type.getOwnedElementsOfType()
    }


/**
 * @return the joined set of properties in case of inheritance:
 * - properties are added if present in a single properties sets
 * - properties are intersected if present in both properties sets
 * - an exception is thrown if a property is present in both, but the inherited is not a
 *   specified value is not a subset of the superclass.
 *   This is considered as an inconsistency in the model.
 */
private fun Session.mergeFeatures(own: Collection<Feature>, inherited: Collection<Feature>): Collection<Feature> {
    val merged = mutableSetOf<Feature>()
    merged.addAll(own)
    for (i in inherited) {
        var overridden = false
        for (o in own) {
            // If there is already a property with the same name, compute subset of it.

            if ((o.declaredName == i.declaredName && i.declaredName != null) || (o.declaredShortName == i.declaredShortName) && i.declaredShortName != null) {
                // Basic requirement for inheritance, must hold in all cases otherwise something went wrong before ...
                // TODO: Limit this to subclass which is sufficient.
                if ( o.generalization.isEmpty() ) return merged

                if ( i in o.allSupertypes(true) )
                    status.inconsistency("subclass type  of '${o.qualifiedName}' must be subclass of '${i.qualifiedName}'", element = o)

                if (o.multiplicityRange !in i.multiplicityRange)
                    status.inconsistency("multiplicity of subclass '${i.qualifiedName}' must be subset of superclass '${o.qualifiedName}'", element = o)

                overridden = true
            }
        }
        if (!overridden) {
            merged.add(i)
        }
    }
    return merged
}