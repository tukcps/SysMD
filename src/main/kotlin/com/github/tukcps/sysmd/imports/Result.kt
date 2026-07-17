package com.github.tukcps.sysmd.imports

import kotlinx.serialization.Serializable

/**
 * Result of the characterization as a summary.
 */
@Serializable
data class Result(
    val constraintName : String,
    val resultValue: Double,
    val resultUnit: String,
    val referenceValue : Double,
    val referenceUnit : String,
    val successful : Boolean,
    val attributeQualifiedName: String,
)
