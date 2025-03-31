package com.github.tukcps.sysmd.services.inheritance

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import java.util.*

/**
 * For inheritance: This function creates a deep copy of a feature.
 * The new feature, its Multiplicity, Specialization will be marked as a transient element.
 * @param addTo namespace, to which the cloned elements will be added.
 */
fun Feature.deepCloneWithInheritedFeature(addTo: Namespace): Feature {
    val klon = clone()
    klon.model = model
    klon.ownedElement = mutableListOf()
    klon.owner = Resolved(null, null, null)

    if (addTo.isStandard || addTo.isLibraryElement) {
        klon.elementId = Generators.nameBasedGenerator().generate("${addTo.qualifiedName}::${klon.escapedName()}")
        klon.isTransient = true
    } else {
        klon.elementId = UUID.randomUUID()
        klon.isTransient = true
    }
    val createdKlon = model!!.create(klon, addTo)

    if (klon == createdKlon) {
        ownedElement.forEach {
            when (it.ref) {
                is Multiplicity -> {
                    val multiplicity = it.ref!!.clone()
                    if (addTo.isStandard || addTo.isLibraryElement) {
                        multiplicity.elementId = Generators.nameBasedGenerator().generate("${addTo.qualifiedName}::${klon.escapedName()}::multiplicity")
                        multiplicity.isTransient = false
                    } else {
                        multiplicity.elementId = UUID.randomUUID()
                        multiplicity.isTransient = true
                    }
                    model?.create(multiplicity as Multiplicity, createdKlon)
                }
                is Feature ->
                    if (!(it.ref as Feature).isDerived)
                        model?.create((it.ref as Feature).deepCloneWithInheritedFeature(createdKlon), createdKlon)
                is Specialization -> {
                    val spec = it.ref!!.clone()
                    if (( addTo.isStandard || addTo.isLibraryElement) && (it.ref?.escapedName() != null)) {
                        spec.elementId = Generators.nameBasedGenerator().generate(it.ref?.qualifiedName)
                        spec.isTransient = false
                    } else {
                        spec.elementId = UUID.randomUUID()
                        spec.isTransient = true
                    }
                    model?.create(spec as Specialization, createdKlon)
                }
            }
        }
    }
    return createdKlon
}