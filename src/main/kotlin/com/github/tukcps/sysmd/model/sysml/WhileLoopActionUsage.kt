package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression

interface WhileLoopActionUsage : LoopActionUsage {
    val untilArgument: Expression?
    val whileArgument: Expression
}
