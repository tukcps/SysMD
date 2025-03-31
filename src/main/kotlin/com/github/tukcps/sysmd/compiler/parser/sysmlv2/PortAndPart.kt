@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.PartUsageActions
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
    UsageDeclaration(portUsage as FeatureActions<Feature>)
    DefinitionBody(Resolved(portUsage.created!!))
    portUsage.finish()
}

/**
 * 8.2.2.11 Parts Textual Notation
 *
 *      PartDefinition = OccurrenceDefinitionPrefix 'part' 'def' Definition
 */
fun SysMLv2.PartDefinition() {
    val partDefinition = PartDefinitionActions(semantics)
    PART.consume()
    DEF.consume()
    DefinitionDeclaration(partDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(partDefinition.created!!))
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