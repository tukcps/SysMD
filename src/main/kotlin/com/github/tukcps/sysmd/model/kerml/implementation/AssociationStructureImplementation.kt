package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AssociationStructure
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.util.SimpleName

class AssociationStructureImplementation(
    declaredName: SimpleName?,
    declaredShortName: SimpleName?,
    elementType: String = "AssociationStructure",
): AssociationStructure, Class, AssociationImplementation(
    declaredName, declaredShortName, elementType=elementType
) {
    override var isImplied: Boolean = false
    override fun clone() = AssociationStructureImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { it.model = model }
}