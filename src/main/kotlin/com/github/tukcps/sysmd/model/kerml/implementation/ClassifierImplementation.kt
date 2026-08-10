package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ClassifierImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): Classifier, TypeImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName = declaredShortName,
) {

    override fun clone(): Classifier = ClassifierImplementation(model).also { it.updateFrom(this) }
}
