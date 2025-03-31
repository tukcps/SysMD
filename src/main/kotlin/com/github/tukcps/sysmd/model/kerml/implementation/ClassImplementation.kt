package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.util.SimpleName

open class ClassImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Class"
): Class, ClassifierImplementation(
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

    override fun clone(): Class {
        return ClassImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}