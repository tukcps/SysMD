package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName

/**
 * The root namespace as a dedicated class, not a regular namespace.
 * Used as default start of semantic actions ownership hierarchy.
 */
class RootNamespaceAction(
    context: ActionsContext,
): NamespaceAction(
    context, ElementType.Namespace,
) {
    var qualifiedName : QualifiedName? = null
}
