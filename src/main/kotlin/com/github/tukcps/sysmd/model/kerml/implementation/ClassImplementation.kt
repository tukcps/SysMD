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

    override fun clone(): Class {
        return ClassImplementation().also {
            it.updateFrom(this)
        }
    }
}