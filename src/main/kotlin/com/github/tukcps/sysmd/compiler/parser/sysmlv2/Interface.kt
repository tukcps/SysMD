@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.parse
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionDefinitionAction
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.InterfaceUsageAction
import com.github.tukcps.sysmd.model.generated.ElementType


/**
 * 8.2.2.14 Interfaces Textual Notation
 * 8.2.2.14.1 Interface Definitions
 *
 *      InterfaceDefinition = OccurrenceDefinitionPrefix 'interface' 'def' DefinitionDeclaration InterfaceBody
 */
fun SysMLv2.InterfaceDefinition() = ConnectionDefinitionAction(
    semantics, ElementType.InterfaceDefinition, "Interfaces::Interface"
).parse {
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
        interfaceOccurrenceUsageStarts() -> InterfaceOccurrenceUsageMember()
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
    when {
        structureUsageElementStarts() -> StructureUsageElement()
        behaviorUsageElementStarts() -> BehaviorUsageElement()
        token.kind in setOf(IN, INOUT, OUT, ABSTRACT, VARIATION, END) -> {
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
fun SysMLv2.interfaceOccurrenceUsageStarts() = structureUsageElementStarts() || behaviorUsageElementStarts()


/**
 *      InterfaceUsage = OccurrenceUsagePrefix 'interface'
 *              InterfaceUsageDeclaration InterfaceBody
 */
fun SysMLv2.InterfaceUsage() = InterfaceUsageAction(context = semantics).parse {
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
        LBRACE            starts { InterfacePart() }
        NAME_LIT then DOT starts { InterfacePart() }
        NAME_LIT then TO  starts { InterfacePart() }

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
    ConnectorEnd()  .also { semantics.setSourceEnd(it) }
    TO.consume()
    ConnectorEnd()  .also { semantics.setTargetEnd(it) }
}


/**
 *      NaryInterfacePart = '(' InterfaceEndMember ',' InterfaceEndMember ( ',' ownedRelationship += InterfaceEndMember )* ')'
 *      InterfaceEndMember = InterfaceEnd
 *      InterfaceEnd = ( declaredName = Name REFERENCES )? OwnedReferenceSubsetting ( OwnedMultiplicity )?
 */
fun SysMLv2.NaryInterfacePart() {
    LBRACE.consume()
    ConnectorEnd()  .also { semantics.setTargetEnd(it) }
    COMMA.consume()
    ConnectorEnd()  .also { semantics.addTargetEnd(it) }
    noOrMore (COMMA) {
        COMMA.consume()
        ConnectorEnd()  .also { semantics.addTargetEnd(it) }
    }
    RBRACE.consume()
}
