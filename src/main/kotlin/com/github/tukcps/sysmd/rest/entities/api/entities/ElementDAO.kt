package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlin.uuid.Uuid


/**
 * Interface of exchanged abstract element representations.
 * All Element Data Abstractions shall implement this interface for the sake of interoperability.
 * The standard furthermore differentiates between elements and relationships; this is not done here.
 */
interface ElementDAO {
    var elementId: Uuid?
    var aliasIds: MutableList<String>
    var type: String
    var name: String?
    var shortName: String?
    var declaredName: String?
    var declaredShortName: String?
    var ownedElement: MutableList<Identified>
    var owner: Identified?
    var owningMembership: Identified?
    var owningNamespace: Identified?
    var owningRelationship: Identified?
    var ownedRelationship: MutableList<Identified>
    var importedMemberName: String?
    var importedNamespace: String?

    // For type AnnotationElement, Expression:
    var language: String?
    var body: String?

    var isImplied: Boolean?
    var isImpliedIncluded: Boolean?
    var isStandard: Boolean?
    var isLibraryElement: Boolean?

    // Namespace
    var visibility: String?

    // Type
    var isAbstract: Boolean?
    var isSufficient: Boolean?
    var isConjugated: Boolean?

    // Feature
    var isUnique: Boolean?
    var isOrdered: Boolean?
    var isComposite: Boolean?
    var isEnd: Boolean?
    var isDerived: Boolean?
    var isReadOnly: Boolean?
    var direction: String?

    // Annotations
    var documentation: Identified?
    var textualRepresentation: MutableList<Identified>?

    // For Relationship and subtypes thereof:
    var source: MutableList<Identified>?
    var target: MutableList<Identified>?

    /**
     * A simple function that copies all fields from a source that implements the ElementDAO interface
     * @param source of data
     * @return the DAO with the copied data
     */
    @Suppress("unused")
    fun copyFrom(source: ElementDAO): ElementDAO {
        elementId = source.elementId
        type = source.type
        aliasIds = mutableListOf<String>().also { it.addAll(source.aliasIds) }
        name = source.name
        shortName = source.shortName
        declaredName = source.declaredName
        declaredShortName = source.declaredShortName
        owner = source.owner?.clone()
        owningNamespace = source.owningNamespace
        owningRelationship = source.owningRelationship

        ownedElement = source.ownedElement.clone()
        direction = source.direction
        importedMemberName = source.importedMemberName
        importedNamespace = source.importedNamespace
        language = source.language
        body = source.body

        visibility = source.visibility

        isImplied = source.isImplied
        isImpliedIncluded = source.isImpliedIncluded
        isStandard = source.isStandard
        isLibraryElement = source.isLibraryElement
        isAbstract = source.isAbstract
        isSufficient = source.isSufficient
        isConjugated = source.isConjugated
        isUnique = source.isUnique
        isOrdered = source.isOrdered
        isComposite = source.isComposite
        isEnd = source.isEnd
        isDerived = source.isDerived
        isReadOnly = source.isReadOnly

        textualRepresentation = source.textualRepresentation?.clone()
        documentation = source.documentation

        // For Relationship and subtypes thereof:
        this.source = source.source?.clone()
        this.target = source.target?.clone()
        this.ownedRelationship = source.ownedRelationship.clone()
        return this
    }
}
