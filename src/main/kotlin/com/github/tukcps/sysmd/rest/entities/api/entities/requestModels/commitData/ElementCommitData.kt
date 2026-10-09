package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData

import com.github.tukcps.sysmd.model.datamodel.*
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.sysml.*
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.real.ia.RealRange
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * The data payload of a commit
 */
@Serializable
data class ElementCommitData (
    override var id: Uuid? = null,
    override var type: ElementType = ElementType.Element,
    override var aliasIds: MutableList<String> = ArrayList(),
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var elementId: Uuid = Uuid.random(),
    override var isImpliedIncluded: Boolean? = null,
    override var ownedAnnotation: MutableList<ElementReference> = ArrayList(),
    override var ownedElement: MutableList<ElementReference> = ArrayList(),
    override var ownedRelationship: MutableList<ElementReference> = ArrayList(),
    override var owner: ElementReference? = null,
    override var owningMembership: ElementReference? = null,
    override var owningNamespace: ElementReference? = null,
    override var owningRelationship: ElementReference? = null,
    override var qualifiedName: String? = null,
    override var language: String? = null,
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,
    override var body: String? = null,
    override var isImplied: Boolean? = null,
    override var isStandard: Boolean? = null,
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
    override var direction: Feature.FeatureDirectionKind? = null,
    override var source: MutableList<ElementReference> = mutableListOf(),
    override var target: MutableList<ElementReference> = mutableListOf(),
    override var ownedRelatedElement: MutableList<ElementReference> = mutableListOf(),
    override var owningRelatedElement: ElementReference? = null,
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
    @Serializable(with = LongBoundSerializer::class)
    override var literalIntegerValue: LongBound? = null,
    @Serializable(with = RealRangeSerializer::class)
    override var literalRationalValue: RealRange? = null,
    override var literalBooleanValue: Boolean? = null,
    override var isNegated: Boolean? = null,
    override var isParallel: Boolean? = null,
    override var operator: String? = null,
    override var reqId: String? = null,
    override var triggerInvocationExpressionKind: TriggerInvocationExpression.TriggerKind? = null
): CommitData {
    override fun clone() = copy()

    /**
     * `indices` declared by `./.`.
     * MOF multiplicity: `0..1`.
     */
    override var indices: IntRange?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * `input` declared by `./.`.
     * MOF multiplicity: `0..1`.
     */
    override var input: String?
        get() = TODO("Not yet implemented")
        set(value) {}
}