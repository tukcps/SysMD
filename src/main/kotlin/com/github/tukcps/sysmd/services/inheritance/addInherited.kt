package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation

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
        val features = supertype.visibleMemberships().filter { it.memberElement is Feature }.map { it.memberElement as Feature }
        features.forEach { feature ->
            if (feature.escapedName() !in redefinedNames)
                toBeCloned.add(feature)
        }
    }

    // For redefinitions, replace type, multiplicity, constraints from redefinition
    redefinitions.forEach { feature ->
        // If redefining feature is unresolve, resolve it first
        if(feature.redefining is Unresolved) {
            var found: Membership? = null
            generalization.forEach { supertype ->
                found = supertype.resolve((feature.redefining as Unresolved).relativeName!!)
            }
            if (found != null) {
                feature.ownedRelationship.filterIsInstance<Redefinition>().firstOrNull()?.redefinedFeature = found.memberElement as Feature
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

        // ... ValueDomain ... with unit and range
        if (feature.redefining?.resolveLocal("range") != null && feature.resolveLocal("range") == null) {
            model?.addOwnedMember(feature.redefining!!.resolveLocal("range")!!.memberElement, feature)
        }

        if (feature.redefining?.resolveLocal("unit") != null && feature.resolveLocal("unit") != null) {
            model?.addOwnedMember(feature.redefining!!.resolveLocal("unit")!!.memberElement, feature)
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
        }
    }

    // get all features of general type if that is already resolved
    val supertypeFeatures: MutableList<Feature> = mutableListOf()
    generalization.forEach { general ->
        supertypeFeatures += general.visibleMemberships().mapNotNull { if (it is Feature) it.memberElement as Feature else null }
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
            /*
            model!!.addOwnedRelationship(
                MembershipImplementation(membershipOwningNamespace = this, memberElement = superTypeFeature),
                this)
             */
        }  // else -- needs to differentiate between features that were cloned by previous run (e.g. by reading library) and that were specified by compiler ...
           // model?.status?.error("Attempt to overload feature ${existingWithSameName.escapedName()} of supertype '${superTypeFeature.qualifiedName}'; use redefinition.")
    }
}