package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.rest.entities.api.entities.ElementDAO
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.SerialName
import kotlin.uuid.Uuid

/**
 * Might be incomplete.
 */
class RelationshipResponse(
    @SerialName("@id")
    var id: Uuid? = null,
    @SerialName("@type")
    override var type: String = "relationship",
    override var aliasIds: MutableList<String> = mutableListOf(),
    override var name: String? = null,
    override var shortName: String? = null,
    override var documentation: Identified? = null,
    val effectiveName: String? = null,
    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var owner: Identified? = null,
    override var ownedElement: MutableList<Identified> = mutableListOf(),
    val ownedAnnotation: List<Identified> = mutableListOf(),
    val ownedRelatedElement: List<Identified> = mutableListOf(),
    override var ownedRelationship: MutableList<Identified> = mutableListOf(),
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    val qualifiedName: String? = null,
    val relatedElement: List<Identified>? = null,
    override var source: MutableList<Identified>? = mutableListOf(), // Id
    override var target: MutableList<Identified>? = mutableListOf(), // Id
    override var textualRepresentation: MutableList<Identified>? = null,
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var owningRelationship: Identified? = null,
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
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,
    override var language: String? = null,
    override var body: String? = null,
    override var direction: String? = null,
): ElementDAO {
    override var elementId: Uuid?
        get() = id
        set(value) { id = value }

    override var isStandard: Boolean? = null
    override var isLibraryElement: Boolean? = null

}
