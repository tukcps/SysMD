package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassActions
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

class ActionDefinitionActions<T: Class>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<QualifiedName> = mutableListOf(),
): ClassActions<T>(
    context = context,
    creator = creator,
    specializes = specializes,
)