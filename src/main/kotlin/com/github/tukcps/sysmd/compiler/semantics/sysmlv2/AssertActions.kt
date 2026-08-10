@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.model.generated.ElementType

class AssertAction(
    context: ActionsContext,
): FeatureAction(
    context = context,
    type = ElementType.Invariant,
)