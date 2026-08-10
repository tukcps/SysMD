package com.github.tukcps.sysmd.services.session.implementation

import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.exceptions.InternalError
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.toElement
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.HandledAsElement
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.rest.entities.api.entities.CommitDataObject
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.Data
import com.github.tukcps.sysmd.services.session.*
import io.github.tukcps.aadd.DDBuilder
import java.util.*
import kotlin.reflect.full.isSubclassOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/**
 * This class implements a model representation for use in the frontend SysMD compiler.
 * The model class holds all information read from a SysMD file.
 * It should be more or less a subset of the backend repository and lower-level service functionality.
 * @param libraries standard libraries that are loaded
 * @param status a status object in which issues etc. are reported
 * @param settings a settings object via which settings are given
 *
 */
open class SessionImplementation(
    vararg libraries: String,
    override val status: SessionStatus = SessionStatus(),
    override var settings: SessionSettings = SessionSettings(),
    runlevel: Runlevel = Runlevel.NONE
): Session {

    /**
     * Unique id of the session.
     */
    override val id: Uuid = Uuid.random()
    override var librariesLoaded: LinkedHashSet<String> = linkedSetOf()
    override val repo: Repository = Repository()
    override var solver = Solver(this)
    override var builder: DDBuilder = DDBuilder()

    init {
        settings.runlevel = runlevel
    }

    /**
     * We have one special package "Global" that is the root namespace.
     * It can be accessed by the reference global.
     */
    override val global = NamespaceImplementation(
        this,   // TODO --> Refactor both names to null or ""?
        elementId = UuidPolicies.uuid5("Global"),
        declaredName="Global",
        declaredShortName = "Global"
    ).also {
        it.owningRelationship = null
    }

    /**
     * Loads the specified libraries into the model.
     * The libraries must be in the libraries folder in the resources folder.
     * @param libraries list of libraries to be loaded.
     */
    fun loadLibraries(libraries: List<String>) =
        libraries.forEach { loadLibrary(it) }

    /**
     * Loads a single library from resources into the session.
     * @param library name of the library
     */
    fun loadLibrary(library: String) {
        if (library !in librariesLoaded) {
            librariesLoaded.add(library)
            val elements = LibraryRepository.getLibrary(library, status)
            this.import(elements)
        }
    }

    init {
        initialize(libraries.toList())
    }

    /** We set up initial libraries of KerML */
    private fun initialize(libraries: List<String>) {
        repo[global.elementId] = global
        loadLibraries(libraries)
        initialize(settings.runlevel)
    }

    /**
     * Adds an element to the repo with the hashmap from all the uuid to its elements.
     * If an element with the same elementId exists, it will be updated
     * @param element the element to be added.
     */
    fun <T: Element> addElement(element: T): T {
        assert(element.model === this) { "Foreign element" }

        val id = element.elementId

        repo[id]?.also { conflict ->
            if( ! conflict::class.isSubclassOf(element::class) )
                throw SysMDException("Attempt to update existing, incompatible element ${element.qualifiedName}", element = element)

            conflict.updateFrom(element)
            status.updatedValues[element.path()] = "updated: '${element.qualifiedName}'"

            @Suppress("UNCHECKED_CAST")
            return conflict as T // this cast is checked via reflection
        }

        repo[id] = element

        return element
    }

    override fun get(): Collection<Element> = repo.elements()
    override fun get(elementId: UUID): Element? = repo[elementId.toKotlinUuid()]
    @OptIn(ExperimentalUuidApi::class)
    override fun get(elementId: Uuid): Element? = repo[elementId]

    /**
     * Adds an owned member to a namespace.
     * The method creates an owning membership relationship and gives the element an elementId if it is still null.
     * @param element the element to be added; must not be an owned relationship, then use addOwnedRelationship
     * @param namespace the namespace to which the element will be added via a membership
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Element> addOwnedMember(element: T, namespace: Namespace, visibility: Import.VisibilityKind): T {
        require(element is Namespace || element is Annotation || element is Dependency || element !is Relationship)
        check(element !== namespace)
        check(element !== global)
        assert(element.model === this) { "Foreign element" }
        assert(namespace.model === this) { "Foreign namespace" }

        (element.name ?: element.shortName)?.let { namespace.getOwned<Element>(it) }?.let { existing ->
            if (existing::class.isSubclassOf(element::class)) {
                existing.updateFrom(element)
                element.ownedRelationship.toList().forEach { rel ->
                    rel.owningRelatedElement = existing
                    if (rel is Specialization && existing is Type) {
                        rel.specific = existing
                    }
                    addOwnedRelationship(rel, existing)
                }
                status.updatedValues[existing.path()] = "updated: '${existing.qualifiedName}'"
                return existing as T
            } else {
                status.fatal("Attempt to update existing, incompatible element ${element.escapedName()}")
            }
        }

        val owningMembership = when (element) {
            is Feature if namespace is Function -> when {
                // FIXME: Distinguish unnamed out and return parameters
                element.direction == Feature.FeatureDirectionKind.OUT && element.name === null -> ReturnParameterMembershipImplementation(
                    this,
                    ownedMemberParameter = element,
                    owningType = namespace,
                    parameterIndex = namespace.parameter.size
                )

                else -> ParameterMembershipImplementation(
                    this,
                    ownedMemberParameter = element,
                    owningType = namespace,
                    parameterIndex = namespace.parameter.size,
                )
            }

            is Feature if namespace is InstantiationExpression -> ParameterMembershipImplementation(
                this,
                ownedMemberParameter = element,
                owningType = namespace,
                parameterIndex = namespace.parameter.size
            )

            is Feature if namespace is Type -> if (element.isEnd)
                EndFeatureMembershipImplementation(this, ownedMemberFeature = element, owningType = namespace)
            else
                FeatureMembershipImplementation(this, ownedMemberFeature = element, owningType = namespace)

            else -> OwningMembershipImplementation(this, membershipOwningNamespace = namespace, memberElement = element)
        }
        owningMembership.visibility = visibility

        return addOwnedRelationship(owningMembership).let {
            check(it.owningNamespace === namespace || it.memberElement !== element) {
                "Attempt to change ownership of element"
            }
            it.memberElement as T
        }
    }

    /**
     * Method that adds an owned relationship to an element.
     * @param owningElement the element to be added; if null, the source of the relationship is used as the owning element
     * @param relationship the relationship to be added as the owned related element
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T: Relationship> addOwnedRelationship(relationship: T, owningElement: Element?): T {
        val owningElement = owningElement ?: relationship.source.first()
        assert(relationship.model === this) { "Foreign relationship" }
        assert((relationship.source + relationship.target).all { it.model === this })

        relationship.owningRelatedElement = owningElement

        if (relationship is OwningMembership) {
            relationship.ownedElement.add(relationship.target.first())

            val member = relationship.memberElement

            if(member.owningRelationship !== null)
                return member.owningRelationship as T

            member.owningRelationship = relationship

            addElement(member).also {
                if(it !== member)
                    return it.owningRelationship as T
            }
        }

        // Specializations and subclasses thereof; updates if a similar specialization exists
        // As several specialization elements might exist, we check if there are some that are similar, i.e.
        // - have the same qualified name for the general class
        // - have the same id for the general class.
        if (relationship is Specialization) {
            @Suppress("UNCHECKED_CAST")
            val foundSpecializations = owningElement.ownedRelationship.filter { it.javaClass == relationship.javaClass } as List<Specialization>
            foundSpecializations.forEach { found ->
                @Suppress("UNCHECKED_CAST")
                if (relationship.general == found.general ) { // id is equal after name resolution/loading
                    if (owningElement is Type) found.specific = owningElement
                    return found as T
                }
                if (relationship.general is Unresolved) {
                    val resolved = if (relationship !is Redefinition)
                        (relationship.owningNamespace?.resolve((relationship.general as Unresolved).relativeName!!) )?.member<Type>()
                    else
                        (relationship.owningNamespace as Type).resolve((relationship.general as Unresolved).relativeName!!)?.member<Feature>()

                    if ( resolved?.escapedName() == found.general.escapedName() ) {
                        if (owningElement is Type) found.specific = owningElement
                        @Suppress("UNCHECKED_CAST")
                        return found as T
                    }
                }
            }
            if (owningElement is Type) {
                relationship.specific = owningElement
            }
        }

        addElement(relationship).also {
            if(it !== relationship)
                return it
        }

        owningElement.ownedRelationship.add(relationship)

        return relationship
    }

    /**
     * Method that adds an element to a package or the root namespace.
     * @param element the element to be added.
     * @param namespace the namespace where to add the element.
     */
    fun addOwnershipToGivenNamespace(element: Element, namespace: Namespace) {
        check(element != namespace)
        check(element != global)

        val names = namespace.member.map { it.name }
        if (element.declaredName in names) return

        val owningMembership = when(element) {
            is Feature -> FeatureMembershipImplementation(this)
            else -> OwningMembershipImplementation(this)
        }
        element.owningRelationship = owningMembership
        owningMembership.membershipOwningNamespace = namespace
        owningMembership.owningRelatedElement = namespace
        owningMembership.ownedElement.add(element)
        owningMembership.source = mutableListOf(namespace)
        owningMembership.target = mutableListOf(element)
        namespace.ownedRelationship.add(owningMembership)
        repo[element.elementId] = element
        repo[owningMembership.elementId] = owningMembership
    }


    // Created packages, if not existing, random Uuid is Ok
    private fun resolveOrCreate(name: QualifiedName?): Namespace {
        if (name.isNullOrBlank() || name == "Global") return global
        val segments = name.split("::")
        var where: Namespace = global
        segments.forEach { segment ->
            val resolved = where.resolveLocal(segment)?.member<Namespace>()
            where = resolved ?: addOwnedMember(PackageImplementation(this, declaredName = segment), where)
        }
        return where
    }

    /**
     * Adds new elements to the session. The elements are organized under the
     * root given as the second parameter.
     * @param newElements collection of elements that will be added.
     * @param namespaceQualifiedName where the new elements will be added; root as default.
     */
    override fun import(newElements: Collection<ElementDataIF>, namespaceQualifiedName: QualifiedName?) {
        val namespace = if (namespaceQualifiedName != null) resolveOrCreate(namespaceQualifiedName) else global
        // check(namespace == global || namespace is Package)
        import(newElements, namespace)
    }

    /**
     * Adds new elements to the session. The elements are organized under the
     * root given as the second parameter.
     * @param newElements collection of elements that will be added.
     * @param namespace where the new elements will be added; root as default.
     */
    override fun import(newElements: Collection<ElementDataIF>, namespace: Namespace) {
        try {
            fun resolveUuids(elements: MutableList<Element>) {
                val iterator = elements.listIterator()
                while (iterator.hasNext()) {
                    val unresolved = iterator.next() as? UnresolvedElement ?: continue

                    // 1. Resolve element based on availability (ID has priority over namespace/global context)
                    val resolved = when {
                        unresolved.id != null -> get(unresolved.id!!)
                        unresolved.relativeName == null -> namespace
                        else -> null // Pure relative names cannot be resolved in this pass
                    }

                    // 2. Apply resolved element or log error and remove the unresolved one
                    if (resolved != null) {
                        iterator.set(resolved)
                    } else {
                        if (unresolved.relativeName == null) {
                            val missingIdentifier = unresolved.id ?: unresolved.relativeName ?: "unknown"
                            status.error(
                                "Import of elements has reference to undefined element '$missingIdentifier'",
                                kind = Issue.Kind.ERROR_UNRESOLVED_NAME
                            )
                            iterator.remove()
                        }
                    }
                }
            }

            if(settings.reportDoubleNames)
            {
                newElements.groupBy { it.elementId }.filter { it.value.size > 1 }.forEach { (id, hits) ->
                    val existing = get(id)
                    val names = hits.mapNotNull {
                        it.declaredName ?: it.declaredShortName
                    }.toSet().joinToString("/").let {
                        if(it.isEmpty()) it else " ($it)"
                    }

                    val desc = existing?.path() ?: "$id$names"

                    status.warn(
                        message = "Duplicate element $desc is present ${hits.size} times in import",
                        element = hits.first() as? ElementData
                    )
                }
            }

            // 1. Add all created/updated elements to model
            val added = newElements.map { dao ->
                val element = dao.toElement(this)
                repo.set(dao.elementId, element) // potentially returns existing, updated element
            }
            // Now, there exists an element for each new elementId in the repository.

            // 2. Resolve IDs of all new relationships, elements
            added.forEach { addedElement ->

                val owningRelationship = addedElement.owningRelationship
                if (owningRelationship is Unresolved) {
                    if (owningRelationship.id === null) // && (addedElement is HandledAsElement)
                        addOwnershipToGivenNamespace(addedElement, namespace)
                    else
                        addedElement.owningRelationship = repo[owningRelationship.id] as? OwningMembership
                            ?: throw InternalError("In import: id of owning membership of element '${addedElement.declaredName ?: addedElement.declaredShortName}' not found", element = addedElement)
                }

                if (addedElement is Relationship) {
                    if (addedElement !is Namespace && addedElement !is Dependency && addedElement !is AnnotatingElement) {
                        val owningRelatedElement = addedElement.owningRelatedElement // owner just is a getter ...
                        if (owningRelatedElement is Unresolved)
                            if (owningRelatedElement.id != null) {
                                addedElement.owningRelatedElement = repo[owningRelatedElement.id]
                                    ?: throw InternalError("Inconsistent data in import (id of owning element not found)")
                                repo[owningRelatedElement.id]?.ownedRelationship?.add(addedElement)
                            } else {
                                addedElement.owningRelatedElement = namespace
                                namespace.ownedRelationship.add(addedElement)
                            }
                        resolveUuids(addedElement.ownedRelatedElement)
                    }
                    resolveUuids(addedElement.source)
                    resolveUuids(addedElement.target)
                }
            }
        } catch (ex: Exception) {
            if (ex !is SysMDException)
                status.fatal("Error during import", cause = ex )
            else
                status.fatal("Error during import", element = ex.element, cause = ex)
        }
    }

    /**
     * The function export creates a list of elements where
     * 1) Anything and Global are not included.
     * 2) owners or superclass are set to null for global resp. any.
     * 3) To be sure, all elements are cloned. No reference shall leave the session;
     *    just to prevent side effects.
     *    @return A pair of InterchangeProject data and a list of all Elements as DAO
     */
    override fun export(): Collection<CommitDataObject> {
        checkConsistency(repo.elements(), checkForNoTransients = false)
        val exportCollection = mutableListOf<ElementDataIF>()

        // create a collection for export, leaving out global and any
        repo.elements().forEach { element ->

            // We might pass unresolved elements later to allow resolution after importing other
            // libraries/usages
            if (element is Unresolved)
                logger.error("Unresolved element ${element.qualifiedName?:element.path()} in export.")

            if (element is Relationship) {
                element.source.filterIsInstance<Unresolved>().forEach {
                    logger.warn("Unresolved source ${it.relativeName} in export.")
                }
                element.target.filterIsInstance<Unresolved>().forEach {
                    logger.warn("Unresolved target ${it.relativeName} in export.")
                }
            }

            if (element is HandledAsElement) {
                if (element.owner == global) {
                    element.owningRelationship = null
                }
            }

            if (element != global) // && (element as? OwningMembership)?.membershipOwningNamespace != global
                exportCollection.add(element.toElementData())
        }

        for (element in exportCollection) {
            if (element.owner?.id == global.elementId) element.owner = null
        }
        return exportCollection.map { Data(payloadElementSnapshot = it) }
    }

    override fun toString(): String {
        return "$status, ${repo.elements().size} elements"
    }
}

/**
 * Returns a list of all elements of Type T
 * @param T type for which elements will be filtered.
 */
inline fun <reified T: Element> Session.getAllOfClass(): List<T> =
    get().filterIsInstance<T>()
