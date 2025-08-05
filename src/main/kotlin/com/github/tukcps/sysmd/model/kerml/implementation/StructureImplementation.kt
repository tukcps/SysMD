package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.model.util.SimpleName

/**
 * A Class that is an occurrence
 */
open class StructureImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Structure",
): Structure, ClassImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType=elementType
) {

    override fun clone(): Structure {
        return StructureImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            elementType = elementType
        ).also {
            it.updateFrom(this)
        }
    }
}