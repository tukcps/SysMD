package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class DataTypeImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): DataType, ClassifierImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
) {
    override fun clone(): DataType = DataTypeImplementation(
        model,
        declaredName=declaredName,
        declaredShortName=declaredShortName,
    ).also { it.updateFrom(this) }
}