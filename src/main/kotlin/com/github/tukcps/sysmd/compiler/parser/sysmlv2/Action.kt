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
import com.github.tukcps.sysmd.model.kerml.implementation.ParameterMembershipImplementation
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
        nonBehaviorBodyItemStart()  -> NonBehaviorBodyItem()

        initialNodeMemberStarts()   -> {
            InitialNodeMember()
            noOrMore(THEN) { ActionTargetSuccessionMember() }
        }

        THEN.starts()               -> {
            SourceSuccessionMember()
            when {  // ActionBehaviorMember
                behaviorUsageElementStarts()-> BehaviorUsageElement()
                actionNodeStarts() -> ActionNode()
            }
        }

        match(FIRST, NAME_LIT, DOT) or match(FIRST, NAME_LIT, IF) or match(SUCCESSION)
                                    -> GuardedSuccession()

        behaviorUsageElementStarts() -> BehaviorUsageElement()
        actionNodeStarts()           -> ActionNode()
        else -> throwSyntaxError("Unknown action body item ${token.kind}")
    }
}

fun SysMLv2.actionBodyItemStarts() =
    nonBehaviorBodyItemStart() || (token.kind == FIRST) ||
            behaviorUsageElementStart.starts() ||
            actionNodeStarts()

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
        // Variant missing
        structureUsageElementStarts() or THEN.starts()  -> {
            if (THEN.starts()) { SourceSuccessionMember() }
            StructureUsageElement() 
        }
        nonOccurrenceUsageStarts()  -> { NonOccurrenceUsageElement() }

        // TODO
    }
}
fun SysMLv2.nonBehaviorBodyItemStart() = IMPORT.starts() || ALIAS.starts()
        || nonOccurrenceUsageStarts()
        || definitionElementStarts()
        || structureUsageElementStarts()
        || (THEN.starts() && !ASSIGN.isNext() && !NAME_LIT.isNext())


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
fun SysMLv2.initialNodeMemberStarts() = match(FIRST, NAME_LIT, DPDP) or match(FIRST, NAME_LIT, LCURBRACE)


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
        ASSIGN           starts { AssignmentNode() }
        SEND             starts { Unsupported() }
        ACCEPT           starts { Unsupported() }
        IF               starts { IfNode() }
        WHILE or LOOP    starts { WhileLoopNode() }
        FOR              starts { Unsupported() }
    }
}
fun SysMLv2.actionNodeStarts() = token.kind in controlNodeStart + setOf(WHILE, LOOP, SEND, ACCEPT, IF, FOR, ASSIGN)

/**
 *      IfNode = ActionNodePrefix
 *          'if' ExpressionParameterMember ActionBodyParameterMember
 *          ('else' ( ActionBodyParameterMember | IfNodeParameterMember) )?
 */
fun SysMLv2.IfNode() {
    IF.consume()
    OwnedExpression()
    Expression().also {
        model.addOwnedRelationship(ParameterMembershipImplementation(
            ownedMemberParameter = it,
            owningType = semantics.element()
        ))
    }
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


/**
 *      WhileLoopNode : WhileLoopActionUsage =
 *          ActionNodePrefix
 *          ( 'while' ownedRelationship += ExpressionParameterMember
 *          | 'loop' ownedRelationship += EmptyParameterMember
 *          )
 *          ownedRelationship += ActionBodyParameterMember
 *          ( 'until' ownedRelationship += ExpressionParameterMember ';' )?
 */
fun SysMLv2.WhileLoopNode() {
    when(token.kind) {
        WHILE   -> { WHILE.consume(); Expression() }
        LOOP    -> { LOOP.consume() }
        else    -> { throwSyntaxError("While loop node: ${token.kind}, expect 'while' or 'loop'") }
    }
    ActionBodyParameter()
    optional(UNTIL, consume = true) { Expression(); SEMICOLON.consume() }
}

/**
 *      8.2.2.17.5 Assignment Action Usages
 *          AssignmentNode : AssignmentActionUsage =
 *              OccurrenceUsagePrefix
 *              AssignmentNodeDeclaration ActionBody
 */
fun SysMLv2.AssignmentNode() {
    AssignmentNodeDeclaration()
    ActionBody()
}

/**
 *      AssignmentNodeDeclaration: ActionUsage =
 *          ( ActionNodeUsageDeclaration )? 'assign'
 *          ownedRelationship += AssignmentTargetMember
 *          ownedRelationship += FeatureChainMember ':='
 *          ownedRelationship += NodeParameterMember
 */
fun SysMLv2.AssignmentNodeDeclaration() {
    ASSIGN.consume()
    NAME_LIT.consume()
    DPEQ.consume()
    Expression()
}

/**
 * AssignmentTargetMember : ParameterMembership =
 * ownedRelatedElement += AssignmentTargetParameter
 * AssignmentTargetParameter : ReferenceUsage =
 * ( ownedRelationship += AssignmentTargetBinding '.' )?
 * AssignmentTargetBinding : FeatureValue =
 * ownedRelatedElement += NonFeatureChainPrimaryExpression
 * FeatureChainMember : Membership =
 * memberElement = [QualifiedName]
 * | OwnedFeatureChainMember
 * OwnedFeatureChainMember : OwningMembership =
 * ownedRelatedElement += OwnedFeatureChain
 */