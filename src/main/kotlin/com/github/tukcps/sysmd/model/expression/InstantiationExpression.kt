package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.*

interface InstantiationExpression: Expression, Type {
    val argument: List<Expression>

    /** Name of the invoked function */
    var functionName : QualifiedName?

    override fun clone(): InstantiationExpression
}