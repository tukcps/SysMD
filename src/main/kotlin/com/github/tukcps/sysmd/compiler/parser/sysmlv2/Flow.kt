@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.ValuePart
import com.github.tukcps.sysmd.compiler.parser.kerml.valuePartStart
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ConnectionUsageActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.sysml.ConnectionUsage
import com.github.tukcps.sysmd.model.sysml.implementation.ConnectionUsageImplementation

/**
 * 8.2.2.13.4 Messages and Flow Connections
 *
 *      FlowConnectionDefinition : OccurrenceDefinitionPrefix 'flow' 'def' Definition
 */
fun SysMLv2.FlowConnectionDefinition() {
    FLOW.consume()
    DEF.consume()
    // Definition()
}

/**
 *      Message : FlowConnectionUsage = OccurrenceUsagePrefix 'message' MessageDeclaration DefinitionBody { isAbstract = true }
 */
fun SysMLv2.Message() {
    val message = ConnectionUsageActions<ConnectionUsage>(semantics, ::ConnectionUsageImplementation, mutableListOf("Connections::Connection"))
    MESSAGE.consume()
    MessageDeclaration(message as FeatureActions<Feature>)
    DefinitionBody(Resolved(message.created!!))
}

/**
 *      MessageDeclaration : FlowConnectionUsage = UsageDeclaration ValuePart?
 *          ( 'of' ownedRelationship += FlowPayloadFeatureMember )?
 *          ( 'from' ownedRelationship += MessageEventMember
 *              'to' ownedRelationship += MessageEventMember
 *          )?
 *          | ownedRelationship += MessageEventMember 'to'
 *          ownedRelationship += MessageEventMember
 */
fun SysMLv2.MessageDeclaration(message: FeatureActions<Feature>) {
    UsageDeclaration(message)
    valuePartStart.optional {
        ValuePart(message)
    }
    OF.optional {

    }
    FROM.optional {

    }
    TO.optional {

    }
}

/**
 * MessageEventMember : ParameterMembership = MessageEvent
 *
 * MessageEvent : EventOccurrenceUsage = ownedRelationship += OwnedReferenceSubsetting
 *
 *      FlowConnectionUsage = OccurrenceUsagePrefix 'flow' FlowConnectionDeclaration DefinitionBody
 */
fun SysMLv2.FlowConnectionUsage() {
    val flowConnectionUsage = ConnectionUsageActions<ConnectionUsage>(semantics, ::ConnectionUsageImplementation, mutableListOf("Connections::Connection"))
    FLOW.consume()
    FlowConnectionDeclaration(flowConnectionUsage as FeatureActions<Feature>)
    DefinitionBody(Resolved(flowConnectionUsage.created!!))
    flowConnectionUsage.finish()
}

/**
 *      SuccessionFlowConnectionUsage = OccurrenceUsagePrefix 'succession' 'flow' FlowConnectionDeclaration DefinitionBody
 *      FlowConnectionDeclaration : FlowConnectionUsage = UsageDeclaration ValuePart?
 *          ( 'of' ownedRelationship += FlowPayloadFeatureMember )?
 *          ( 'from' ownedRelationship += FlowEndMember 'to' ownedRelationship += FlowEndMember )?
 *          | ownedRelationship += FlowEndMember 'to' ownedRelationship += FlowEndMember
 */
fun SysMLv2.FlowConnectionDeclaration(flowConnectionUsage: FeatureActions<Feature>) {

    when {
        token.kind in setOf(NAME_LIT, LCBRACE) && nextToken.kind !in setOf(DPDP, DOT) -> {
            UsageDeclaration(flowConnectionUsage)
            valuePartStart.optional {
                ValuePart(flowConnectionUsage)
            }
            OF.optional {
                Unsupported()
            }
            FROM.optional {
                FlowEndMember()
                TO.consume()
                FlowEndMember()
            }
        }
        else -> {
            flowConnectionUsage.create(identification = Identification(null, null))
            FlowEndMember()
            TO.consume()
            FlowEndMember()
        }
    }
}

/**
 * FlowPayloadFeatureMember : FeatureMembership =
 * ownedRelatedElement += FlowPayloadFeature
 * FlowPayloadFeature : ItemFeature =
 * PayloadFeature
 * PayloadFeature : Feature =
 * Identification? PayloadFeatureSpecializationPart
 * ValuePart?
 * | ownedRelationship += OwnedFeatureTyping
 * ( ownedRelationship += OwnedMultiplicity )?
 * | ownedRelationship += OwnedMultiplicity
 * ownedRelationship += OwnedFeatureTyping
 * PayloadFeatureSpecializationPart : Feature =
 * ( -> FeatureSpecialization )+ MultiplicityPart?
 * FeatureSpecialization*
 * | MultiplicityPart FeatureSpecialization+
 */

/**
 *      FlowEndMember : EndFeatureMembership = ownedRelatedElement += FlowEnd
 */
fun SysMLv2.FlowEndMember() {
    ConnectorEndMember()
}

/**
 *      FlowEnd : ItemFlowEnd = ( ownedRelationship += FlowEndSubsetting )?
 *          ownedRelationship += FlowFeatureMember
 *
 * FlowEndSubsetting : ReferenceSubsetting =
 * referencedFeature = [QualifiedName]
 * | referencedFeature = FeatureChainPrefix
 * { ownedRelatedElement += referencedFeature }
 * FeatureChainPrefix : Feature =
 * ( ownedRelationship += OwnedFeatureChaining '.' )+
 * ownedRelationship += OwnedFeatureChaining '.'
 * FlowFeatureMember : FeatureMembership =
 * ownedRelatedElement += FlowFeature
 * FlowFeature : ReferenceUsage =
 * ownedRelationship += FlowFeatureRedefinition
 * (See Note 1)
 * FlowFeatureRefefinition : Redefinition =
 * redefinedFeature = [QualifiedName]
 *
 */