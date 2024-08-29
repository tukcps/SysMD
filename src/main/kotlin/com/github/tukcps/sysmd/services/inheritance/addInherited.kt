package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.services.deepCloneWithInheritedFeature
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.reportInconsistency
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.getAllOfClass


/**
 * Initializes the types (first classifiers, then features) by
 * 1) Checking that superclasses are ok and not cyclic.
 * 2) Creating a copy of the features inherited from superclasses in subclasses.
 */
fun Session.addInherited() {

    // We do checks on types and features
    val features = getAllOfClass<Feature>()
    val classifier = getAllOfClass<Classifier>().filter { it !is Anything }
    val types =  (classifier + features).filter { !it.qualifiedName.startsWith("KerML::") }

    // We do static semantic checks ...
    types.forEach { type ->
        type.checkSupertypesAreResolved()
        type.checkForCycles()
    }

    features.forEach { feature ->
        feature.checkIsNotTypedByOwner()
    }

    classifier.forEach { it.updated = false }
    any.updated = true
    var stable: Boolean
    val specializations = get().filter { it.elementType == "Specialization" }
    val updated = mutableListOf<Type>()
    do {
        stable = true
        specializations.forEach {
            try {
                if (it is Specialization && it.general.ref!!.updated && !it.specific.ref!!.updated) {
                    it.specific.ref!!.addInheritedElements()
                    it.specific.ref!!.updated = true
                    stable = false
                    updated.add(it.specific.ref!!)
                }
            } catch (error: Exception) {
                report(it, "Problem in deriving inherited features", error)
            }
        }
    } while (! stable )

    features.forEach {
        try {
            it.addInheritedElements()
        } catch (error: Exception) {
            report(it, "Error inheriting feature from typing '${it.allSupertypes()} of ${it.qualifiedName}", cause = error)
        }
    }
}


/**
 * For inherited properties that are computed, computation can -- depending on the scope --
 * lead to different results. Hence, we create a volatile, inherited property that is
 * a clone of the superclass' property.
 */
fun Type.addInheritedElements() {
    // "Clone" features of superclass iff not there!
    require(model != null)
    if (this is Feature && referencedFeature != null) return

    val superTypeFeatures: HashMap<Identification, Feature> = hashMapOf()
    generalization.forEach { general ->
        superTypeFeatures += model!!.getAllInheritedFeatures(general.ref?:model!!.any).associateBy { Identification(it) }
    }
    val localFeatures = getOwnedElementsOfType<Feature>().associateBy { Identification(it) }

    // For each inherited property, we create a clone; needed as changes in the owning class
    // to the property shall not change the original property of the superclass.
    superTypeFeatures.values.forEach { superclassFeature ->
        if (localFeatures[Identification(superclassFeature)] == null) {
            val klon = superclassFeature.deepCloneWithInheritedFeature(this)
            model!!.create(klon, this)
        } else {
            val local = localFeatures[Identification(superclassFeature)]!!
            //If the property is redefined, we need to update the type and constraints.
            if (local.isRedefined) {
                if(local.type.elementAt(0).ref!!.qualifiedName == "Base::Anything") {
                    //Go over all Types of the superclass property and add them
                    superclassFeature.type.forEach { type ->
                        //Remove old type (Base::Anything)
                        local.ownedElement.removeIf { it.ref is FeatureTypingImplementation && it.ref!!.qualifiedName == "Base::Anything" }
                        //Add all types of refined property
                        val typing = FeatureTypingImplementation(
                            owner = Resolved(ref = local),
                            typedFeature = Resolved(ref = local),
                            type = Resolved(ref = type.ref!!)
                        )
                        local.ownedElement.add(Resolved(typing))
                    }
                }
                //add constraints
                local.typeConstraint = superclassFeature.typeConstraint
                local.unitConstraint = superclassFeature.unitConstraint
                local.isSufficient = superclassFeature.isSufficient
            }
        }
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
 * @return the joined set of properties in case of inheritance, where
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
                // if (o.ofClass.str != i.ofClass.str)
                if ( o.generalization.isEmpty() ) return merged
                //    reportError(o, "INTERNAL ERROR: ${o.ofClass.str} was not resolved property.")

                if ( i in o.allSupertypes(true) )
                    reportInconsistency(o, "subclass type  of '${o.qualifiedName}' must be subclass of '${i.qualifiedName}'")

                //if(o::class != i::class )
                //    reportInconsistency(i,"Value Feature may not override Part")

                if (o.multiplicity !in i.multiplicity)
                    reportInconsistency(o, "multiplicity of subclass '${i.qualifiedName}' must be subset of superclass '${o.qualifiedName}'.")

                /*
                when (o.quantity.value) {
                    is AADD -> o.quantity = o.quantity.intersect(i.quantity)
                    is IDD  -> o.quantity = o.quantity.intersect(i.quantity)
                    is BDD  -> o.quantity = o.quantity.intersect(i.quantity)
                    is StrDD -> { } // TODO()
                } */
                overridden = true
            }
        }
        if (!overridden) {
            merged.add(i)
        }
    }
    return merged
}