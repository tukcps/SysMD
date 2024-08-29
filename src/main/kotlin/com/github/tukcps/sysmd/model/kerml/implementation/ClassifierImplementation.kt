package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*

open class ClassifierImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElements: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "Classifier"
): Classifier, TypeImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    ownedElement = ownedElements,
    owner = owner,
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
            ownedElements=Resolved.copyOfIdentityList(ownedElement),
            owner=Resolved(owner)
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}
