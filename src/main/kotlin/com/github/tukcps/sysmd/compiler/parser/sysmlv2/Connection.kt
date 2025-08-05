@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.ConnectorEndMember
import com.github.tukcps.sysmd.compiler.parser.kerml.ValuePart
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionUsageActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.implementation.ConnectionDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ConnectionUsageImplementation

/**
 * 8.2.2.13.1 Connection Definition and Usage
 *
 *      ConnectionDefinition = OccurrenceDefinitionPrefix 'connection' 'def' Definition
 */
fun SysMLv2.ConnectionDefinition() = ConnectionDefinitionActions(semantics, ::ConnectionDefinitionImplementation).parse {
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
fun SysMLv2.ConnectionUsage() = ConnectionUsageActions<ConnectionUsage>(semantics, ::ConnectionUsageImplementation, "Connections::Connection").parse {
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
            semantics.create(null)
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
    ConnectorEndMember()        .also { semantics.setSourceEnd(it) }
    TO.consume()
    ConnectorEndMember()        .also { semantics.setTargetEnd(it) }
}
val binaryConnectorPartStart = setOf(NAME_LIT)


/**
 *      NaryConnectorPart = '(' ConnectorEndMember ',' ConnectorEndMember (',' ConnectorEndMember)* ')'
 */
fun SysMLv2.NaryConnectorPart() {
    LBRACE.consume()
    ConnectorEndMember()    .also { semantics.setTargetEnd(it) }
    noOrMore(COMMA) {
        COMMA.consume()
        ConnectorEndMember().also { semantics.addTargetEnd(it) }
    }
    RBRACE.consume()
}
val naryConnectorPartStart = setOf(LBRACE)