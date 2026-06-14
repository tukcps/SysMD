package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlin.uuid.Uuid

/**
 * Might be incomplete; not in use, better use the simpler CommitData.
 */
data class RelationshipCommitData(
    override var id: Uuid? = null,
    override var type: String = "Relationship",       // mandatory type of the metamodel as annotation
    override var elementId: Uuid? = null,
    override var aliasIds: MutableList<String> = ArrayList(),
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var documentation: Identified? = null,
    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var isLibraryElement: Boolean? = null,
    override var name: String? = null,
    override var ownedAnnotation: MutableList<Identified> = ArrayList(),
    override var ownedElement: MutableList<Identified> = ArrayList(),
    var ownedRelatedElement: Identified? = null,
    override var ownedRelationship: MutableList<Identified> = ArrayList(),
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    var owningRelatedElement: Identified? = null,
    override var owningRelationship: Identified? = null,
    override var qualifiedName: String? = null,
    var relatedElement: Identified? = null,
    override var shortName: String? = null,
    override var source: MutableList<Identified>? = ArrayList(),
    override var target: MutableList<Identified>? = ArrayList(),
    override var textualRepresentation: MutableList<Identified>? = ArrayList(),
    override var body: String? = null,
    override var direction: String? = null,
    override var language: String? = null,
    override var importedMemberName: String?,
    override var importedNamespace: String?,
    override var isStandard: Boolean?,
    override var visibility: String?,
    override var isAbstract: Boolean?,
    override var isSufficient: Boolean?,
    override var isConjugated: Boolean?,
    override var isUnique: Boolean?,
    override var isOrdered: Boolean?,
    override var isComposite: Boolean?,
    override var isEnd: Boolean?,
    override var isDerived: Boolean?,
    override var isReadOnly: Boolean?,
): CommitData {
    override fun clone() = this.copy()
}