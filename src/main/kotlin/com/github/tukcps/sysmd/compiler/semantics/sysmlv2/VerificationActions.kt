package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.sysml.VerificationCaseUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class VerificationCaseActions<T: VerificationCaseUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: SimpleName = "VerificationCaseUsage"
) : ActionUsageActions<T>(context, creator, defaultType)