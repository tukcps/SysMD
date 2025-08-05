@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.ITEM
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ItemDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ItemUsageActions


/**
 * 8.2.2.10 Items Textual Notation
 *
 *      ItemDefinition = OccurrenceDefinitionPrefix 'item' 'def' Definition
 */
fun SysMLv2.ItemDefinition() = ItemDefinitionActions(semantics).parse {
    ITEM.consume()
    DEF.consume()
    DefinitionDeclaration()
    DefinitionBody()
}

/**
 *      ItemUsage = OccurrenceUsagePrefix 'item' Usage
 */
fun SysMLv2.ItemUsage() = ItemUsageActions(semantics).parse {
    ITEM.consume()
    Usage()
}