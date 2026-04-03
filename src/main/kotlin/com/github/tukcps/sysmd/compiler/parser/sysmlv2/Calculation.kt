@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.MemberPrefix
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedExpression
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.CalculationDefinitionActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
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
fun SysMLv2.CalculationDefinition() = CalculationDefinitionActions(semantics, ::CalculationDefinitionImplementation).parse {
    CALC.consume()
    DEF.consume()
    DefinitionDeclaration()
    CalculationBody()
}

/**
 *      CalculationUsage = OccurrenceUsagePrefix 'calc'
 *          ActionUsageDeclaration CalculationBody
 */
fun SysMLv2.CalculationUsage() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
    CALC.consume()
    ActionUsageDeclaration()
    CalculationBody()
}

/**
 *      CalculationBody = ';' | '{' CalculationBodyPart '}'
 *      CalculationBodyPart = CalculationBodyItem* (ResultExpressionMember)?
 */
fun SysMLv2.CalculationBody() {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            noOrMore({ CalculationbodyItemStarts() } ) {
                CalculationBodyItem()
            }
            optional ({token.kind != RCURBRACE}) {
                ResultExpressionMember()
            }
            RCURBRACE.consume()
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
    when {
        tokenIs(RETURN) -> {
            RETURN.consume().also { semantics.prefixes.add(OUT) }
            UsageElement()
        }
        actionBodyItemStarts() -> ActionBodyItem()
        else -> handleSyntaxError("Expected Calculation body item, but read '$token'")
    }
}
fun SysMLv2.CalculationbodyItemStarts(): Boolean = actionBodyItemStarts() || (token.kind == RETURN)

/**
 *      ResultExpressionMember = MemberPrefix?  OwnedExpression
 */
fun SysMLv2.ResultExpressionMember() {
    val owner = semantics.element<Namespace>()
    val iBeforeExpression = token.indices.first
    OwnedExpression().also {
        if (owner is Feature) {
            owner.indices = iBeforeExpression..consumedToken.indices.last
            owner.expression = input.subSequence(owner.indices!!).toString().trim()
        }
    }
}