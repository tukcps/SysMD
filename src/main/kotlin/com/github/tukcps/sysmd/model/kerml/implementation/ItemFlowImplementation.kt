package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.ItemFlow
import com.github.tukcps.sysmd.model.util.SimpleName

class ItemFlowImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "ItemFlow",
): ItemFlow, ConnectorImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType,
)