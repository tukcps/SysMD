package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.AssociationStructure
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class AssociationStructureImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): AssociationStructure, Class, AssociationImplementation(
    model,
    elementId = elementId, declaredName, declaredShortName
) {
    override var isImplied: Boolean = false
    override fun clone() = AssociationStructureImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    )
}