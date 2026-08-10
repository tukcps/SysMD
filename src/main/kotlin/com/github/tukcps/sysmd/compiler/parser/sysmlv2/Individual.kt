@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.INDIVIDUAL
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 *      IndividualUsage = BasicUsagePrefix 'individual'?
 *          UsageExtensionKeyword* Usage
 */
fun SysMLv2.IndividualUsage() = FeatureAction(semantics, ElementType.Feature, "Occurrences::Occurrence").parse {
    INDIVIDUAL.consume()
    UsageExtensionKeyword()
    Usage()
}