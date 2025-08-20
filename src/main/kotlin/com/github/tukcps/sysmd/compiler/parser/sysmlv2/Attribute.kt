@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.ATTRIBUTE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AttributeDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.AttributeUsageActions
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.AttributeDefinitionImplementation

/**
 * 8.2.2.7 Attributes Textual Notation
 *
 *       AttributeDefinition = DefinitionPrefix 'attribute' 'def' Definition
 */
fun SysMLv2.AttributeDefinition() = AttributeDefinitionActions(this.semantics, ::AttributeDefinitionImplementation).parse {
    ATTRIBUTE.consume()
    DEF.consume()
    DefinitionDeclaration()
    DefinitionBody()
}



/**
 *      AttributeUsage = UsagePrefix 'attribute' Usage
 */
fun SysMLv2.AttributeUsage() = AttributeUsageActions(this.semantics).parse {
    ATTRIBUTE.consume()
    Usage()
}