package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.exceptions.CyclicDependency
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation

/**
 * Ensures that
 * - type has an owned specialization relationship; if not, a specialization relationship to Anything is added
 * - the references to generalizations have been resolved
 */
fun Type.checkSupertypesAreResolved() {
    require(model != null)

    // Just in case there is a really faulty internal data structure ...
    // NOTE: We allow that Multiplicities do not have an owned Specialization as they are Integer anyhow.
    if (ownedSpecialization.isEmpty() && !isStandard && this !is Multiplicity) {
        model!!.create(SpecializationImplementation(this, model!!.anything), this)
        model!!.status.warn(Issue.Kind.WARN, "'${this.qualifiedName}': specialization object missing; added it.", element = this)
    }

    // Unresolved supertypes must be resolved
    generalization.forEach { general ->
        if (general.str != "ScalarValues::Natural") {
            if (general.ref == null) {
                general.ref = model!!.anything
                model!!.status.warn(Issue.Kind.WARN_UNRESOLVED_TYPE,"'${this.qualifiedName}': Could not resolve type '${general.str}'", element = this)
            } else if (general.str != null && general.ref == model!!.anything && general.str !in setOf(
                    "Anything",
                    "Base::Anything"
                )
            ) {
                model!!.status.warn(Issue.Kind.WARN_UNRESOLVED_TYPE,"'${this.qualifiedName}': Could not resolve type '${general.str}'", element = this)
            }
        }
    }
}

/**
 * Detects and fixes cyclic dependencies such that analysis can continue.
 */
fun Type.checkForCycles() {
    require(model != null)

    generalization.forEach { general ->
        if (general.ref === this) {
            general.ref = model!!.anything
            model!!.status.error("Type ${general.str} of '${qualifiedName} cannot be itself; replaced by Anything.", element = this)
        }

        if (general.ref?.isCyclic() == true) {
            val cyclic = general.str
            general.str = "Base::Anything"
            general.ref = model!!.anything
            model!!.status.error(message = "Cyclic definition: '$cyclic' cannot be type of '${qualifiedName}'; replacing type with Base::Anything",
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
    if (owner.ref is Type && owner.ref in allSupertypes(true) && multiplicity.min > 0 && !isEnd && !isDerived) {
        throw CyclicDependency("Cyclic dependency: a feature $qualifiedName may not be typed by its owner and have multiplicity larger than 1.", this)
    }
}