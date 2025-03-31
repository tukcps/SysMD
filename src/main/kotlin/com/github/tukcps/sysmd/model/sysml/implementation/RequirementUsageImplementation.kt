package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import java.util.UUID

/**
 * A requirement as defined in SysML v2 metamodel
 */
class RequirementUsageImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
): RequirementUsage, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = "RequirementUsage",
) {
    override fun clone() = RequirementUsageImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName).also {
        it.model = model
    }
}