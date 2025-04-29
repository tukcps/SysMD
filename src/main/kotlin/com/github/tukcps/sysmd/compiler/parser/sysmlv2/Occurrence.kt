@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.OccurrenceDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.OccurrenceUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation

/**
 * In this file we collect parser production implementations for
 * - part usage and definition
 * - port usage and definition
 *
 * 8.2.2.9.1 Occurrence Definitions
 *
 *      OccurrenceDefinitionPrefix = BasicDefinitionPrefix? ( 'individual' )? DefinitionExtensionKeyword*
 *
 *      OccurrenceDefinition = OccurrenceDefinitionPrefix 'occurrence' 'def' Definition
 *
 *      IndividualDefinition = BasicDefinitionPrefix? 'individual' DefinitionExtensionKeyword* 'def' Definition
 */
fun SysMLv2.OccurrenceDefinition() {
    val occurrenceDefinition = OccurrenceDefinitionActions(semantics)
    OCCURRENCE.consume()
    DEF.consume()
    DefinitionDeclaration(occurrenceDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(occurrenceDefinition.created!!))
    occurrenceDefinition.finish()
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
fun SysMLv2.OccurrenceUsage() {
    val occurenceUsage = OccurrenceUsageActions(semantics)
    OCCURRENCE.consume()
    Usage(occurenceUsage as FeatureActions<Feature>)
    occurenceUsage.finish()
}

/**
 *      PortionUsage =
 *          BasicUsagePrefix ('individual')? PortionKind
 *          UsageExtensionKeyword* Usage
 */
fun SysMLv2.PortionUsage() {
    val portionUsage = FeatureActions<Feature>(semantics, defaultType = mutableListOf("Occurrences::Occurrence"), creator = ::FeatureImplementation)
    INDIVIDUAL.optional()
    alternatives { // PortionKind
        SNAPSHOT  starts { consume(); }
        TIMESLICE starts { consume(); }
    }
    UsageExtensionKeyword()
    Usage(portionUsage)
}

/**
 *      EventOccurrenceUsage =
 *          OccurrenceUsagePrefix 'event'
 *          ( OwnedReferenceSubsetting FeatureSpecializationPart?
 *            | 'occurrence' UsageDeclaration? )
 *          UsageCompletion
 */
fun SysMLv2.EventOccurrenceUsage() {
    val eventOccurrenceUsage = FeatureActions<Feature>(semantics, defaultType = mutableListOf("Occurrences::Occurrence"), creator = ::FeatureImplementation)
    EVENT.consume()
    alternatives {
        OCCURRENCE starts {
            OCCURRENCE.consume().also { semantics.prefixes.add(OUT) }
            UsageDeclaration(eventOccurrenceUsage)
        }
        // OwnedReferenceSubsetting()
        // FeatureSpecializationPart(eventOccurrenceUsage)
    }
    eventOccurrenceUsage.finish()
    UsageCompletion(eventOccurrenceUsage)
}