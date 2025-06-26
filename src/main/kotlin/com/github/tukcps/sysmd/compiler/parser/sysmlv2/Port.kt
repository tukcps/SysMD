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
import com.github.tukcps.sysmd.model.kerml.Resolved
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
fun SysMLv2.PortDefinition() {
    val portDefinition = PortDefinitionActions(semantics)
    PORT.consume()
    DEF.consume()
    DefinitionDeclaration(portDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(portDefinition.created!!))
    portDefinition.finish()
}

/**
 *      PortUsage = OccurrenceUsagePrefix 'port' Usage
 */
fun SysMLv2.PortUsage() {
    val portUsage = PortUsageActions(semantics)
    PORT.consume()
    Usage(portUsage as FeatureActions<Feature>)
    portUsage.finish()
}

