package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.exceptions.CyclicDependency
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type


/**
 * Detects and fixes cyclic dependencies such that analysis can continue.
 */
fun Type.checkForCycles() {
    require(model != null)

    generalization.forEach { general ->
        if (general === this) {
            ownedSpecialization.forEach { it.target = mutableListOf(model!!.anything) }
            if (!isLibraryElement) {
                model!!.status.error("Type ${general.qualifiedName} of '${qualifiedName} cannot be itself; replaced by Anything.", element = this)
            }
        }

        if (general.isCyclic()) {
            ownedSpecialization.forEach { it.target = mutableListOf(model!!.anything) }
            model!!.status.error(
                message = "Cyclic definition: '$general' cannot be type of '${qualifiedName}'; replacing type with Base::Anything",
                element = this,
                cause = CyclicDependency(),
                kind = Issue.Kind.ERROR_CYCLIC_DEPENDENCY
            )
        }
    }
}

/**
 * If a feature is typed by its owner, the feature recursively must feature itself.
 * This is checked in this function.
 */
fun Feature.checkIsNotTypedByOwner() {
    if (owner is Type && owner in allSupertypes(true) && multiplicityRange.min > 0 && !isEnd && !isDerived && referencedFeature == null) {
        throw CyclicDependency("Recursive structure: $qualifiedName may not be typed by its owner and have multiplicity that may not be 0.", this)
    }
}