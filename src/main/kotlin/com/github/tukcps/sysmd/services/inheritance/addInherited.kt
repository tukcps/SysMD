package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.services.session.reportInconsistency
import com.github.tukcps.sysmd.services.session.Session

/**
 * For a type, iterates overall subtypes, and adds clones of the own owned features to it.
 * Then, recursively the same for all subtypes.
 * @param calls used to recognize loops
 */
fun Type.addInheritedToSubtypes(calls: Int = 0) {
    addInheritedFeatures()
    subtypes.forEach { subtype ->
        subtype.addInheritedToSubtypes(calls + 1)
    }
}

/**
 * For inherited properties that are computed, computation can -- depending on the scope --
 * lead to different results. Hence, we create a volatile, inherited property that is
 * a clone of the superclass' property.
 */
fun Type.addInheritedFeatures() {
    // "Clone" features of superclass iff not there!
    // Type element must be part of model
    require(model != null)
    if (this is Feature && (referencedFeature != null || isDerived)) return

    // get all features of general type if that is already resolved
    val typeFeatures: MutableList<Feature> = mutableListOf()
    generalization.forEach { general ->
        typeFeatures += model!!.getAllInheritedFeatures(general.ref?: model!!.anything)
    }

    val localFeatures = getOwnedElementsOfType<Feature>().associateBy { Identification(it) }

    // For each inherited property, we create a clone; needed as changes in the owning class
    // to the property shall not change the original property of the superclass.
    typeFeatures.forEach { feature ->

        // Cyclic dependencies are ok, but we must not clone them recursively ...
        if (this.owner.ref in feature.allSupertypes(true))
            return

        // If feature with same identification does not exist, add a clone from general
        val local = localFeatures[Identification(feature)]
        if (local == null) {
            val klon = feature.deepCloneWithInheritedFeature(this)
            model!!.create(klon, this)
        } else if (local.isRedefined){
            //If the property is redefined, we need to update the type and constraints.
            // if(local.type.elementAt(0).ref!!.qualifiedName == "Base::Anything") {
                //Go over all Types of the superclass property and add them
                feature.type.forEach { type ->
                    //Remove old type (Base::Anything)
                    local.ownedElement.removeIf {
                        it.ref is FeatureTypingImplementation &&
                                (it.ref as FeatureTypingImplementation).target
                                    .firstOrNull()?.ref?.qualifiedName in listOf("Base::DataValue", "Base::Anything")
                    }
                    if (type.ref == null) {
                        type.ref = model!!.get(type.id!!) as Type?
                    }
                    //Add all types of refined property
                    val typing = FeatureTypingImplementation(
                        typedFeature = Resolved(ref = local),
                        type = Resolved(ref = type.ref!!)
                    ).also {
                        it.isTransient
                        it.isImplied
                    }
                    model!!.create(typing, local)
                }
            //}
            //add constraints
            local.typeConstraint = feature.typeConstraint
            local.unitConstraint = feature.unitConstraint
            local.isSufficient = feature.isSufficient
            if(local.name=="range" || local.name=="spec"){ //range for Integer, Real, spec for Boolean
                if(local.owner.ref is Feature) {
                    // remove """ and " " from the string
                    val specString = local.expression!!.replace("\"", "").replace(" ", "")
                    // set the type constraint to the list of specs split by "," (vectors)
                    (local.owner.ref as FeatureImplementation).typeConstraint.clear()
                    (local.owner.ref as FeatureImplementation).typeConstraint.addAll(specString.split(","))
                }
            }
            if(local.name=="unit"){
                if(local.owner.ref is Feature)
                    (local.owner.ref as FeatureImplementation).unitConstraint = local.expression!!.replace("\"","")
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