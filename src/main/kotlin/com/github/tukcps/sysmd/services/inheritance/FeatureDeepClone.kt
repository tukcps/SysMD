package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Specialization

/**
 * For inheritance: This function creates a deep copy of a feature.
 * The cloned feature, including its Multiplicity, Specialization and its owned features,
 * will be marked as a transient element and added to the namespace given as parameter.
 * @param addTo namespace, to which the cloned elements will be added.
 */
fun Feature.deepCloneWithInheritedFeature(addTo: Namespace): Feature {
    val klon = clone()
    klon.model = model
    klon.isImpliedIncluded = true

    val createdKlon = model!!.addOwnedMember(klon, addTo)

    if(klon == createdKlon) {
        ownedElement.forEach {
            when (it) {
                is Expression if klon is Expression -> {} // expressions already perform deep clone by default
                is Multiplicity -> model?.addOwnedMember(it.clone(), createdKlon)
                is Feature if (!it.isDerived) -> it.deepCloneWithInheritedFeature(createdKlon)
                is Specialization -> model?.addOwnedRelationship(it.clone(), createdKlon)
            }
        }
    }
    return createdKlon
}