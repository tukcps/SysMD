@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PART
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartUsageActions

/**
 * 8.2.2.11 Parts Textual Notation
 *
 *      PartDefinition = OccurrenceDefinitionPrefix 'part' 'def' Definition
 */
fun SysMLv2.PartDefinition() = PartDefinitionActions(semantics).parse {
    PART.consume()
    DEF.consume()
    Definition()
}

/**
 *      PartUsage = * OccurrenceUsagePrefix 'part' Usage
 */
fun SysMLv2.PartUsage() = PartUsageActions(semantics).parse {
    PART.consume()
    Usage()
}