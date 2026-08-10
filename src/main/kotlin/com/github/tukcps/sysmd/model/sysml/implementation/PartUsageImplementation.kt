package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.IntegerRange
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
    override var multiplicityRange: MultiplicityRange
        get() = MultiplicityRange(getOwnedElementOfType<Multiplicity>()?.getOwnedElementOfType<Feature>()?.expression?: "1..1")
        set(value) { multiplicity()?.variable?.intSpecs = mutableListOf(IntegerRange(value.toLongRange())) }


    override fun clone(): PartUsage = PartUsageImplementation(
        model, declaredName = declaredName, declaredShortName = declaredShortName,
    ).also { klon ->
        klon.updateFrom(this)
    }
}