@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.InterfaceUsageActions
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.InterfaceUsageImplementation


/**
 * 8.2.2.14 Interfaces Textual Notation
 * 8.2.2.14.1 Interface Definitions
 *
 *      InterfaceDefinition = OccurrenceDefinitionPrefix 'interface' 'def' DefinitionDeclaration InterfaceBody
 */
fun SysMLv2.InterfaceDefinition() = ConnectionDefinitionActions(semantics, ::InterfaceDefinitionImplementation, specializes = "Interfaces::Interface").parse {
    // OccurrenceDefinitionPrefix is handled by owning body and results are in prefixes
    INTERFACE.consume()
    DEF.consume()
    DefinitionDeclaration()
    InterfaceBody()
}

/**
 *      InterfaceBody = ';' | '{' InterfaceBodyItem* '}'
 */
fun SysMLv2.InterfaceBody() {
    alternatives {
        LCURBRACE then {
            noOrMore(end = { token.kind == RCURBRACE }) {
                InterfaceBodyItem()
            }
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
 *          | (SourceSuccessionMember)? InterfaceOccurrenceUsageMember
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
 *      InterfaceOccurrenceUsageMember =
 *          MemberPrefix InterfaceOccurrenceUsageElement
 *
 *      InterfaceOccurrenceUsageElement = DefaultInterfaceEnd | StructureUsageElement | BehaviorUsageElement
 *      DefaultInterfaceEnd = (FeatureDirection)? ( 'abstract' | 'variation')? 'end' Usage
 */
internal fun SysMLv2.InterfaceOccurrenceUsageMember() {
    when (token.kind) {
        in structureUsageElementStart -> StructureUsageElement()
        in behaviorUsageElementStart -> BehaviorUsageElement()
        in setOf(IN, INOUT, OUT, ABSTRACT, VARIATION, END) -> {
            IN.optional()
            OUT.optional()
            INOUT.optional()
            alternatives {
                ABSTRACT then {}
                VARIATION then {}
                others { }
            }
            END.consume()
            Unsupported("Default Interface End not yet implemented")
        }
        else -> {}
    }
}
fun SysMLv2.interfaceOccurrenceUsageStart() = token.kind in structureUsageElementStart + behaviorUsageElementStart



/**
 *      InterfaceUsage = OccurrenceUsagePrefix 'interface'
 *              InterfaceUsageDeclaration InterfaceBody
 */
fun SysMLv2.InterfaceUsage() = InterfaceUsageActions(context = semantics, creator=::InterfaceUsageImplementation, defaultType = "Interfaces::Interface").parse {

    INTERFACE.consume()
    InterfaceUsageDeclaration()
    InterfaceBody()
}


/**
 *      InterfaceUsageDeclaration =
 *          UsageDeclaration ValuePart? ('connect' InterfacePart)?
 *          | InterfacePart
 */
fun SysMLv2.InterfaceUsageDeclaration() {

    alternatives {
        LBRACE            starts { semantics.create(null); InterfacePart() }
        NAME_LIT then DOT starts { semantics.create(null); InterfacePart() }
        NAME_LIT then TO  starts { semantics.create(null); InterfacePart() }

        others {
            UsageDeclaration()
            optional(EQ or DPEQ) {
                ValuePart()
            }
            optional(CONNECT) {
                CONNECT.consume()
                InterfacePart()
            }
        }
    }
}

/**
 *      InterfacePart: InterfaceUsage = BinaryInterfacePart | NaryInterfacePart
 */
fun SysMLv2.InterfacePart() {
     alternatives {
         LBRACE starts { NaryInterfacePart() }
         others { BinaryInterfacePart() }
     }
}

/**
 *      BinaryInterfacePart = InterfaceEndMember 'to' InterfaceEndMember
 */
fun SysMLv2.BinaryInterfacePart() {
    ConnectorEndMember()  .also { semantics.setSourceEnd(it) }
    TO.consume()
    ConnectorEndMember()  .also { semantics.setTargetEnd(it) }
}


/**
 *      NaryInterfacePart = '(' InterfaceEndMember ',' InterfaceEndMember ( ',' ownedRelationship += InterfaceEndMember )* ')'
 *      InterfaceEndMember = InterfaceEnd
 *      InterfaceEnd = ( declaredName = Name REFERENCES )? OwnedReferenceSubsetting ( OwnedMultiplicity )?
 */
fun SysMLv2.NaryInterfacePart() {
    LBRACE.consume()
    ConnectorEndMember()  .also { semantics.setTargetEnd(it) }
    COMMA.consume()
    ConnectorEndMember()  .also { semantics.addTargetEnd(it) }
    noOrMore (COMMA) {
        COMMA.consume()
        ConnectorEndMember()  .also { semantics.addTargetEnd(it) }
    }
    RBRACE.consume()
}
