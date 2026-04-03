package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.cspsolver.Variable

data class VariableResponse(
    var qualifiedName: String?,
    var value: String,
) {
    constructor(variable: Variable): this(
        variable.path,
        variable.valueStr
    )
}

data class VariablesResponse(
    var variables: Collection<VariableResponse> = mutableListOf()
){
    constructor(variables: List<Variable>): this(variables.map { VariableResponse(it) })
}
