package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Specialization

/**
 * For inheritance: This function creates a deep copy of a feature.
 * The new feature, its Multiplicity, Specialization will be marked as a transient element.
 * @param addTo namespace, to which the cloned elements will be added.
 */
fun Feature.deepCloneWithInheritedFeature(addTo: Namespace): Feature {
    val klon = clone()
    klon.model = model
    klon.isImpliedIncluded = true

    val createdKlon = model!!.addOwnedMember(klon, addTo)

    if (klon == createdKlon) {
        ownedElement.forEach {
            when (it) {
                is Multiplicity -> model?.addOwnedMember(it.clone(), createdKlon)
                is Feature if (!it.isDerived) -> model?.addOwnedMember(it.deepCloneWithInheritedFeature(createdKlon), createdKlon)
                is Specialization -> model?.addOwnedRelationship(it.clone(), createdKlon)
            }
        }
    }
    return createdKlon
}