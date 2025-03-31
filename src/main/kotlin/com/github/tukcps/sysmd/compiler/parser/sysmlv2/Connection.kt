@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.GeneralType
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedMultiplicity
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionUsageActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.implementation.ConnectionDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ConnectionUsageImplementation

/**
 * 8.2.2.13.1 Connection Definition and Usage
 *
 *      ConnectionDefinition = OccurrenceDefinitionPrefix 'connection' 'def' Definition
 *      Definition = DefinitionDeclaration DefinitionBody
 */
fun SysMLv2.ConnectionDefinition() {
    val connectionDefinition = ConnectionDefinitionActions<ConnectionDefinition>(semantics, ::ConnectionDefinitionImplementation, mutableListOf("Connections::Connection"))
    CONNECTION.consume()
    DEF.consume()
    DefinitionDeclaration(connectionDefinition as TypeActions<Type>)
    DefinitionBody(Resolved())
    connectionDefinition.finish()
}

/**
 *
 *      ConnectionUsage = OccurrenceUsagePrefix
 *          ( 'connection' UsageDeclaration ValuePart?
 *              ( 'connect' ConnectorPart )? | 'connect' ConnectorPart )
 *          UsageBody
 */
fun SysMLv2.ConnectionUsage() {
    val connection = ConnectionUsageActions<ConnectionUsage>(this.semantics, ::ConnectionUsageImplementation, mutableListOf("Connections::Connection"))
    connection.create(Identification(null, null))
    optional(CONNECTION) {
        CONNECTION.consume()
        Identification().also { connection.setIdentification(it) }
        optional(TYPED_BY) {
            TYPED_BY.consume()
            QualifiedName().also { connection.addTyping(mutableListOf(it)) }
        }
    }
    CONNECT.consume()
    ConnectorPart(connection)
    UsageBody(Resolved(ref=connection.created!!))
    connection.finish()
}
val connectionUsageStart = setOf(CONNECT, CONNECTION)


/**
 * ConnectorPart = BinaryConnectorPart | NaryConnectorPart
 */
fun SysMLv2.ConnectorPart(connectorPart: ConnectionUsageActions<ConnectionUsage>) {
    when(token.kind) {
        in binaryConnectorPartStart -> BinaryConnectorPart(connectorPart)
        in naryConnectorPartStart   -> NaryConnectorPart(connectorPart)
        else -> throwSyntaxError("Expected a binary or n-ary connector part")
    }
}
val connectorPartStart get() = binaryConnectorPartStart + naryConnectorPartStart

/**
 *      BinaryConnectorPart = ConnectorEndMember 'to' ConnectorEndMember
 */
fun SysMLv2.BinaryConnectorPart(connectorPart: ConnectionUsageActions<ConnectionUsage>) {
    ConnectorEndMember()        .also { connectorPart.addSource(mutableListOf(it)) }
    TO.consume()
    ConnectorEndMember()        .also { connectorPart.addTarget(mutableListOf(it)) }
}
val binaryConnectorPartStart = setOf(NAME_LIT)

/**
 *      NaryConnectorPart = '(' ConnectorEndMember ',' ConnectorEndMember ( ',' ConnectorEndMember )* ')'
 */
fun SysMLv2.NaryConnectorPart(connectorPart: ConnectionUsageActions<ConnectionUsage>) {
    val sources = mutableListOf<String>()
    LBRACE.consume()
    ConnectorEndMember()    .also { sources.add(it) }
    noOrMore(COMMA) {
        COMMA.consume()
        ConnectorEndMember().also { sources.add(it) }
    }
    connectorPart.addSource(sources)
    RBRACE.consume()
}
val naryConnectorPartStart = setOf(LBRACE)

/**
 *      ConnectorEndMember = ConnectorEnd
 *      ConnectorEnd = ( declaredName = NAME REFERENCES )?
 *          OwnedReferenceSubsetting ( OwnedMultiplicity )?
 */
fun SysMLv2.ConnectorEndMember(): String {
    val result: String
    if (nextToken.kind == REFERENCES) {
        NAME_LIT.consume() // .also { model.reportInfo(semantics.namespace, "Not yet supported: named connector end member") }
        REFERENCES.consume()
    }
    OwnedReferenceSubsetting().also { result = it }
    optional(LCBRACE) { OwnedMultiplicity() }  // TODO
    return result
}

fun SysMLv2.OwnedReferenceSubsetting() = GeneralType()
