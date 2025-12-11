@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ActionDefinitionActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ActionUsageActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ActionDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.ActionUsageImplementation


/**
 * 8.2.2.16.1 Action Definitions
 *
 *      ActionDefinition = OccurrenceDefinitionPrefix 'action' 'def'
 *          DefinitionDeclaration ActionBody
 */
fun SysMLv2.ActionDefinition() = ActionDefinitionActions(semantics, ::ActionDefinitionImplementation, "Actions::Action" ).parse {
    ACTION.consume()
    DEF.consume()
    DefinitionDeclaration()
    ActionBody()
}


/**
 *      ActionBody = ';' | '{' ActionBodyItem* '}'
 */
fun SysMLv2.ActionBody() {
    alternatives {
        SEMICOLON then {}
        LCURBRACE starts {
            LCURBRACE.consume()
            noOrMore(end = {token.kind == RCURBRACE}) {
                ActionBodyItem()
            }
            RCURBRACE.consume()
        }
    }
}

/**
 *      ActionBodyItem =
 *          NonBehaviorBodyItem
 *          | InitialNodeMember (ActionTargetSuccessionMember)*
 *          | SourceSuccessionMember? ActionBehaviorMember (ActionTargetSuccessionMember)*
 *          | GuardedSuccessionMember
 *
 *      ActionBehaviorMember = BehaviorUsageMember | ActionNodeMember
 */
fun SysMLv2.ActionBodyItem() {
    MemberPrefix()
    when {
        nonBehaviorBodyItemStart() -> NonBehaviorBodyItem()
        match(FIRST, NAME_LIT, DPDP) or match(FIRST, NAME_LIT, LCURBRACE) -> {
            InitialNodeMember()
            noOrMore(THEN) {
                ActionTargetSuccessionMember()
            }
        }

        match(FIRST, NAME_LIT, DOT) or match(FIRST, NAME_LIT, IF) or match(SUCCESSION) ->
            GuardedSuccession()

        behaviorUsageElementStart.starts() -> BehaviorUsageElement()
        actionNodeStart.starts() -> ActionNode()
        else -> throwSyntaxError("Unknown action body item ${token.kind}")
    }
}

fun SysMLv2.actionBodyItemStarts() =
    nonBehaviorBodyItemStart() || (token.kind == FIRST) ||
            behaviorUsageElementStart.starts() ||
            actionNodeStart.starts()

/**
 * ActionTargetSuccessionMember = MemberPrefix ActionTargetSuccession
 */
fun SysMLv2.ActionTargetSuccessionMember(){
    ActionTargetSuccession()
}

/**
 *      NonBehaviorBodyItem =
 *          Import | AliasMember | DefinitionMember | VariantUsageMember
 *          | NonOccurrenceUsageMember | ( SourceSuccessionMember )?  StructureUsageMember
 */
fun SysMLv2.NonBehaviorBodyItem() {
    when {
        IMPORT.starts()             -> { Import() }
        ALIAS.starts()              -> { AliasMember() }
        definitionElementStarts()   -> { DefinitionElement() }
        nonOccurrenceUsageStarts()  -> { NonOccurrenceUsageElement() }
        // TODO
    }
}
fun SysMLv2.nonBehaviorBodyItemStart() = IMPORT.starts() || ALIAS.starts()
        || nonOccurrenceUsageStarts()
        || definitionElementStarts()


/**
 *      InitialNodeMember = MemberPrefix 'first' [QualifiedName] RelationshipBody
 *
 *      ActionNodeMember = MemberPrefix ActionNode
 *
 *      ActionTargetSuccessionMember = MemberPrefix ActionTargetSuccession
 *
 *      GuardedSuccessionMember = MemberPrefix GuardedSuccession
 */
fun SysMLv2.InitialNodeMember() {
    FIRST.consume()
    optional(NAME_LIT) {
        QualifiedName()
    }
    RelationshipBody()
}


/**
 * 8.2.2.16.2 Action Usages
 *
 *      ActionUsage = OccurrenceUsagePrefix 'action' ActionUsageDeclaration ActionBody
 */
fun SysMLv2.ActionUsage() = ActionUsageActions(semantics, ::ActionUsageImplementation).parse {
    ACTION.consume()
    ActionUsageDeclaration()
    ActionBody()
}

/**
 *      ActionUsageDeclaration = UsageDeclaration ValuePart?
 */
fun SysMLv2.ActionUsageDeclaration() {
    UsageDeclaration()
    optional(valuePartStart) {
        ValuePart()
    }
}

/**
 *      PerformActionUsage = OccurrenceUsagePrefix 'perform' PerformActionUsageDeclaration ActionBody
 */
