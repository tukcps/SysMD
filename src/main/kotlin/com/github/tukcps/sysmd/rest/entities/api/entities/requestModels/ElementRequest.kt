package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.model.datamodel.IntRangeSerializer
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.sysml.*
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


/**
 * The payload model for a request of a client to a SysML v2 server.
 * The value of _type_ is mandatory.
 * All other fields are optional, depending on the type.
 */
@Serializable
class ElementRequest(): ElementDataIF {
    @SerialName("@id")
    var id: Uuid = Uuid.random()

    @SerialName("@type")
    override var type: ElementType = ElementType.Element

    /**
     * `indices` declared by `./.`.
     * MOF multiplicity: `0..1`.
     */
    @Serializable(with = IntRangeSerializer::class)
    override var indices: IntRange? = null

    /**
     * `input` declared by `./.`.
     * MOF multiplicity: `0..1`.
     */
    override var input: String? = null

    // The following are required for every Element
    var name: String? = null
    var shortName: String? = null
    override var declaredShortName: String? = null
    override var declaredName: String? = null
    override var ownedElement: MutableList<Identified> = mutableListOf() // The IDs of the owned elements.
    override var owner: Identified? = null                 // The ID of the owner; if null, the node is the root of an ownership tree

    // For type Feature :
    override var direction: Feature.FeatureDirectionKind? = null

    /**
     * `isInitial` declared by `FeatureValue`.
     * MOF multiplicity: `0..1`.
     */
    override var isInitial: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isDefault` declared by `FeatureValue`.
     * MOF multiplicity: `0..1`.
     */
    override var isDefault: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isVariation` declared by `Usage`.
     * MOF multiplicity: `0..1`.
     */
    override var isVariation: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var importedMemberName: String? = null
    override var importedNamespace: String? = null

    /**
     * `value` declared by `LiteralString`.
     * MOF multiplicity: `0..1`.
     */
    override var literalStringValue: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `value` declared by `LiteralInteger`.
     * MOF multiplicity: `0..1`.
     */
    override var literalIntegerValue: Long?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `value` declared by `LiteralRational`.
     * MOF multiplicity: `0..1`.
     */
    override var literalRationalValue: Double?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `value` declared by `LiteralBoolean`.
     * MOF multiplicity: `0..1`.
     */
    override var literalBooleanValue: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isNegated` declared by `Invariant`.
     * MOF multiplicity: `0..1`.
     */
    override var isNegated: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isParallel` declared by `StateUsage`.
     * MOF multiplicity: `0..1`.
     */
    override var isParallel: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `operator` declared by `OperatorExpression`.
     * MOF multiplicity: `0..1`.
     */
    override var operator: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `reqId` declared by `RequirementUsage`.
     * MOF multiplicity: `0..1`.
     */
    override var reqId: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `kind` declared by `TriggerInvocationExpression`.
     * MOF multiplicity: `0..1`.
     */
    override var triggerInvocationExpressionKind: TriggerInvocationExpression.TriggerKind?
        get() = TODO("Not yet implemented")
        set(value) {}

    // For type AnnotationElement and subclasses
    override var language: String? = null   // language, e.g. SysMD, SysML
    override var body: String? = null       // The code in e.g. SysMD or SysML v2 textual

    /**
     * `locale` declared by `Comment`.
     * MOF multiplicity: `0..1`.
     */
    override var locale: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    // For Relationship and subtypes thereof:
    override var source: MutableList<Identified> = mutableListOf()     // qualified names of sources
    override var target: MutableList<Identified> = mutableListOf()     // qualified names of targets

    override var visibility: Import.VisibilityKind? = null

    /**
     * `isRecursive` declared by `Import`.
     * MOF multiplicity: `0..1`.
     */
    override var isRecursive: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isImportAll` declared by `Import`.
     * MOF multiplicity: `0..1`.
     */
    override var isImportAll: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `memberShortName` declared by `Membership`.
     * MOF multiplicity: `0..1`.
     */
    override var memberShortName: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `memberName` declared by `Membership`.
     * MOF multiplicity: `0..1`.
     */
    override var memberName: String? = null

    override var isAbstract: Boolean? = null
    override var isSufficient: Boolean? = null
    override var isConjugated: Boolean? = null

    /**
     * `kind` declared by `RequirementConstraintMembership`.
     * MOF multiplicity: `0..1`.
     */
    override var requirementConstraintMembershipKind: RequirementConstraintMembership.RequirementConstraintKind?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `kind` declared by `TransitionFeatureMembership`.
     * MOF multiplicity: `0..1`.
     */
    override var transitionFeatureMembershipKind: TransitionFeatureMembership.TransitionFeatureKind?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `kind` declared by `StateSubactionMembership`.
     * MOF multiplicity: `0..1`.
     */
    override var stateSubactionMembershipKind: StateSubactionMembership.StateSubactionKind?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isIndividual` declared by `OccurrenceUsage`.
     * MOF multiplicity: `0..1`.
     */
    override var isIndividual: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `portionKind` declared by `OccurrenceUsage`.
     * MOF multiplicity: `0..1`.
     */
    override var portionKind: OccurrenceUsage.PortionKind?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isUnique: Boolean? = null
    override var isOrdered: Boolean? = null
    override var isComposite: Boolean? = null
    override var isEnd: Boolean? = null
    override var isDerived: Boolean? = null

    /**
     * `isPortion` declared by `Feature`.
     * MOF multiplicity: `0..1`.
     */
    override var isPortion: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `isVariable` declared by `Feature`.
     * MOF multiplicity: `0..1`.
     */
    override var isVariable: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isConstant: Boolean? = null
    override var isStandard: Boolean? = false
    override var isImplied: Boolean? = null
    override var isImpliedIncluded: Boolean? = null

    override var elementId: Uuid
        get() = id
        set(value) { id = value}

    override var aliasIds: MutableList<String> = mutableListOf()
    override var owningMembership: Identified? = null
    override var owningNamespace: Identified? = null

    /**
     * `ownedRelationship` declared by `Element`.
     * MOF multiplicity: `0..*`.
     * Structural element reference.
     */
    override var ownedRelationship: MutableList<Identified>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var owningRelationship: Identified? = null

    override var ownedRelatedElement: MutableList<Identified>
        get() = TODO("Not yet implemented")
        set(value) {}

    override var owningRelatedElement: Identified?
        get() = TODO("Not yet implemented")
        set(value) {}

}
