@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ItemDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ItemUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type


/**
 * 8.2.2.10 Items Textual Notation
 *
 *      ItemDefinition = OccurrenceDefinitionPrefix 'item' 'def' Definition
 */
fun SysMLv2.ItemDefinition() {
    val itemDefinition = ItemDefinitionActions(semantics)
    ITEM.consume()
    DEF.consume()
    DefinitionDeclaration(itemDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(itemDefinition.created!!))
    itemDefinition.finish()
}

/**
 *      ItemUsage = OccurrenceUsagePrefix 'item' Usage
 */
fun SysMLv2.ItemUsage() {
    val itemUsage = ItemUsageActions(semantics)
    ITEM.consume()
    Usage(itemUsage as FeatureActions<Feature>)
    itemUsage.finish()
}