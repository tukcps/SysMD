package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType.Unknown
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.session.Session


/**
 * The data model of a variable, for collecting and exchanging variables.
 * The data class can be used as parameter to the constructor of a variable.
 * @param path the path to the feature that is implemented by the variable.
 * The feature may be only an inherited one that not necessarily exists in memory.
 * @param type the base type of the variable, i.e., Int, Bool, Real, String.
 * @param value a constraint for the domain that the value can take.
 * @param unit the unit of the quantity, if it is a quantity.
 * @param domain the domain of the quantity, if it is a quantity.
 * @param expression an operation or expression that, after evaluation, produces the value.
 */
data class VariableData(
    val path: String,
    val type: BaseType,
    val value: List<String>?=null,
    val unit: String?=null,
    val domain: String?=null,
    val expression: String?=null
)


/**
 * Function that checks the type and translates it, if possible, to one of the base types.
 */
fun Type.toBaseType(): BaseType = when {
    this is Multiplicity -> BaseType.Int
    this.specializes(this.model!!.repo.integerType) -> BaseType.Int
    this.specializes(this.model!!.repo.booleanType) -> BaseType.Bool
    this.specializes(this.model!!.repo.realType) -> BaseType.Real
    this.specializes(this.model!!.repo.stringType) -> BaseType.String
    else -> Unknown
}

/**
 * Function that gets the unit of a feature if it is a quantity.
 */
fun Feature.getUnit(): String? {
    if (this.specializes(this.model!!.repo.quantity)) {
        val unit = resolveLocal("unit")?.member<Feature>()?.expression?.trim('"', ' ')?:""
        return unit
    }
    return null
}

fun Feature.getRange(): List<String>? {
    return when {
        this is Invariant -> { mutableListOf(if (isNegated) "False" else "True") }
        typeConstraint.isNotEmpty() -> { typeConstraint.map {it} }
        this.specializes(this.model!!.repo.range) -> {
            val rangeExpr = resolveLocal("range")?.member<Feature>()?.expression?.trim('"', ' ') ?: ""
            // Check if this is a 3D vector type and range expression is empty
            if (rangeExpr.isEmpty() && type.any { it.qualifiedName?.contains("3dVector") == true }) {
                listOf("", "", "") // in this case the Domain is a 3D vector and should contain 3 values
            } else {
                rangeExpr.split(",").map { it.trim() } // Split into Components for Vectors
            }
        }
        this.specializes(this.model!!.repo.booleanType) -> { listOf(resolveLocal("range")?.member<Feature>()?.expression?.trim('"', ' ') ?: "") }
        else -> { null }
    }
}

fun Feature.getDomain(): String {
    var unitDomain = type.firstOrNull { it.owner?.declaredName == "ISQ" || it.owner?.declaredName == "Quantities"
            || it.owner?.declaredName == "Ranges"  || it.owner?.declaredName == "ScalarValues"}?.qualifiedName  ?: type.first().qualifiedName!!
    unitDomain = unitDomain.replace("ISQ::","").replace("Quantity::","")
        .replace("Ranges::","").replace("ScalarValues::","")
//    if(!unitDomain.contains("Vector") )  && values.size>1)
//        throw UnitDomainError("Vectors only allowed with type a type extended from Quantities::VectorQuantityValue")
    return unitDomain
}

fun pathOrNameSegment(membership: Membership): String {
    if (membership.memberElement.escapedName() != null)
        return "::${membership.memberElement.escapedName()}"
    else {
        val index = membership.owningNamespace?.membership?.indexOfFirst { it == membership }
        return "/$index/0"
    }
}


/**
 * Gets the Variable data of a membership(if it is a feature) in a Namespace.
 * @param namespace Qualified name of a namespace
 * @param membership the membership
 * @return a list with the Variable Data of the variables needed by the solver, determined by recursive depth-search
 */
fun getVariableInfo(namespace: QualifiedName, membership: Membership): List<VariableData> {
    val element = membership.memberElement
    val elementPath = namespace + pathOrNameSegment(membership)
    val result = mutableListOf<VariableData>()

    if (element.owner is Type && (element.owner as Type).specializes(element.model!!.repo.inRangeType))
        return result

    if (element is Feature &&
        ( element.specializes(element.model!!.repo.booleanType)
                || element.specializes(element.model!!.repo.realType)
                || element.specializes(element.model!!.repo.integerType)
                || element.specializes(element.model!!.repo.stringType)
                )
    ) {
        result.add(VariableData(
            path = elementPath,
            type = element.toBaseType(),
            value = element.getRange(),
            unit = element.getUnit(),
            domain = element.getDomain(),
            expression = element.expression)
        )
    }

    if (element is Namespace) {
        element.ownedMembership.forEach {
            val vars = getVariableInfo(elementPath, it)
            result.addAll(vars)
        }
    }
    // following piece is needed in branches where we have fully compliant membership implementation.
    /* if (element is Type)
        element.inheritedMembership.forEach {
            val vars = getVariableInfo(elementPath, it)
            result.addAll(vars)
        } */
    return result
}

fun getVariableInfo(model: Session): List<VariableData> {
    val result = mutableListOf<VariableData>()
    model.global.ownedMembership.forEach {
        result.addAll(getVariableInfo("", it))
    }
    return result
}