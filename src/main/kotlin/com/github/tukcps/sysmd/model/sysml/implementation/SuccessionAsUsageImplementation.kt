package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ConnectorImplementation
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.util.SimpleName

open class SuccessionAsUsageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "SuccessionAsUsage"
) : SuccessionAsUsage, ConnectorImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): SuccessionAsUsage = SuccessionAsUsageImplementation()
        .also { klon -> klon.updateFrom(this) }
}