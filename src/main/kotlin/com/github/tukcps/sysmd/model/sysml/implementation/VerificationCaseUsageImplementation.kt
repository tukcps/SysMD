package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.VerificationCaseUsage
import com.github.tukcps.sysmd.model.util.SimpleName

class VerificationCaseUsageImplementation(
    declaredShortName: SimpleName? ?= null,
    declaredName: SimpleName? ?= null,
    elementType: String = "ActionUsage"
): VerificationCaseUsage, ActionUsageImplementation(
    elementType = elementType
)