package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

class SysMLv2PackageBody {
}


/*
 * SysML Elements
 */
fun KerML.PackageBodyElement() {
    FeaturePrefixes()
    alternatives {
        t1 = token.considerMetaKeywords() // Dirty hack
        ALLOCATION starts { AllocationUsage() }
        ALLOCATE starts { AllocationUsage()}
        ALLOCATION then DEF starts { AllocationDefinition() }
        ASSERT starts { AssertConstraintUsage() }
        ASSOC starts { Association() }
        (ATTRIBUTE then DEF) starts { AttributeDef() }
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
        REDEFINES starts { AttributeUsage() }
        REP starts { TextualRepresentation() }
        REQUIREMENT starts { RequirementUsage() }
        REQUIREMENT then DEF starts { RequirementDefinition() }
        (STATE then DEF) starts { StateDef() }
        STATE starts { State() }
        SEMICOLON starts { SEMICOLON.consume(); /* Empty statement */ }
        TRANSITION starts { Transition() }
        // TODO
        // HASHTAG starts { UserDefinedKeyWord() }
    }
}