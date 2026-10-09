package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.quantities.VectorQuantity

/**
 * A variable of a session.
 * - `value` is the display string of the Representer, which rounds to about 5 mantissa digits
 *   (e.g., 2.000001 is shown as 2, and 0..1e-7 as 0). Clients should not parse it.
 * - `min` and `max` are the exact bounds of the value as numbers, in `unit`. For a point value, both are equal.
 *   They are null if the variable is not a scalar number (Boolean, string, vector) or if a bound is not finite.
 */
data class VariableResponse(
    var qualifiedName: String?,
    var value: String,
    var unit: String,
    var min: Double? = null,
    var max: Double? = null,
) {
    constructor(variable: Variable): this(
        variable.path,
        variable.valueStr,
        variable.vectorQuantity.unit.toString(),
        numericBound(variable.vectorQuantity) { it.getMinAsDouble() },
        numericBound(variable.vectorQuantity) { it.getMaxAsDouble() },
    )

    private companion object {
        fun numericBound(quantity: VectorQuantity, bound: (VectorQuantity) -> Double): Double? =
            if (!quantity.isScalar || !(quantity.isReal || quantity.isInt)) null
            else try {
                bound(quantity).takeIf { it.isFinite() }
            } catch (_: Exception) {
                null // not a scalar number (Boolean, string, vector)
            }
    }
}

data class VariablesResponse(
    var variables: Collection<VariableResponse> = mutableListOf()
){
    constructor(variables: List<Variable>): this(variables.map { VariableResponse(it) })
}
