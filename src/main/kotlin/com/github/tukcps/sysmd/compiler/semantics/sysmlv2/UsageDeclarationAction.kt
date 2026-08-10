package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.MultiplicityAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeAction

/**
 * Standalone, does not inherit additional behavior.
 */
class UsageDeclarationAction(
    var context: ActionsContext,
) {

    fun addDefaultMultiplicity() {
        if (!(context.action as TypeAction).multiplicityAdded) {
            MultiplicityAction(context).run {
                parse({})
            }
            (context.action as TypeAction).multiplicityAdded = true
        }
    }

    fun parse( production: UsageDeclarationAction.() -> Unit ) {
        production()
    }
}