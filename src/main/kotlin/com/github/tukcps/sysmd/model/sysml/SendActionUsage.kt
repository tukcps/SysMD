package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression

interface SendActionUsage : ActionUsage {
    val payloadArgument: Expression
    val receiverArgument: Expression?
    val senderArgument: Expression?
}
