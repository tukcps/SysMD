package com.github.tukcps.sysmd.services

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import java.util.*

/**
 * For inheritance: This function creates a deep copy of a feature.
 * The new feature, its Multiplicity, Specialization will be marked as a transient element.
 * @param addTo namespace, to which the cloned elements will be added.
 */
fun Feature.deepCloneWithInheritedFeature(addTo: Namespace): Feature {
    val klon = clone()  // Clone of this feature, but with new ID.
    klon.ownedElement = mutableListOf()
    klon.owner = Resolved(null, null, null)

    if (addTo.isStandard || addTo.isLibraryElement) {
        klon.elementId = Generators.nameBasedGenerator().generate("${addTo.qualifiedName}::${klon.escapedName()}")
        klon.isTransient = false
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
                is Feature -> model?.create((it.ref as Feature).deepCloneWithInheritedFeature(createdKlon), createdKlon) // .also { it!!.isTransient = true }
                is Specialization -> {
                    val spec = it.ref!!.clone()
                    if (addTo.isStandard || addTo.isLibraryElement) {
                        spec.elementId = Generators.nameBasedGenerator().generate("${addTo.qualifiedName}::${klon.escapedName()}::${spec.elementType}")
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