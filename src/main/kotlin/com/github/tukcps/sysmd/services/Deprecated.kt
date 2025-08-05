package com.github.tukcps.sysmd.services

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session


/**
 * Direct interface to the internal model:
 * some extensions and definitions for convenience.
 * AVOID ITS USE; MOSTLY INTENDED FOR TESTING.
 */
fun Session.letVar(value: Variable, dd: DD<*>): Variable { //TODO not for Vectors
    require(dd.builder === builder)

    when (dd) {
        is AADD -> value.valueSpecs = mutableListOf(dd.getRange())
        is IDD  -> value.valueSpecs = mutableListOf(dd.getRange())
        is BDD  -> value.valueSpecs = mutableListOf(dd.value)
        else -> throw SemanticError("parameter must be of subtype of DD<*>")
    }

    value.feature.typeConstraint = mutableListOf(dd.toString() )

    value.vectorQuantity = when(dd){
        is AADD ->  Quantity(dd, value.unitSpec)
        is BDD ->  Quantity(dd)
        is IDD ->  Quantity(dd)
        else -> {throw SemanticError("Unsupported type for ${value.vectorQuantity}.")
        }
    }

    //Sync with Conditions
    if (builder.conds.indexes[value.elementId.toString()] != null)
        builder.conds.x[builder.conds.indexes[value.elementId.toString()]!!] = dd

    return value
}


/**
 * Updates a value in the model.
 */
fun Session.letVar(qualifiedName: String, value: DD<*>): Variable {
    require(value.builder == builder)
    val variable = global.resolveVar(qualifiedName)
        ?: throw ElementNotFoundException(global, "Not found in scope Global:  $qualifiedName")
    return letVar(variable, value)
}


fun Session.defScalarVar(name: String, value: String, unitStr: String = "", type: String, namespace: Namespace = global) {
    val feature = FeatureImplementation(
        declaredName = name,
        unitConstraint = unitStr,
        typeConstraint = mutableListOf(value)
    )
    addOwnedMember(feature, namespace)
    addOwnedRelationship(SpecializationImplementation(feature, global.resolve<Classifier>(type)!!), feature)
}