package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.parseIntegerRange
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.integer.IntegerRange
import kotlin.uuid.Uuid

open class PartUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): PartUsage, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName =declaredName,
    declaredShortName =declaredShortName
) {
    /**
     * Getter and setter for the specified multiplicity.
     * Setter only works for solver ... TODO!
     *  - should be only for model, separated approach for solver needed.
     */
    override var multiplicityRange: IntegerRange
        get() = parseIntegerRange(getOwnedElementOfType<Multiplicity>()?.getOwnedElementOfType<Feature>()?.expression?: "1..1")
        set(value) { multiplicity()?.variable?.intSpecs = mutableListOf(value) }

    override fun clone(): PartUsage = PartUsageImplementation(
        model, declaredName = declaredName, declaredShortName = declaredShortName,
    ).also { klon ->
        klon.updateFrom(this)
    }
}