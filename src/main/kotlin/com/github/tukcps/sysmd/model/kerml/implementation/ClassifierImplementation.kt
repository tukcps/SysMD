package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Classifier
import java.util.*

open class ClassifierImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Classifier"
): Classifier, TypeImplementation(
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun toString(): String {
        return "$elementType {" +
                (if (declaredName != null) "declaredName='$declaredName', " else "") +
                (if (declaredShortName != null) "declaredShortName='$declaredShortName', " else "") +
                "general='$generalization', " +
                "imports='$imports', " +
                "id='${elementId}...'}"
    }

    override fun clone(): Classifier {
        return ClassifierImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}
