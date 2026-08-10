@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureSpecializationPart
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedReferenceSubsetting
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.OccurrenceDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.OccurrenceUsageAction
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * In this file we collect parser production implementations for
 * - part usage and definition
 * - port usage and definition
 *
 * 8.2.2.9.1 Occurrence Definitions
 *
 *      OccurrenceDefinitionPrefix = BasicDefinitionPrefix? ( 'individual' )? DefinitionExtensionKeyword*
  *     OccurrenceDefinition = OccurrenceDefinitionPrefix 'occurrence' 'def' Definition
 *
 */
fun SysMLv2.OccurrenceDefinition() = OccurrenceDefinitionAction(semantics).parse {
    INDIVIDUAL.optional()
    // DefinitionExtensionKeyword
    OCCURRENCE.consume()
    DEF.consume()
    Definition()
}

/**
 *      IndividualDefinition = BasicDefinitionPrefix? 'individual' DefinitionExtensionKeyword* 'def' Definition
 */
fun SysMLv2.IndividualDefinition() = OccurrenceDefinitionAction(semantics).parse {
    INDIVIDUAL.consume()
    // DefinitionExtensionKeyword
    DEF.consume()
    Definition()
}


/**
 * 8.2.2.9.2 Occurrence Usages
 *
 *      OccurrenceUsagePrefix = BasicUsagePrefix 'individual'?
 *          PortionKind? UsageExtensionKeyword*
 *
 *      PortionKind = 'snapshot' | 'timeslice'
 */
fun SysMLv2.OccurrenceUsagePrefix() {
    BasicUsagePrefix()
    INDIVIDUAL.optional { semantics.prefixes.add(INDIVIDUAL)}
    when(token.kind) { // PortionKind?
        SNAPSHOT ->  { consume(); semantics.prefixes.add(SNAPSHOT) }
        TIMESLICE -> { consume(); semantics.prefixes.add(TIMESLICE) }
        else -> { }
    }
}

/**
 *      OccurrenceUsage = OccurrenceUsagePrefix 'occurrence' Usage
 */
fun SysMLv2.OccurrenceUsage() = OccurrenceUsageAction(semantics).parse {
    OCCURRENCE.optional() // Optional in informal description, not optional in metamodel (Bug in Spec?)
    Usage()
}

/**
 *      PortionUsage =
 *          BasicUsagePrefix ('individual')? PortionKind
 *          UsageExtensionKeyword* Usage
 */
fun SysMLv2.PortionUsage() = FeatureAction(semantics, isImplicit = "Occurrences::Occurrence", type = ElementType.Feature).parse {
    INDIVIDUAL.optional()
    when(token.kind) { // PortionKind
        SNAPSHOT  -> { consume(); }
        TIMESLICE -> { consume(); }
        else -> { handleSyntaxError("Expected a portion kind (snapshot, timeslice)")}
    }
    UsageExtensionKeyword()
    Usage()
}

/**
 *      EventOccurrenceUsage =
 *          OccurrenceUsagePrefix 'event'
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *            | 'occurrence' UsageDeclaration? )
 *          UsageCompletion
 */
fun SysMLv2.EventOccurrenceUsage() = FeatureAction(semantics, ElementType.Feature, isImplicit = "Occurrences::Occurrence").parse {
    EVENT.consume()
    when(token.kind) {
        OCCURRENCE -> {
            OCCURRENCE.consume().also { semantics.prefixes.add(OUT) }
            UsageDeclaration()
        }
        NAME_LIT -> {
            OwnedReferenceSubsetting()
            FeatureSpecializationPart()
        }
        else -> { handleSyntaxError("Expected occurrence, reference subsetting, or feature specification")}
    }
    UsageCompletion()
}