package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ReferenceUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): ReferenceUsage, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName =declaredName,
    declaredShortName =declaredShortName,
){
    override fun clone(): ReferenceUsage = ReferenceUsageImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { updateFrom(this) }
}
