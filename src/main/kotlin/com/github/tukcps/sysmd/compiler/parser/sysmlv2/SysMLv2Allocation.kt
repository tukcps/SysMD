@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.ElementList
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

/**
 * 8.2.2.15 Allocations Textual Notation
 *
 * AllocationDefinition = OccurrenceDefinitionPrefix 'allocation' 'def' Definition
 *
 * AllocationUsage = OccurrenceUsagePrefix AllocationUsageDeclaration UsageBody
 * AllocationUsageDeclaration : AllocationUsage =
 *   'allocation' UsageDeclaration ( 'allocate' ConnectorPart )?
 * | 'allocate' ConnectorPart
 */


fun KerML.AllocationDefinition() {
    val allocationDefinition = sysMLSemantics.AllocationDefinitionSemantics()
    ALLOCATION.consume()
    DEF.consume()
    // Definition
    Identification().also { allocationDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { allocationDefinition.specialization = it }
    }
    allocationDefinition.create()
    UsageBody(Resolved())
}

fun KerML.AllocationUsage() {
    val allocationUsage = sysMLSemantics.AllocationUsageSemantics()
    optional(ALLOCATION) {
        ALLOCATION.consume()
        Identification().also { allocationUsage.identification = it }
        optional(DP) {
            DP.consume()
            QualifiedName().also { allocationUsage.association = it }
        }
    }
    ALLOCATE.consume()

    alternatives {
        NAME_LIT starts {
            QualifiedName().also { allocationUsage.source += it }
            TO.consume()
            QualifiedName().also { allocationUsage.target += it }
        }

        LBRACE starts {
            LBRACE.consume()
            QualifiedNameList().also { allocationUsage.target += it }
            RBRACE.consume()
        }
    }
    allocationUsage.create()
    UsageBody(Resolved(ref=allocationUsage.created!!))
}



fun KerML.UsageBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            ElementList()
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
        DOT then { }
    }
}