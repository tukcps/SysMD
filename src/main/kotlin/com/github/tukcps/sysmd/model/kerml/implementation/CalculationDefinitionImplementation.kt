package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Calculation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*

class CalculationDefinitionImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    elementType: String = "CalculationDefinition"
): Calculation, AssociationImplementation(
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
                (if (declaredShortName != null) "shortName='$declaredShortName', " else "") +
                "supertype='$generalization', " +
                "imports='$imports', " +
                "id='${elementId}...'}"
    }

    override fun clone() = CalculationDefinitionImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
}