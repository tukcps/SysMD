@file:Suppress("UNCHECKED_CAST", "FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.ActionDefinitionActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.ActionDefinitionImplementation


/**
 * 8.2.2.16.1 Action Definitions
 *
 *      ActionDefinition = OccurrenceDefinitionPrefix 'action' 'def'
 *          DefinitionDeclaration ActionBody
 */
fun SysMLv2.ActionDefinition() {
    val actionDefinition = ActionDefinitionActions<ActionDefinition>(semantics, ::ActionDefinitionImplementation, mutableListOf("Actions::Action" ))
    ACTION.consume()
    DEF.consume()
    DefinitionDeclaration(actionDefinition as TypeActions<Type>)
    ActionBody(Resolved(ref=actionDefinition.created!!))
    actionDefinition.finish()
}


/**
 *      ActionBody = ';' | '{' ActionBodyItem* '}'

 */
fun SysMLv2.ActionBody(owner: Resolved<Type>) {
    alternatives {
        SEMICOLON then {}
        LCURBRACE starts {
            LCURBRACE.consume()
            semantics.pushOwner(owner)
            noOrMore(end = {token.kind == RCURBRACE}) {
                ActionBodyItem()
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
    }
}

/**
 *      ActionBodyItem =
 *          NonBehaviorBodyItem
 *          | InitialNodeMember ( ActionTargetSuccessionMember )*
 *          | SourceSuccessionMember? ActionBehaviorMember ( ActionTargetSuccessionMember )*
 *          | GuardedSuccessionMember
 *
 *      ActionBehaviorMember = BehaviorUsageMember | ActionNodeMember
 */
fun SysMLv2.ActionBodyItem() {
    MemberPrefix()
    when {
        nonBehaviorBodyItemStart()          -> NonBehaviorBodyItem()
        FIRST.starts()                      -> {
            InitialNodeMember()
            noOrMore(THEN) {
                    ActionTargetSuccessionMember()
                }
        }
        behaviorUsageElementStart.starts()  -> BehaviorUsageElement()
        actionNodeStart.starts()            -> ActionNode()
        else -> throwSyntaxError("Unknown action body item ${token.kind}")
    }
}

fun SysMLv2.actionBodyItemStarts() =
    nonBehaviorBodyItemStart() or (token.kind == FIRST) or
            behaviorUsageElementStart.starts() or
            actionNodeStart.starts()

fun SysMLv2.ActionTargetSuccessionMember(){
    THEN.consume()
    QualifiedName()
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
    RelationshipBody(Resolved(null, null, null))
}


/**
 * 8.2.2.16.2 Action Usages
 *
 *      ActionUsage = OccurrenceUsagePrefix 'action' ActionUsageDeclaration ActionBody
 */
fun SysMLv2.ActionUsage() {
    val actionUsage = sysMLSemantics.ActionUsageSemantics()
    ACTION.consume()
    ActionUsageDeclaration(actionUsage as FeatureActions<Feature>)
    ActionBody(Resolved(actionUsage.created!!))
    actionUsage.finish()
}

/**
 *      ActionUsageDeclaration = UsageDeclaration ValuePart?
 */
fun SysMLv2.ActionUsageDeclaration(featureActions: FeatureActions<Feature>) {
    UsageDeclaration(featureActions)
    optional(valuePartStart) {
        ValuePart(featureActions)
    }
}

/**
 *      PerformActionUsage = OccurrenceUsagePrefix 'perform' PerformActionUsageDeclaration ActionBody
 */
fun SysMLv2.PerformActionUsage() {
    val performUsage = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Actions::Action" ))
    PERFORM.consume()
    PerformActionUsageDeclaration(performUsage)
    ActionBody(Resolved(performUsage.created!!) )
    performUsage.finish()
}

/**
 *      PerformActionUsageDeclaration =
 *      ( OwnedReferenceSubsetting FeatureSpecializationPart? | 'action' UsageDeclaration ) ValuePart?
 *
 *  Note: OwnedReferenceSubsetting is a qualified name or feature chain
 */
fun SysMLv2.PerformActionUsageDeclaration(performUsage: FeatureActions<Feature>) {
    alternatives {
        NAME_LIT starts {
            OwnedReferenceSubsetting().also { performUsage.create(Identification(it)); performUsage.addReferences(it) }
            featureSpecializationPartStart.optional { FeatureSpecializationPart(performUsage) }
        }
        ACTION starts {
            ACTION.consume()
            UsageDeclaration(performUsage)
        }

    }
    optional(valuePartStart) {
        ValuePart(performUsage)
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
 *          ( 'else' ( ActionBodyParameterMember | IfNodeParameterMember ) )?
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
fun SysMLv2.ActionBodyParameter() {
    val actionBodyParameter = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Actions::Action" ))
    ACTION.optional {
        UsageDeclaration(actionBodyParameter)
    }
    LCURBRACE.consume()
    noOrMore({token.kind != RCURBRACE}) {
        ActionBodyItem()
    }
    RCURBRACE.consume()
    actionBodyParameter.finish()
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
 *      ActionNodeBody = ';' | '{' ( AnnotatingMember )* '}'
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