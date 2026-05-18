package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation


/**
 * Topologically iterates over all types in the model, ensuring supertypes are evaluated
 * before subtypes, and re-evaluating when new cloned features are added.
 * @param calls kept for backwards compatibility but unused
 */
fun Type.addInheritedToSubtypes(@Suppress("unused") calls: Int = 0) {
    val session = this.model ?: return
    val processed = mutableSetOf<Type>()

    fun process(type: Type) {
        if (type in processed) return

        if (type.qualifiedName?.let {
            it.contains("annotatedElement") || it.contains("AnnotatingElement") ||
            it.contains("Annotation") || it.contains("Comment") ||
            it.contains("MetadataFeature")
        } == true) {
            processed.add(type)
            return
        }

        type.generalization.forEach { supertype ->
            if (supertype !is Unresolved) process(supertype)
        }
        type.addInheritedFeaturesFromGeneral()
        processed.add(type)
    }

    session.get().filterIsInstance<Type>().forEach { process(it) }
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

    // For each supertype, determine the features that are not re-defined; they are cloned.
    val toBeCloned = generalization.flatMap { supertype ->
        supertype.visibleMemberships().map { it.memberElement }.filterIsInstance<Feature>().filter {
            it.escapedName() !in redefinedNames
        }
    }

    // For redefinitions, replace type, multiplicity, constraints from redefinition
    redefinitions.forEach { feature ->
        // If redefining feature is unresolve, resolve it first
        if(feature.redefining is Unresolved) {
            generalization.mapNotNull { supertype ->
                supertype.resolve((feature.redefining as Unresolved).relativeName!!)
            }.lastOrNull {
                it.memberElement !== feature && it.memberElement.owner !== this
            }?.let {
                val redefined = it.memberElement as Feature
                if (redefined !== feature) {
                    feature.ownedRelationship.filterIsInstance<Redefinition>().firstOrNull()?.redefinedFeature = redefined
                }
            }
        }

        val redefiningExpr = feature.redefining?.expression
        val isBySpecializations = redefiningExpr?.trimStart()?.startsWith("bySpecializations(") == true

        val hasIdenticalExpression = when {
            redefiningExpr?.isNotBlank() == true && feature.expression?.isNotBlank() == true -> {
                redefiningExpr.trim() == feature.expression?.trim()
            }
            redefiningExpr?.isBlank() != false && feature.expression?.isBlank() != false -> true
            else -> false
        }

        if (redefiningExpr?.isNotBlank() == true && feature.redefining?.isDefaultValue == false && !isBySpecializations && !hasIdenticalExpression) {
            if (feature.expression?.isNotBlank() == true) {
                model?.status?.error("Cannot override a non-default feature value. Redefined: ${feature.redefining?.qualifiedName} ('${feature.redefining?.expression}'), Feature: ${feature.qualifiedName} ('${feature.expression}')", element = feature)
            }
        }

        // Clone the features from redefined class, except multiplicity and ValueDomain (range, unit)
        // Clone stops cloning if in the model a feature already exists.
        feature.redefining!!.features().filter { it !is Multiplicity && it.name != "range" && it.name != "unit" }.forEach {
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
            (feature.redefining!!.resolveLocal("range")!!.memberElement as? Feature)?.deepCloneWithInheritedFeature(feature)
        }

        if (feature.redefining?.resolveLocal("unit") != null && feature.resolveLocal("unit") == null) {
            (feature.redefining!!.resolveLocal("unit")!!.memberElement as? Feature)?.deepCloneWithInheritedFeature(feature)
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

    val existingFeatures = getOwnedElementsOfType<Feature>().associateBy { Identification(it) }

    // For Supertype-Features that are not re-defined, create a clone
    toBeCloned.forEach { superTypeFeature ->

        // Cyclic dependencies are ok, but we must not clone them recursively ...
        if (this.owner in superTypeFeature.allSupertypes(true))
            return

        val existingFeature = existingFeatures[Identification(superTypeFeature)]

        // If feature with same identification does not exist, add a clone from general
        if(existingFeature === null) {
            // Simple inheritance  -- we just clone it
            superTypeFeature.deepCloneWithInheritedFeature(this)
        } else {
            if (existingFeature.multiplicity() == null && superTypeFeature.multiplicity() != null) {
                model?.addOwnedMember(superTypeFeature.multiplicity()!!.clone(), existingFeature)
            }

            if (existingFeature.type.isEmpty() && superTypeFeature.type.isNotEmpty()) {
                superTypeFeature.type.forEach { type ->
                    val typing = FeatureTypingImplementation(
                        typedFeature = existingFeature,
                        type = type
                    ).also { it.isImplied }
                    model!!.addOwnedRelationship(typing, existingFeature)
                }
            }

            val superRange = superTypeFeature.resolveLocal("range")
            val existingRange = existingFeature.resolveLocal("range")
            if (superRange != null && existingRange == null && existingFeature.expression == null) {
                (superRange.memberElement as? Feature)?.deepCloneWithInheritedFeature(existingFeature)
            }
        }
    }
}