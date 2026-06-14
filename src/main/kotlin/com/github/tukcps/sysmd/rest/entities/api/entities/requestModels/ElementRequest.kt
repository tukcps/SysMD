package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.rest.entities.api.entities.ElementDAO
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
class ElementRequest(): ElementDAO {
    @SerialName("@id")
    var id: Uuid = Uuid.random()

    @SerialName("@type")
    override var type: String = ""

    // The following are required for every Element
    override var name: String? = null
    override var shortName: String? = null
    override var declaredShortName: String? = null
    override var declaredName: String? = null
    override var ownedElement: MutableList<Identified> = mutableListOf() // The IDs of the owned elements.
    override var owner: Identified? = null                 // The ID of the owner; if null, the node is the root of an ownership tree

    // For type Feature :
    override var direction: String? = null

    override var importedMemberName: String? = null
    override var importedNamespace: String? = null

    // For type AnnotationElement and subclasses
    override var language: String? = null   // language, e.g. SysMD, SysML
    override var body: String? = null       // The code in e.g. SysMD or SysML v2 textual

    // For Relationship and subtypes thereof:
    override var source: MutableList<Identified>? = null     // qualified names of sources
    override var target: MutableList<Identified>? = null     // qualified names of targets

    override var visibility: String? = null

    override var isLibraryElement: Boolean? = false
    override var isAbstract: Boolean? = null
    override var isSufficient: Boolean? = null
    override var isConjugated: Boolean? = null
    override var isUnique: Boolean? = null
    override var isOrdered: Boolean? = null
    override var isComposite: Boolean? = null
    override var isEnd: Boolean? = null
    override var isDerived: Boolean? = null
    override var isReadOnly: Boolean? = null
    override var isStandard: Boolean? = false
    override var isImplied: Boolean? = null
    override var isImpliedIncluded: Boolean? = null

    override var elementId: Uuid?
        get() = id
        set(value) { id = value ?: Uuid.random() }

    override var aliasIds: MutableList<String> = mutableListOf()
    override var owningMembership: Identified? = null
    override var owningNamespace: Identified? = null
    override var owningRelationship: Identified? = null
    override var ownedRelationship: MutableList<Identified> = mutableListOf()
    override var documentation: Identified? = null
    override var textualRepresentation: MutableList<Identified>? = null

    constructor(element: ElementDAO): this() {
        copyFrom(element)
    }
}
