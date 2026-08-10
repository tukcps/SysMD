package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConnectorAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName

class InterfaceUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.InterfaceUsage,
    isImplicit: QualifiedName = "Interfaces::Interface",
): ConnectorAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)