@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationUsageAction

/**
 * 8.2.2.15 Allocations Textual Notation
 *
 *      AllocationDefinition = OccurrenceDefinitionPrefix 'allocation' 'def' Definition
 */

fun SysMLv2.AllocationDefinition() = AllocationDefinitionAction(semantics).parse {
    ALLOCATION.consume()
    DEF.consume()
    DefinitionDeclaration()
    UsageBody()
}

/**
 * 8.2.2.15 Allocations Textual Notation
 *
 *      AllocationUsage = OccurrenceUsagePrefix AllocationUsageDeclaration UsageBody
 *      AllocationUsageDeclaration =
 *          'allocation' UsageDeclaration ( 'allocate' ConnectorPart )?
 *          | 'allocate' ConnectorPart
 *
 */
fun SysMLv2.AllocationUsage() = AllocationUsageAction(semantics).parse {
    alternatives {
        ALLOCATION starts {
            ALLOCATION.consume()
            UsageDeclaration()
            optional(ALLOCATE) {
                ALLOCATE.consume()
                ConnectorPart()
            }
        }
        ALLOCATE starts {
            ALLOCATE.consume()
            ConnectorPart()
        }
    }
    UsageBody()
}
val allocationUsageStart = setOf(ALLOCATION, ALLOCATE)
