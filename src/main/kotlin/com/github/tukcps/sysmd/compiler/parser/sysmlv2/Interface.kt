@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.CONNECT
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DPDP
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.INTERFACE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LBRACE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.NAME_LIT
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.RBRACE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.TO
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.TYPED_BY
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.InterfaceUsageActions
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceUsageImplementation


/**
 * 8.2.2.14 Interfaces Textual Notation
 * 8.2.2.14.1 Interface Definitions
 *
 *      InterfaceDefinition = OccurrenceDefinitionPrefix 'interface' 'def' DefinitionDeclaration InterfaceBody
 */
fun SysMLv2.InterfaceDefinition() {
    val interfaceDefinition = ConnectionDefinitionActions(semantics, ::InterfaceDefinitionImplementation, specializes = mutableListOf("Interfaces::Interface"))
    INTERFACE.consume()
    DEF.consume()
    DefinitionDeclaration(interfaceDefinition as TypeActions<Type>)
    DefinitionBody(Resolved(interfaceDefinition.created!!))
    interfaceDefinition.finish()
}

/**
 *
 *      InterfaceBody = ';' | '{' InterfaceBodyItem* '}'
 *
 *      InterfaceBodyItem =
 *           DefinitionMember
 *          | VariantUsageMember
 *          | InterfaceNonOccurrenceUsageMember
 *          | SourceSuccessionMember )? InterfaceOccurrenceUsageMember
 *          | AliasMember
 *          | Import
 *
 *      InterfaceNonOccurrenceUsageMember = MemberPrefix InterfaceNonOccurrenceUsageElement
 *
 *      InterfaceNonOccurrenceUsageElement = ReferenceUsage
 *          | AttributeUsage
 *          | EnumerationUsage
 *          | BindingConnectorAsUsage
 *          | SuccessionAsUsage
 *
 *      InterfaceOccurrenceUsageMember =
 *          MemberPrefix InterfaceOccurrenceUsageElement
 *
 *      InterfaceOccurrenceUsageElement = DefaultInterfaceEnd | StructureUsageElement | BehaviorUsageElement
 *      DefaultInterfaceEnd = ( FeatureDirection )? ( 'abstract' | 'variation')? 'end' Usage
 */


/**
 * TODO
 *
 *      InterfaceUsage = OccurrenceUsagePrefix 'interface'
 *              InterfaceUsageDeclaration InterfaceBody
 *
 *      InterfaceUsageDeclaration =
 *          UsageDeclaration ValuePart? ( 'connect' InterfacePart )?
 *          | InterfacePart
 *
 *      InterfacePart : InterfaceUsage = BinaryInterfacePart | NaryInterfacePart
 *
 *      BinaryInterfacePart = InterfaceEndMember 'to' InterfaceEndMember
 *
 *      NaryInterfacePart = '(' InterfaceEndMember ',' InterfaceEndMember ( ',' ownedRelationship += InterfaceEndMember )* ')'
 *
 *      InterfaceEndMember = InterfaceEnd
 *
 *      InterfaceEnd = ( declaredName = Name REFERENCES )? OwnedReferenceSubsetting ( OwnedMultiplicity )?
 */
fun SysMLv2.InterfaceUsage() {
    val connection = InterfaceUsageActions(context = this.semantics, creator=::InterfaceUsageImplementation, defaultType = mutableListOf("Interfaces::Interface"))
    connection.create(Identification(null, null))
    INTERFACE.consume()
    alternatives {
        NAME_LIT then TO starts { }
        NAME_LIT then DPDP starts { }
        others {
            Identification().also { connection.setIdentification(it) }
            optional(TYPED_BY) {
                TYPED_BY.consume()
                QualifiedNameList().also { connection.addTyping(it) }
            }
            CONNECT.consume()
        }
    }

    alternatives {
        NAME_LIT starts {
            QualifiedName().also { connection.addSource(mutableListOf(it)) }
            TO.consume()
            QualifiedName().also { connection.addTarget(mutableListOf(it)) }
        }

        LBRACE starts {
            LBRACE.consume()
            QualifiedNameList().also { connection.addSource(it) }
            RBRACE.consume()
        }
    }
    DefinitionBody(Resolved())
    connection.finish()
}
