@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.compiler.parser.kerml.MemberPrefix
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation


/**
 * 8.2.2.18 Calculations Textual Notation
 *
 *      CalculationDefinition = OccurrenceDefinitionPrefix 'calc' 'def'
 *          DefinitionDeclaration CalculationBody
 *
 *      CalculationUsage = OccurrenceUsagePrefix 'calc'
 *          ActionUsageDeclaration CalculationBody
 *
 *      CalculationBody = ';' | '{' CalculationBodyPart '}'
 *
 *      CalculationBodyPart = CalculationBodyItem*
 *          ( ownedRelationship += ResultExpressionMember )?
 *
 *      CalculationBodyItem =
 *          ActionBodyItem
 *          | ReturnParameterMember
 *
 *      ReturnParameterMember = MemberPrefix? 'return' UsageElement
 *
 *      ResultExpressionMember = MemberPrefix? OwnedExpression
 */

/**
 *      CalculationDefinition = OccurrenceDefinitionPrefix 'calc' 'def'
 *          DefinitionDeclaration CalculationBody
 */
fun SysMLv2.CalculationDefinition() {
    val calculation = CalculationDefinitionActions(semantics,
        ::CalculationDefinitionImplementation, mutableListOf("Calculations::Calculation"))
    CALC.consume()
    DEF.consume()
    DefinitionDeclaration(calculation as TypeActions<Type>)
    CalculationBody(Resolved(calculation.created!!))
    calculation.finish()
}

/**
 *      CalculationUsage = OccurrenceUsagePrefix 'calc'
 *          ActionUsageDeclaration CalculationBody
 */
fun SysMLv2.CalculationUsage() {
    val calculationUsage = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf())
    CALC.consume()
    ActionUsageDeclaration(calculationUsage)
    CalculationBody(Resolved(calculationUsage.created!!))
}

/**
 *      CalculationBody = ';' | '{' CalculationBodyPart '}'
 *      CalculationBodyPart = CalculationBodyItem* ( ResultExpressionMember )?
 */
fun SysMLv2.CalculationBody(owner: Resolved<Element>) {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(end = { !CalculationbodyItemStarts() } ) {
                CalculationBodyItem()
            }
            optional ({token.kind != RCURBRACE}) {
                ResultExpressionMember()
            }
            RCURBRACE.consume()
            semantics.popOwner()
        }
    }
}

/**
 *      CalculationBodyItem  = ActionBodyItem | ReturnParameterMember
 *
 *      ReturnParameterMember = MemberPrefix? 'return' UsageElement
 */
fun SysMLv2.CalculationBodyItem() {
    MemberPrefix()
    alternatives {
        RETURN starts {
            RETURN.consume().also { semantics.prefixes.add(OUT) }
            UsageElement()
        }
        others {
            if (actionBodyItemStarts()) {
                ActionBodyItem() }
        }
    }
}
fun SysMLv2.CalculationbodyItemStarts(): Boolean = usageElementStarts() or (token.kind == RETURN)

/**
 *      ResultExpressionMember = MemberPrefix?  OwnedExpression
 */
fun SysMLv2.ResultExpressionMember() {
    Expression()
}