@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*



/**
 * Standard:
 *      CalculationDefinition = OccurrenceDefinitionPrefix 'calc' 'def' DefinitionDeclaration CalculationBody
 *
 * Implemented; prefixes are consumed before.
 *      CalculationDefinition :- "calc def" Identification [ :> QualifiedName] CalculationBody
 */
fun KerML.CalculationDefinition() {
    val semantics = semantics.calculationActions()
    consume(CALCULATION)
    consume(DEF)
    Identification().also { semantics?.identification = it }
    optional(SPECIALIZES, consume = true) {
        QualifiedName().also { semantics?.superclass = it }
    }
    semantics?.create()
    CalculationBody(Resolved(semantics?.created!!))
}

/**
 * CalculationUsage : CalculationUsage = OccurrenceUsagePrefix 'calc' ActionUsageDeclaration CalculationBody
 */

/*
 * CalculationBody : Type = ';' | '{' CalculationBodyPart '}'
 * CalculationBodyPart : Type = CalculationBodyItem* ( ownedRelationship += ResultExpressionMember )?
 */
fun KerML.CalculationBody(owner: Resolved<Element>) {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            semantics.pushOwner(owner)
            oneOrMoreUntil(RCURBRACE) {
                CalculationBodyItem()
            }
            RCURBRACE.consume()
            semantics.popOwner()
        }
    }
}

/**
 * CalculationBodyItem : Type = ActionBodyItem | ownedRelationship += ReturnParameterMember
 * ReturnParameterMember : ReturnParameterMembership = MemberPrefix? 'return' ownedRelatedElement += UsageElement
 * ResultExpressionMember : ResultExpressionMembership =MemberPrefix? ownedRelatedElement += OwnedExpression
 */
fun KerML.CalculationBodyItem() {
    alternatives {
        RETURN starts { AttributeUsage() }
        others { DefinitionBodyItem() } // --> ActionBodyItem
    }
}