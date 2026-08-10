package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class OccurrenceUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): OccurrenceUsage, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName =declaredName,
    declaredShortName =declaredShortName,
) {
    override fun clone(): OccurrenceUsage = OccurrenceUsageImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { super.updateFrom(this) }

    override val individualDefinition: OccurrenceDefinition?
        get() = TODO("Not yet implemented")
    override var isIndividual: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val occurrenceDefinition: MutableList<Class>
        get() = TODO("Not yet implemented")
    override var portionKind: OccurrenceUsage.PortionKind?
        get() = TODO("Not yet implemented")
        set(value) {}
}