@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PART
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartUsageAction

/**
 * 8.2.2.11 Parts Textual Notation
 *
 *      PartDefinition = OccurrenceDefinitionPrefix 'part' 'def' Definition
 */
fun SysMLv2.PartDefinition() = PartDefinitionAction(semantics).parse {
    PART.consume()
    DEF.consume()
    Definition()
}

/**
 *      PartUsage = * OccurrenceUsagePrefix 'part' Usage
 */
fun SysMLv2.PartUsage() = PartUsageAction(semantics).parse {
    PART.consume()
    Usage()
}