package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.rest.entities.api.entities.ElementDAO
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
    var id: Uuid? = null,

    @SerialName("@type")
    override var type: String,

    override var name: String? = null,
    override var shortName: String? = null,
    override var declaredName: String? = null,
    override var declaredShortName: String? = null,
    override var ownedElement: MutableList<Identified> = mutableListOf(),     // The IDs of the owned elements.
    override var owner: Identified? = null,
    override var owningMembership: Identified? = null,
    override var owningNamespace: Identified? = null,
    override var owningRelationship: Identified? = null,

    // For type = Feature
    override var direction: String? = null,

    // For type = Import
    override var importedMemberName: String? = null,
    override var importedNamespace: String? = null,

    // For type = AnnotationElement
    override var language: String? = null,                       // Language, e.g. SysML, SysMD, ...
    override var body: String? = null,                           // Documentation

    // For type = Relationship and subtypes thereof
    override var source: MutableList<Identified>? = null,
    override var target: MutableList<Identified>? = null,
    override var isStandard: Boolean? = null,
    override var isLibraryElement: Boolean? = false,
    override var isImplied: Boolean? = null,
    override var isImpliedIncluded: Boolean? = null,
    override var documentation: Identified? = null,
    override var textualRepresentation: MutableList<Identified>? = null,
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
): ElementDAO {
    override var elementId: Uuid?
        get() = id
        set(value) { id = value }
    override var aliasIds: MutableList<String> = mutableListOf()
    override var ownedRelationship: MutableList<Identified> = mutableListOf()

    constructor(element: ElementDAO): this(id = element.elementId, type = element.type) {
        copyFrom(element)
    }
    private constructor(): this(id= Uuid.random(), type="Element")
}
