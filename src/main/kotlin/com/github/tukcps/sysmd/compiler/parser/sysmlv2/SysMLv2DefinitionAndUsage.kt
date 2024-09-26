@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.kerml.Comment
import com.github.tukcps.sysmd.compiler.parser.kerml.Documentation
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved


/**
 * Body :-
 *            "{" ElementList "}"
 *          | ";"
 *          | "."  // Only for SysMD to end Triple
 */
internal fun KerML.DefinitionBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(end = { token.kind == RCURBRACE }) {
                DefinitionBodyItem()
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
        DOT then { }
    }
}



fun KerML.DefinitionBodyItem() {
    FeaturePrefixes()
    alternatives {
        t1 = token.considerMetaKeywords() // Dirty hack
        ALLOCATION starts { AllocationUsage() }
        ALLOCATE starts { AllocationUsage()}
        ALLOCATION then DEF starts { AllocationDefinition() }
        ASSERT starts { AssertConstraintUsage() }
        ASSOC starts { Association() }
        (ATTRIBUTE then DEF) starts { AttributeDefinition() }
        ATTRIBUTE starts { AttributeUsage() } // TODO
        CALCULATION starts { AttributeUsage() } // TODO
        (CALCULATION then DEF) starts{ CalculationDefinition() }
        COMMENT starts { Comment() }
        REGULAR_COMMENT starts { Comment() }
        CONNECT starts { ConnectionUsage() }
        CONNECTION starts { ConnectionUsage() }
        CONNECTION then DEF starts { ConnectionDefinition() }
        CONSTRAINT starts { ConstraintUsage() }
        DEPENDENCY starts { Dependency() }
        DOC starts { Documentation() }
        (ENTRY then ACTION) starts { Action() }
        INTERFACE starts { InterfaceUsage() }
        INTERFACE then DEF starts { InterfaceDefinition() }
        INV starts { Invariant() }
        ITEM starts { ItemUsage() }
        ITEM then DEF starts { ItemDefinition() }
        OCCURRENCE starts { OccurrenceUsage() }
        OCCURRENCE then DEF starts { OccurrenceDefinition() }
        PACKAGE starts { Package() }
        (PART then DEF) starts { PartDefinition() }
        PART starts { PartUsage() }
        PORT starts { PortUsage() }
        (PORT then DEF) starts { PortDefinition() }
        REP starts { TextualRepresentation() }
        REDEFINES starts { AttributeUsage() } // Could redefine whatever, or?
        REQUIREMENT starts { RequirementUsage() }
        REQUIREMENT then DEF starts { RequirementDefinition() }
        (STATE then DEF) starts { StateDef() }
        STATE starts { State() }
        SEMICOLON starts { SEMICOLON.consume(); /* Empty statement */ }
        TRANSITION starts { Transition() }
        HASHTAG starts { TODO() }

        others {
            if (semantics.prefixes.isNotEmpty()) {
                AttributeUsage()
            } else
                error.invoke("")
            }
    }
    semantics.prefixes.clear()
}