package com.github.tukcps.sysmd.services.inheritance

import com.github.tukcps.sysmd.exceptions.CyclicDependency
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.reportInfo

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
        model!!.create(SpecializationImplementation(this, model!!.any), this)
        model!!.reportInfo(this, "'${this.qualifiedName}': specialization object missing; added it.")
    }

    // Unresolved supertypes must be resolved
    generalization.forEach { general ->
        if (general.ref == null && !isStandard) {
            general.ref = model!!.any
            model!!.reportInfo(this, "'${this.qualifiedName}': Could not resolve type '${general.str}'")
        } else if ( general.str != null && general.ref == model!!.any && general.str !in setOf("Anything", "Base::Anything")) {
            model!!.reportInfo(this, "'${this.qualifiedName}': Could not resolve type '${general.str}'")
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
            general.ref = model!!.any
            model!!.report(this, "Type ${general.str} of '${qualifiedName} cannot be itself; replaced by Anything.")
        }

        if (general.ref?.isCyclic() == true) {
            val cyclic = general.str
            general.str = "Base::Anything"
            general.ref = model!!.any
            model!!.report(CyclicDependency(message = "Cyclic definition: '$cyclic' cannot be type of '${qualifiedName}'; replacing type with Base::Anything", this))
        }
    }
}

/**
 * If a feature is typed by its owner, the feature recursively must feature itself.
 * This is checked in this function.
 */
fun Feature.checkIsNotTypedByOwner() {
    if (owner.ref in allSupertypes(true)) {
        throw CyclicDependency("Cyclic dependency: a feature $qualifiedName may not be typed by its owner.", this)
    }
}