fun SysMLv2.PerformActionUsage() = FeatureActions<Feature>(semantics, ::FeatureImplementation, "Actions::Action").parse {
    PERFORM.consume()
    PerformActionUsageDeclaration()
    ActionBody()
}

/**
 *      PerformActionUsageDeclaration =
 *      (OwnedReferenceSubsetting FeatureSpecializationPart? | 'action' UsageDeclaration) ValuePart?
 *
 *  Note: OwnedReferenceSubsetting is a qualified name or feature chain
 */
fun SysMLv2.PerformActionUsageDeclaration() {
    alternatives {
        NAME_LIT starts {
            OwnedReferenceSubsetting()
            featureSpecializationPartStart.optional { FeatureSpecializationPart() }
            semantics.create(null)
        }
        ACTION starts {
            ACTION.consume()
            UsageDeclaration()
        }
    }
    optional(valuePartStart) {
        ValuePart()
    }
}
val performActionUsageDeclarationStart = setOf(NAME_LIT, ACTION)

/**
 *      ActionNode = ControlNode| AssignmentNode | SendNode | AcceptNode | IfNode | WhileLoopNode | ForLoopNode
 *
 *      ActionNodeUsageDeclaration = 'action' UsageDeclaration?
 *
 *      ActionNodePrefix = OccurrenceUsagePrefix ActionNodeUsageDeclaration?
 */
fun SysMLv2.ActionNode() {
    alternatives {
        controlNodeStart starts { ControlNode() }
        SEND             starts { Unsupported() }
        ACCEPT           starts { Unsupported() }
        IF               starts { IfNode() }
        //
        FOR              starts { Unsupported() }
    }
}
val actionNodeStart get() = controlNodeStart

/**
 *      IfNode = ActionNodePrefix
 *          'if' ExpressionParameterMember ActionBodyParameterMember
 *          ('else' ( ActionBodyParameterMember | IfNodeParameterMember) )?
 */
fun SysMLv2.IfNode() {
    IF.consume()
    Expression()
    ActionBodyParameter()
    ELSE.optional {
        ActionBodyItem()
    }
}

/**
 *      ActionBodyParameter = ( 'action' UsageDeclaration? )?
 *          '{' ActionBodyItem* '}'
 */
fun SysMLv2.ActionBodyParameter() = FeatureActions<Feature>(semantics, ::FeatureImplementation, "Actions::Action" ).parse {
    ACTION.optional {
        UsageDeclaration()
    }
    LCURBRACE.consume()
    noOrMore({token.kind != RCURBRACE}) {
        ActionBodyItem()
    }
    RCURBRACE.consume()
}

/**
 * 8.2.2.16.3 Control Nodes
 *
 *      ControlNode = MergeNode | DecisionNode | JoinNode| ForkNode
 *
 *      MergeNode    = ControlNodePrefix 'merge' UsageDeclaration ActionNodeBody
 *      DecisionNode = ControlNodePrefix 'decide' UsageDeclaration ActionNodeBody
 *      JoinNode     = ControlNodePrefix 'join' UsageDeclaration ActionNodeBody
 *      ForkNode     = ControlNodePrefix 'fork' UsageDeclaration ActionNodeBody
 */
fun SysMLv2.ControlNode() {
    ControlNodePrefix()
    alternatives {
        MERGE    then { }
        DECISION then { }
        JOIN     then { }
        FORK     then { }
    }
    // UsageDeclaration()
    ActionNodeBody()
}
val controlNodeStart = setOf(MERGE, DECISION, JOIN, FORK)+INDIVIDUAL

/**
 *      ControlNodePrefix = RefPrefix ('individual' )? ( PortionKind )?
 *          UsageExtensionKeyword*
 */
fun SysMLv2.ControlNodePrefix() {
    INDIVIDUAL.optional()
}

/**
 *      ActionNodeBody = ';' | '{' (AnnotatingMember)* '}'
 *  AnnotatingMember is an owned AnnotatingElement
 */
fun SysMLv2.ActionNodeBody() {
    alternatives {
        SEMICOLON then {}
        LCURBRACE starts {
            LCURBRACE.consume()
            AnnotatingElement()
            RCURBRACE.consume()
        }
    }
}


/**
 *      AcceptParameterPart: AcceptActionUsage =
 *          ownedRelationship += PayloadParameterMember
 *          ('via' ownedRelationship += NodeParameterMember)?
 */
fun SysMLv2.AcceptParameterPart() {
    PayloadParameter()
    optional(VIA) {
        VIA.consume()
        Unsupported("via not yet supported")
    }
}