@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type

/**
 * 8.2.2.11 Parts Textual Notation
 *
 *      PartDefinition = OccurrenceDefinitionPrefix 'part' 'def' Definition
 */
fun SysMLv2.PartDefinition() {
    val partDefinition = PartDefinitionActions(semantics)
    PART.consume()
    DEF.consume()
    Definition(partDefinition as TypeActions<Type>)
    partDefinition.finish()
}

/**
 *      PartUsage = * OccurrenceUsagePrefix 'part' Usage
 */
fun SysMLv2.PartUsage() {
    val partUsage = PartUsageActions(semantics)
    PART.consume()
    Usage(partUsage as FeatureActions<Feature>)
    partUsage.finish()
}