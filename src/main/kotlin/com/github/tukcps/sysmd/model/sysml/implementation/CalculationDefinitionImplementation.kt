package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.sysml.CalculationDefinition
import java.util.*

class CalculationDefinitionImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "CalculationDefinition"
): CalculationDefinition, AssociationImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
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
            elementType = elementType
        ).also {
            it.model = model
            it.updated = updated
        }
}