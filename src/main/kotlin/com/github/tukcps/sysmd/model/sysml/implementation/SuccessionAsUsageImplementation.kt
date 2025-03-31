package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

open class SuccessionAsUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "SuccessionAsUsage"
) : SuccessionAsUsage, ConnectorImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): SuccessionAsUsage {
        return SuccessionAsUsageImplementation(
            declaredName = declaredName, declaredShortName = declaredShortName,
        ).also { klon ->
            klon.from = Resolved.copyOfIdentityList(from)
            klon.to = Resolved.copyOfIdentityList(to)
            klon.model = model
            klon.direction = direction
            klon.updated = updated
        }
    }
}