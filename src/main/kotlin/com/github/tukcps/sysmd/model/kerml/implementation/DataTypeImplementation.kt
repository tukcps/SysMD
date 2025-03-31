package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.DataType
import java.util.*

open class DataTypeImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "DataType"
): DataType, ClassifierImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun toString(): String {
        return "$elementType {" +
                (if (declaredName != null) "name='$declaredName', " else "") +
                (if (declaredShortName != null) "name='$declaredShortName', " else "") +
                ("supertypes='${allSupertypes().map { type -> type.escapedName() }}', ") +
                ("imports='$imports', ") +
                ("id='${elementId}...'}")
    }

    override fun clone(): DataType {
        return DataTypeImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}