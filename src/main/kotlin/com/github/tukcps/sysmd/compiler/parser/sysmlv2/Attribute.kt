@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.ATTRIBUTE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AttributeDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AttributeUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.AttributeDefinitionImplementation

/**
 * 8.2.2.7 Attributes Textual Notation
 *
 *       AttributeDefinition = DefinitionPrefix 'attribute' 'def' Definition
 *
 *       AttributeUsage = UsagePrefix 'attribute' Usage
 */

/**
 *       AttributeUsage = UsagePrefix 'attribute' Usage
 */
fun SysMLv2.AttributeDefinition() {
    val attributeDefinition = AttributeDefinitionActions<AttributeDefinition>(this.semantics, ::AttributeDefinitionImplementation)
    ATTRIBUTE.consume()
    DEF.consume()
    DefinitionDeclaration(attributeDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(attributeDefinition.created!!))
    attributeDefinition.finish()
}



/**
 *      AttributeUsage = UsagePrefix 'attribute' Usage
 */
fun SysMLv2.AttributeUsage() {
    val attribute = AttributeUsageActions(this.semantics)
    ATTRIBUTE.consume()
    Usage(attribute as FeatureActions<Feature>)
    attribute.finish()
}