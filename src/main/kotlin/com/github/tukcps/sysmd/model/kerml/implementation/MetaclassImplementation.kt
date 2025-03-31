package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class MetaclassImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Metaclass",
): Metaclass, StructureImplementation(
        declaredName=declaredName,
        declaredShortName=declaredShortName,
        elementType=elementType
    ) {

    override fun clone(): MetaclassImplementation {
        return MetaclassImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}