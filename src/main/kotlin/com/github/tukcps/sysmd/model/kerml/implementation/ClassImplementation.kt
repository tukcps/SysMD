package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ClassImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): Class, ClassifierImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
) {

    override fun clone(): Class = ClassImplementation(model).also {
        it.updateFrom(this)
    }
}