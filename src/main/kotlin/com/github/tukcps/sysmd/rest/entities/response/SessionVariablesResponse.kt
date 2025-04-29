package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.cspsolver.Variable
import java.util.UUID


data class VariableResponse(
    var elementId: UUID?,
    var qualifiedName: String?,
    var value: String,
) {
    constructor(variable: Variable): this(variable.feature.elementId, variable.feature.qualifiedName, variable.valueStr)
}

data class VariablesResponse(
    var variables: Collection<VariableResponse> = mutableListOf()
){
    constructor(variables: List<Variable>): this(variables.map { VariableResponse(it) })
}
