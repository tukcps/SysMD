@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AllocationUsageActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionUsageActions
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.implementation.AllocationUsageImplementation

/**
 * 8.2.2.15 Allocations Textual Notation
 *
 *      AllocationDefinition = OccurrenceDefinitionPrefix 'allocation' 'def' Definition
 */

fun SysMLv2.AllocationDefinition() {
    val allocationDefinition = AllocationDefinitionActions(semantics)
    ALLOCATION.consume()
    DEF.consume()
    DefinitionDeclaration(allocationDefinition as TypeActions<Type>)
    UsageBody(Resolved(allocationDefinition.created!!))
    allocationDefinition.finish()
}


/**
 * 8.2.2.15 Allocations Textual Notation
 *
 *      AllocationUsage = OccurrenceUsagePrefix AllocationUsageDeclaration UsageBody
 *      AllocationUsageDeclaration = 'allocation' UsageDeclaration
 *          ( 'allocate' ConnectorPart )?
 *          | 'allocate' ConnectorPart
 *
 */
fun SysMLv2.AllocationUsage() {
    val allocationUsage = AllocationUsageActions(this.semantics, ::AllocationUsageImplementation, mutableListOf("Allocations::Allocation"))
    allocationUsage.create(Identification(null, null))
    optional(ALLOCATION) {
        ALLOCATION.consume()
        Identification().also { allocationUsage.setIdentification(it) }
        optional(TYPED_BY) {
            TYPED_BY.consume()
            QualifiedNameList().also { allocationUsage.addTyping(it) }
        }
    }
    ALLOCATE.consume()
    ConnectorPart(allocationUsage as ConnectionUsageActions<ConnectionUsage>)
    UsageBody(Resolved(ref=allocationUsage.created!!))
    allocationUsage.finish()
}
val allocationUsageStart = setOf(ALLOCATION, ALLOCATE)
