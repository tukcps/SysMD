package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.SimpleName

class VerificationCaseUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.VerificationCaseUsage,
    isImplicit: SimpleName = "VerificationCases::VerificationCase"
) : ActionUsageAction(context, type, isImplicit)

class VerificationCaseDefinitionAction(
    context: ActionsContext,
    type: ElementType = ElementType.VerificationCaseDefinition,
    isImplicit: SimpleName = "VerificationCases::VerificationCase"
) : ActionDefinitionAction(context, type, isImplicit)
