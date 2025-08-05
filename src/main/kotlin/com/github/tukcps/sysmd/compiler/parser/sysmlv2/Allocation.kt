@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationUsageActions
import com.github.tukcps.sysmd.model.sysml.implementation.AllocationUsageImplementation

/**
 * 8.2.2.15 Allocations Textual Notation
 *
 *      AllocationDefinition = OccurrenceDefinitionPrefix 'allocation' 'def' Definition
 */

fun SysMLv2.AllocationDefinition() = AllocationDefinitionActions(semantics).parse {
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
fun SysMLv2.AllocationUsage() = AllocationUsageActions(semantics, ::AllocationUsageImplementation, "Allocations::Allocation").parse {
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
            semantics.create(null)
            ALLOCATE.consume()
            ConnectorPart()
        }
    }
    UsageBody()
}
val allocationUsageStart = setOf(ALLOCATION, ALLOCATE)
