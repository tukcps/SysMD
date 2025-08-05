package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.DataType

open class DataTypeImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "DataType"
): DataType, ClassifierImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun clone(): DataType = DataTypeImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
        ).also { it.updateFrom(this) }
}