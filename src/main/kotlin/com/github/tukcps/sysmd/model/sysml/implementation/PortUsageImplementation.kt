package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.PortUsage
import com.github.tukcps.sysmd.model.util.DEFAULT_USAGE_MULTIPLICITY
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.integer.IntegerRange
import kotlin.uuid.Uuid

class PortUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): PortUsage, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override val defaultMultiplicityRange: IntegerRange = DEFAULT_USAGE_MULTIPLICITY

    override fun clone() = PortUsageImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { klon ->
        klon.updateFrom(this)
        klon.updated = updated
        klon.isComposite = isComposite
    }
}