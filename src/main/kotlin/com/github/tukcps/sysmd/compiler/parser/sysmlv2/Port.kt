@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PORT
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PortDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PortUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type


/**
 * 8.2.2.12 Ports Textual Notation
 *
 *      PortDefinition = DefinitionPrefix 'port' 'def' Definition ConjugatedPortDefinitionMember
 *      ConjugatedPortDefinitionMember = ConjugatedPortDefinition
 *      ConjugatedPortDefinition = PortConjugation
 *      PortConjugation = {} // ??? Missing in standard ???
 *
 *      ConjugatedPortTyping = '~' originalPortDefinition = ~[QualifiedName]
 */
fun SysMLv2.PortDefinition() = PortDefinitionActions(semantics).parse {
    PORT.consume()
    DEF.consume()
    DefinitionDeclaration()
    DefinitionBody()
}

/**
 *      PortUsage = OccurrenceUsagePrefix 'port' Usage
 */
fun SysMLv2.PortUsage() = PortUsageActions(semantics).parse {
    PORT.consume()
    Usage()
}

