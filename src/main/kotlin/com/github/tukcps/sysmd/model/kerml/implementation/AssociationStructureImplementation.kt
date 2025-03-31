package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AssociationStructure
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class AssociationStructureImplementation(
    declaredName: SimpleName?,
    declaredShortName: SimpleName?,
    elementType: String = "AssociationStructure",
): AssociationStructure, StructureImplementation(
    declaredName, declaredShortName, elementType
) {
    override var isImplied: Boolean = false
    override var source: MutableList<Resolved<Element>> = mutableListOf()
    override var target: MutableList<Resolved<Element>> = mutableListOf()
    override fun clone(): AssociationStructure {
        return super.clone() as AssociationStructure
    }
}