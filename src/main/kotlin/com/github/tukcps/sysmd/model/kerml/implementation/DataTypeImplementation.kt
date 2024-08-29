package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*

open class DataTypeImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "DataType"
): DataType, ClassifierImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    ownedElements=ownedElement,
    owner=owner,
    elementType=elementType
) {
    override fun toString(): String {
        return "$elementType {" +
                (if (declaredName != null) "name='$declaredName', " else "") +
                (if (declaredShortName != null) "name='$declaredShortName', " else "") +
                ("supertypes='${allSupertypes()}', ") +
                ("imports='$imports', ") +
                ("id='${elementId}...'}")
    }

    override fun clone(): DataType {
        return DataTypeImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            ownedElement=Resolved.copyOfIdentityList(ownedElement),
            owner=Resolved(owner)
        ).also {
            it.model = model
            it.updated = updated
        }
    }
}