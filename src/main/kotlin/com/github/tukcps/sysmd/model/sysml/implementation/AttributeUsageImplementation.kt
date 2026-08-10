package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class AttributeUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): AttributeUsage, UsageImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName
) {

    override val defaultMultiplicityRange = MultiplicityRange.USAGE_DEFAULT

    override fun clone(): AttributeUsageImplementation = AttributeUsageImplementation(
        model,
        declaredName = this.declaredName,
        declaredShortName = this.declaredShortName,
    ).also {
        it.updateFrom(this)
        it.expression = expression
    }

    override val attributeDefinition: MutableList<DataType>
        get() = TODO("Not yet implemented")
    override val isReference: Boolean
        get() = TODO("Not yet implemented")
}