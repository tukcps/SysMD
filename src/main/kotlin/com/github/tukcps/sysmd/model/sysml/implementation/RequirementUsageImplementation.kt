package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * A requirement as defined in SysML v2 metamodel
 */
open class RequirementUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): RequirementUsage, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone() = RequirementUsageImplementation(model).also { it.updateFrom(this) }
}