package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

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
 * Use this class for serialization of the Element Response
 */
@Serializable
class ElementResponse(
    @SerialName("@id")
    var id: Uuid = Uuid.random(),

    @SerialName("@type")
    override var type: ElementType = ElementType.Element,

    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var ownedElement: MutableList<Identified> = mutableListOf(),     // The IDs of the owned elements.
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    override var owningRelationship: Identified? = null,

    // For type = Feature
    override var direction: Feature.FeatureDirectionKind? = null,

    // For type = Import
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,

    // For type = AnnotationElement
    override var language: String? = null,                       // Language, e.g. SysML, SysMD, ...
    override var body: String? = null,                           // Documentation

    // For type = Relationship and subtypes thereof
    override var source: MutableList<Identified> = mutableListOf(),
    override var target: MutableList<Identified> = mutableListOf(),
    override var isStandard: Boolean? = null,
    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var visibility: Import.VisibilityKind? = null,
    override var isAbstract: Boolean? = null,
    override var isSufficient: Boolean? = null,
    override var isConjugated: Boolean? = null,
    override var isUnique: Boolean? = null,
    override var isOrdered: Boolean? = null,
    override var isComposite: Boolean? = null,
    override var isEnd: Boolean? = null,
    override var isDerived: Boolean? = null,
    override var isConstant: Boolean? = null,
    override var locale: String? = null,
    override var isRecursive: Boolean? = null,
    override var isImportAll: Boolean? = null,
    override var memberShortName: String? = null,
    override var memberName: String? = null,
    override var isPortion: Boolean? = null,
    override var isVariable: Boolean? = null,
    override var isInitial: Boolean? = null,
    override var isDefault: Boolean? = null,
    override var isVariation: Boolean? = null,
    override var requirementConstraintMembershipKind: RequirementConstraintMembership.RequirementConstraintKind? = null,
    override var transitionFeatureMembershipKind: TransitionFeatureMembership.TransitionFeatureKind? = null,
    override var stateSubactionMembershipKind: StateSubactionMembership.StateSubactionKind? = null,
    override var isIndividual: Boolean? = null,
    override var portionKind: OccurrenceUsage.PortionKind? = null,
    override var literalStringValue: String? = null,
    override var literalIntegerValue: Long? = null,
    override var literalRationalValue: Double? = null,
    override var literalBooleanValue: Boolean? = null,
    override var isNegated: Boolean? = null,
    override var isParallel: Boolean? = null,
    override var operator: String? = null,
    override var reqId: String? = null,
    override var triggerInvocationExpressionKind: TriggerInvocationExpression.TriggerKind? = null,
    override var ownedRelatedElement: MutableList<Identified> = mutableListOf(),
    override var owningRelatedElement: Identified? = null,
): ElementDataIF {
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

    override var elementId: Uuid
        get() = id
        set(value) { id = value }
    override var aliasIds: MutableList<String> = mutableListOf()
    override var ownedRelationship: MutableList<Identified> = mutableListOf()
}
