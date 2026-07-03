package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.VerificationCaseUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class VerificationCaseUsageActions<T: VerificationCaseUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: SimpleName = "VerificationCases::VerificationCase"
) : ActionUsageActions<T>(context, creator, defaultType)

class VerificationCaseDefinitionActions<T: ActionDefinition>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: SimpleName = "VerificationCases::VerificationCase"
) : ActionDefinitionActions<T>(context, creator, defaultType)
