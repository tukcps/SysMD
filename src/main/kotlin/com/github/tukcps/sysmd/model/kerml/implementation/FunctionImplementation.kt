package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Function

open class FunctionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Function"
): Function, AssociationImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {
    override fun toString(): String {
        return "$elementType {" +
                (if (declaredName != null) "name='$declaredName', " else "") +
                (if (declaredShortName != null) "shortName='$declaredShortName', " else "") +
                "supertype='$generalization', " +
                "imports='$imports', " +
                "id='${elementId}...'}"
    }

    override fun clone(): FunctionImplementation {
        return FunctionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}