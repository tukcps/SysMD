@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.AliasMember
import com.github.tukcps.sysmd.compiler.parser.kerml.FeatureChain
import com.github.tukcps.sysmd.compiler.parser.kerml.FeaturePrefix
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.Import
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.sysmlv2.structureUsageElementStart
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.InterfaceUsageActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Element
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
    // OccurrenceDefinitionPrefix is handled by owning body and results are in prefixes
    INTERFACE.consume()
    DEF.consume()
    DefinitionDeclaration(interfaceDefinition as TypeActions<Type>)
    InterfaceBody(Resolved(interfaceDefinition.created!!))
    interfaceDefinition.finish()
}

/**
 *
 *      InterfaceBody = ';' | '{' InterfaceBodyItem* '}'
 */
fun SysMLv2.InterfaceBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(end = { token.kind == RCURBRACE }) {
                InterfaceBodyItem()
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
    }
}

/**
 *      InterfaceBodyItem =
 *           DefinitionMember
 *          | VariantUsageMember
 *          | InterfaceNonOccurrenceUsageMember
 *          | (SourceSuccessionMember )? InterfaceOccurrenceUsageMember
 *          | AliasMember
 *          | Import
 *
 *      InterfaceNonOccurrenceUsageMember = MemberPrefix InterfaceNonOccurrenceUsageElement
 *
 *      InterfaceNonOccurrenceUsageElement =
 *            ReferenceUsage
 *          | AttributeUsage
 *          | EnumerationUsage
 *          | BindingConnectorAsUsage
 *          | SuccessionAsUsage
 */
internal fun SysMLv2.InterfaceBodyItem() {
    PUBLIC.optional                         { semantics.prefixes.add(PUBLIC)  }
    PRIVATE.optional                        { semantics.prefixes.add(PRIVATE) }
    PROTECTED.optional                      { semantics.prefixes.add(PROTECTED) }
    FeaturePrefix()
    when {
        definitionElementStarts()       -> DefinitionElement()
        tokenIs(VARIANT)            -> VariantUsageElement()
        referenceUsageStarts()          -> ReferenceUsage()
        interfaceOccurrenceUsageStart() -> InterfaceOccurrenceUsageMember()
        tokenIs(ATTRIBUTE)          -> AttributeUsage()
        tokenIs(ENUM)               -> EnumerationUsage()
        tokenIs(ALIAS)              -> AliasMember()
        tokenIs(IMPORT)             -> Import()
        else                -> { handleSyntaxError("Error while processing interface body item") }
    }
}

/**
 *
 *      InterfaceOccurrenceUsageMember =
 *          MemberPrefix InterfaceOccurrenceUsageElement
 *
 *      InterfaceOccurrenceUsageElement = DefaultInterfaceEnd | StructureUsageElement | BehaviorUsageElement
 *      DefaultInterfaceEnd = ( FeatureDirection )? ( 'abstract' | 'variation')? 'end' Usage
 */
internal fun SysMLv2.InterfaceOccurrenceUsageMember() {
    when {
        token.kind in structureUsageElementStart -> StructureUsageElement()
        token.kind in behaviorUsageElementStart -> BehaviorUsageElement()
    }
}
fun SysMLv2.interfaceOccurrenceUsageStart() = token.kind in structureUsageElementStart + behaviorUsageElementStart


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
        NAME_LIT then DPDP starts { } // Qualified name later on
        NAME_LIT then DOT starts { }  // Feature chain later on
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
        NAME_LIT then DPDP starts {
            QualifiedName().also { connection.addSource(mutableListOf(it)) }
            TO.consume()
            QualifiedName().also { connection.addTarget(mutableListOf(it)) }
        }

        NAME_LIT starts {
            FeatureChain().also { connection.addSource(mutableListOf(it)) }
            TO.consume()
            FeatureChain().also { connection.addTarget(mutableListOf(it)) }
        }

        LBRACE starts {
            LBRACE.consume()
            QualifiedNameList().also { connection.addSource(it) }
            RBRACE.consume()
        }
    }
    InterfaceBody(Resolved())
    connection.finish()
}
