package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * The data payload of a commit
 */
@Serializable
data class ElementCommitData (
    override var id: Uuid? = null,
    override var type: String = "ElementCommitData",
    override var aliasIds: MutableList<String> = ArrayList(),
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var documentation: Identified? = null,
    override var elementId: Uuid? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var isLibraryElement: Boolean? = null,
    override var name: String? = null,
    override var ownedAnnotation: MutableList<Identified> = ArrayList(),
    override var ownedElement: MutableList<Identified> = ArrayList(),
    override var ownedRelationship: MutableList<Identified> = ArrayList(),
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    override var owningRelationship: Identified? = null,
    override var qualifiedName: String? = null,
    override var language: String? = null,
    override var shortName: String? = null,
    override var textualRepresentation: MutableList<Identified>? = ArrayList(),
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,
    override var body: String? = null,
    override var isImplied: Boolean? = null,
    override var isStandard: Boolean? = null,
    override var visibility: String? = null,
    override var isAbstract: Boolean? = null,
    override var isSufficient: Boolean? = null,
    override var isConjugated: Boolean? = null,
    override var isUnique: Boolean? = null,
    override var isOrdered: Boolean? = null,
    override var isComposite: Boolean? = null,
    override var isEnd: Boolean? = null,
    override var isDerived: Boolean? = null,
    override var isReadOnly: Boolean? = null,
    override var direction: String? = null,
    override var source: MutableList<Identified>?,
    override var target: MutableList<Identified>?,
): CommitData {
    override fun clone() = copy()
}