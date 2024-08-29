@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceUsageImplementation
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.ElementList
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*


/**
 * 8.2.2.13.1 Connection Definition and Usage
 *
 * ConnectionDefinition = OccurrenceDefinitionPrefix 'connection' 'def' Definition
 */
fun KerML.ConnectionDefinition() {
    val connectionDefinition = sysMLSemantics.ConnectionDefinitionSemantics()
    CONNECTION.consume()
    DEF.consume()
    // Definition ...
    Identification().also { connectionDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { connectionDefinition.specialization = it }
    }
    connectionDefinition.create()
    ConnectionBody(Resolved())
}


/**
 *
 * ConnectionUsage = OccurrenceUsagePrefix
 *      ( 'connection' UsageDeclaration ValuePart? ( 'connect' ConnectorPart )?
 *      | 'connect' ConnectorPart )
 *      UsageBody
 *
 * ConnectorPart : ConnectionUsage =
 *      BinaryConnectorPart | NaryConnectorPart
 *
 * BinaryConnectorPart : ConnectionUsage =
 *      ownedRelationship += ConnectorEndMember 'to'
 *      ownedRelationship += ConnectorEndMember
 *
 * NaryConnectorPart : ConnectionUsage =
 *      '(' ownedRelationship += ConnectorEndMember ',' ownedRelationship += ConnectorEndMember
 *      ( ',' ownedRelationship += ConnectorEndMember )* ')'
 *
 * ConnectorEndMember : EndFeatureMembership :
 *      ownedRelatedElement += ConnectorEnd
 *
 * ConnectorEnd : ReferenceUsage =
 *      ( declaredName = NAME REFERENCES )?
 *      ownedRelationship += OwnedReferenceSubsetting
 *      ( ownedRelationship += OwnedMultiplicity )?
 */
fun KerML.ConnectionUsage() {
    val connection = sysMLSemantics.ConnectionUsageSemantics()
    optional(CONNECTION) {
        CONNECTION.consume()
        Identification().also { connection.identification = it }
        optional(DP) {
            DP.consume()
            QualifiedName().also { connection.association = it }
        }
    }
    CONNECT.consume()

    alternatives {
        // BinaryConnectorPart
        NAME_LIT starts {
            QualifiedName().also { connection.source += it }
            TO.consume()
            QualifiedName().also { connection.target += it }
        }

        // NaryConnectorPart
        LBRACE starts {
            LBRACE.consume()
            QualifiedName().also { connection.source += it }
            COMMA.consume()
            QualifiedName().also { connection.source += it }
            noOrMore(start = COMMA) {
                COMMA.consume()
                QualifiedName().also { connection.source += it }
            }
            RBRACE.consume()
        }
    }
    connection.create()
    ConnectionBody(Resolved(ref=connection.created!!))
}


fun KerML.InterfaceDefinition() {
    val interfaceDefinition = sysMLSemantics.ConnectionDefinitionSemantics(creator = ::InterfaceDefinitionImplementation, specialization = "Interfaces::Interface")
    INTERFACE.consume()
    DEF.consume()
    Identification().also { interfaceDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { interfaceDefinition.specialization = it }
    }
    interfaceDefinition.create()
    ConnectionBody(Resolved())
}

fun KerML.InterfaceUsage() {
    val connection = sysMLSemantics.ConnectionUsageSemantics(creator=::InterfaceUsageImplementation, association = "Interfaces::Interface")
    INTERFACE.consume()
    alternatives {
        NAME_LIT then TO starts { }
        NAME_LIT then DPDP starts { }
        others {
            Identification().also { connection.identification = it }
            optional(DP) {
                DP.consume()
                QualifiedName().also { connection.association = it }
            }
            CONNECT.consume()
        }
    }

    alternatives {
        NAME_LIT starts {
            QualifiedName().also { connection.source += it }
            TO.consume()
            QualifiedName().also { connection.target += it }
        }

        LBRACE starts {
            LBRACE.consume()
            QualifiedNameList().also { connection.source += it }
            RBRACE.consume()
        }
    }
    connection.create()
    ConnectionBody(Resolved())
}


fun KerML.ConnectionBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            ElementList()
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
        DOT then { }
    }
}