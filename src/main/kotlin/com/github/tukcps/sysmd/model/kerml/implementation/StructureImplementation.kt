package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

/**
 * A Class that is an occurrence
 */
open class StructureImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Structure",
): Structure,
    ClassImplementation(
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

    override fun clone(): Structure {
        return StructureImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}