@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Body
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*


/**
 * In this file we collect parser production implementations for
 * - part usage and definition
 * - port usage and definition
 */


fun KerML.OccurrenceDefinition() {
    val occurrenceDefinition = sysMLSemantics.OccurrenceDefinitionSemantics()
    OCCURRENCE.consume()
    DEF.consume()
    optional(NAME_LIT) {
        Identification().also { occurrenceDefinition.identification = it }
    }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { occurrenceDefinition.specialization = it }
    }
    occurrenceDefinition.create()
    Body(Resolved(occurrenceDefinition.identification.name?:occurrenceDefinition.identification.shortName!!))
}


fun KerML.OccurrenceUsage() {
    val occurenceUsage = sysMLSemantics.OccurrenceUsageSemantics()
    // (IN or OUT).consume()
    // ParserSysMD handles prefixes.
    // semantics.prefixes holds a set of the prefix tokens that apply
    OCCURRENCE.consume()
    optional(NAME_LIT) {
        Identification().also { occurenceUsage.identification = it }
    }
    optional(DP) {
        DP.consume()
        QualifiedName().also { occurenceUsage.className = it }
    }
    occurenceUsage.create()
    Body(Resolved(occurenceUsage.identification?.name?:occurenceUsage.identification?.shortName!!))
}
