package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression

interface TerminateActionUsage : ActionUsage {
    val terminatedOccurrenceArgument: Expression?
}
