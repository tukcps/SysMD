@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PORT
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PortDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PortUsageAction


/**
 * 8.2.2.12 Ports Textual Notation
 *
 *      PortDefinition = DefinitionPrefix 'port' 'def' Definition ConjugatedPortDefinitionMember
 *
 *      ConjugatedPortDefinitionMember = ConjugatedPortDefinition
 *      ConjugatedPortDefinition = PortConjugation
 *      PortConjugation = {}
 *
 *      ConjugatedPortTyping = '~' originalPortDefinition = ~[QualifiedName]
 */
fun SysMLv2.PortDefinition() = PortDefinitionAction(semantics).parse {
    PORT.consume()
    DEF.consume()
    DefinitionDeclaration()
    DefinitionBody()
}

/**
 *      PortUsage = OccurrenceUsagePrefix 'port' Usage
 */
fun SysMLv2.PortUsage() = PortUsageAction(semantics).parse {
    PORT.consume()
    Usage()
}

