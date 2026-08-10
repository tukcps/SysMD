@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.ConnectorEnd
import com.github.tukcps.sysmd.compiler.parser.kerml.ValuePart
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionUsageAction
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * 8.2.2.13.1 Connection Definition and Usage
 *
 *      ConnectionDefinition = OccurrenceDefinitionPrefix 'connection' 'def' Definition
 */
fun SysMLv2.ConnectionDefinition() = ConnectionDefinitionAction(semantics, ElementType.ConnectionDefinition).parse {
    CONNECTION.consume()
    DEF.consume()
    Definition()
}

/**
 *
 *      ConnectionUsage = OccurrenceUsagePrefix
 *          (  'connection' UsageDeclaration ValuePart? ( 'connect' ConnectorPart )?
 *            | 'connect' ConnectorPart
 *          )
 *          UsageBody
 */
fun SysMLv2.ConnectionUsage() = ConnectionUsageAction(semantics, ElementType.ConnectionUsage).parse {
    alternatives {
        CONNECTION starts {
            CONNECTION.consume()
            UsageDeclaration()
            optional(EQ or DPEQ or DEFAULT) {
                ValuePart()
            }
            optional(CONNECT) {
                CONNECT.consume()
                ConnectorPart()
            }
        }
        CONNECT starts {
            CONNECT.consume()
            ConnectorPart()
        }
    }
    UsageBody()
}
val connectionUsageStart = setOf(CONNECT, CONNECTION)


/**
 * ConnectorPart = BinaryConnectorPart | NaryConnectorPart
 */
fun SysMLv2.ConnectorPart() {
    when(token.kind) {
        in binaryConnectorPartStart -> BinaryConnectorPart()
        in naryConnectorPartStart   -> NaryConnectorPart()
        else -> throwSyntaxError("Expected a binary or n-ary connector part")
    }
}
val connectorPartStart get() = binaryConnectorPartStart + naryConnectorPartStart


/**
 *      BinaryConnectorPart = ConnectorEndMember 'to' ConnectorEndMember
 */
fun SysMLv2.BinaryConnectorPart() {
    ConnectorEnd()        .also { semantics.setSourceEnd(it) }
    TO.consume()
    ConnectorEnd()        .also { semantics.setTargetEnd(it) }
}
val binaryConnectorPartStart = setOf(NAME_LIT)


/**
 *      NaryConnectorPart = '(' ConnectorEndMember ',' ConnectorEndMember (',' ConnectorEndMember)* ')'
 */
fun SysMLv2.NaryConnectorPart() {
    LBRACE.consume()
    ConnectorEnd()    .also { semantics.setTargetEnd(it) }
    noOrMore(COMMA) {
        COMMA.consume()
        ConnectorEnd().also { semantics.addTargetEnd(it) }
    }
    RBRACE.consume()
}
val naryConnectorPartStart = setOf(LBRACE)


/**
 *      BindingConnectorAsUsage =
 *          UsagePrefix ( 'binding' UsageDeclaration )?
 *          'bind' ownedRelationship += ConnectorEndMember
 *          '=' ownedRelationship += ConnectorEndMember
 *          UsageBody
 */
fun SysMLv2.BindingConnectorAsUsage() {
    // UsagePrefix()
    BINDING.optional { UsageDeclaration() }
    BIND.consume()
    ConnectorEnd()
    EQ.consume()
    ConnectorEnd()
    UsageBody()
}
fun SysMLv2.BindingConnectorAsUsageStarts(): Boolean =
    BIND.starts() || BINDING.starts()