@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.INDIVIDUAL
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation


/**
 *      IndividualUsage = BasicUsagePrefix 'individual'?
 *          UsageExtensionKeyword* Usage
 */
fun SysMLv2.IndividualUsage() {
    val individual = FeatureActions<Feature>(
        semantics,
        defaultType = mutableListOf("Occurrences::Occurrence"),
        creator = ::FeatureImplementation
    )
    INDIVIDUAL.consume()
    UsageExtensionKeyword()
    Usage(individual)
}