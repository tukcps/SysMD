package com.github.tukcps.sysmd.services.session.implementation

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.exceptions.Issue.Kind.ERROR_UNRESOLVED_NAME
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.Data
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.repositories.local.toElement
import com.github.tukcps.sysmd.services.session.*
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.*
import kotlin.reflect.full.isSubclassOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Merges two lists, ensuring some indices are preserved.
 * @param filter A filter predicate to apply to `other` (indices are taken beforehand)
 * @param preserveIndex Predicate under which an element's index mustn't change,
 *                      regardless of which list it originates from.
 * @return The first index that two elements are in conflict for, or null if the lists have been merged successfully
 */
private inline fun<T> MutableList<T>.mergeWith(other : List<T>, filter : (T) -> Boolean, preserveIndex : (T) -> Boolean) : Int?
{
    // the set of fixed indices
    val fixed = this.withIndex().filter { (_,x) ->
        preserveIndex(x)
    }.map {
        it.index
    }.toSet()

    for((ix,x) in other.withIndex())
    {
        when {
            !filter(x) -> continue
            !preserveIndex(x) || ix == size -> addLast(x)
            ix in fixed -> return ix
            ix !in indices -> throw IllegalStateException() // impossible by pigeonhole
            else -> addLast(set(ix, x))
        }
    }

    return null
}

/**
 * This class implements a model representation for use in the frontend SysMD compiler.
 * The model class holds all information read from a SysMD file.
 * It should be more or less a subset of the backend repository and lower-level service functionality.
 */
open class SessionImplementation(
    override val id: Uuid = Uuid.random(),
    override val libraries: List<String> = mutableListOf("SysMLLibraries"),
    override val status: SessionStatus = SessionStatus(),
    override var settings: SessionSettings = SessionSettings(),
    override var builder: DDBuilder = DDBuilder(),
    override val repo: Repository = Repository(),
    override var runlevel: Runlevel = Runlevel.NONE
): Session {

    override var solver = Solver(this)

    init {
        settings.runlevel = runlevel
    }

    /**
     * We have one special package "Global" that is the highest level package.
     * It can be accessed by the reference global.
     */
    override val global = NamespaceImplementation(
        declaredShortName = "Global",   // TODO --> Refactor both names to null or ""?
        declaredName="Global").also {
        it.owningRelationship = null
        it.model = this
        it.elementId = Generators.nameBasedGenerator().generate("Global")
    }

    /**
     * We have a single element "Any" that is the root of the inheritance tree.
     * It can be accessed by the reference anyElement.
     */
    override val anything = Anything(model = this).also {
        it.elementId = Generators.nameBasedGenerator().generate("Base::Anything")
    }

    /**
     * Loads all KerML libraries into the model.
     */
    private fun loadLibraries() {
        libraries.forEach {
            loadLibrary(it)
        }
    }

    init {
        initialize()
    }

    /** We set up initial libraries of KerML */
    private fun initialize() {
        // global, Base, and Anything are always present, even without loading a library.
        repo.elements[global.elementId!!] = global
        val baseLibrary = addOwnedMember(PackageImplementation(declaredName = "Base", isStandard = true, isLibraryElement = true).also {
            it.elementId = Generators.nameBasedGenerator().generate("Base")
        }, global)
        addOwnedMember(anything, baseLibrary)

        require(global === this[global.elementId!!])
        require(baseLibrary.ownedElement.size == 1)
        require(anything.owner == baseLibrary)

        status.updatedValues.clear()
        loadLibraries()

        initialize(settings.runlevel)
    }

    /**
     * Adds an element to the repo with the hashmap from all the uuid to its elements.
     * If an element with the same elementId exists, it will be updated
     * @param element the element to be added.
     */
    fun <T: Element> addElement(element: T): T {
        element.model = this

        val id = element.elementId ?: throw IllegalArgumentException("Attempt to add Element without elementId")

        repo.elements[id]?.also { conflict ->
            if(! conflict::class.isSubclassOf(element::class))
                throw IllegalArgumentException("Attempt to update existing, incompatible element ${element.qualifiedName}")

            conflict.updateFrom(element)
            status.updatedValues[element.path()] = "updated: '${element.qualifiedName}'"

            @Suppress("UNCHECKED_CAST")
            return conflict as T // this cast is checked via reflection
        }

        repo.elements[id] = element

        return element
    }

    override fun get(): Collection<Element> = repo.elements.values
    override fun get(elementId: UUID): Element? = repo.elements[elementId]
    @OptIn(ExperimentalUuidApi::class)
    override fun get(elementId: Uuid): Element? = repo.elements[elementId.toJavaUuid()]

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
        element.model = this
        namespace.model = this

        (element.name ?: element.shortName)?.let { namespace.getOwned<Element>(it) }?.let { existing ->
            if (existing::class.isSubclassOf(element::class)) {
                existing.updateFrom(element)
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
                    ownedMemberParameter = element,
                    owningType = namespace,
                    parameterIndex = namespace.parameter.size
                )

                else -> ParameterMembershipImplementation(
                    ownedMemberParameter = element,
                    owningType = namespace,
                    parameterIndex = namespace.parameter.size,
                )
            }

            is Feature if namespace is InstantiationExpression -> ParameterMembershipImplementation(
                ownedMemberParameter = element,
                owningType = namespace,
                parameterIndex = namespace.parameter.size
            )

            is Feature if namespace is Type -> if (element.isEnd)
                EndFeatureMembershipImplementation(namespace, element)
            else
                FeatureMembershipImplementation(ownedMemberFeature = element, owningType = namespace)

            else -> OwningMembershipImplementation(membershipOwningNamespace = namespace, memberElement = element)
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

        relationship.model = this
        relationship.owningRelatedElement = owningElement

        for(e in (relationship.source + relationship.target))
        {
            check(e.model === null || e.model === this)
            e.model = this
        }

        if (relationship is OwningMembership) {
            relationship.ownedElement.add(relationship.target.first())

            val member = relationship.memberElement

            if(member.owningRelationship !== null)
                return member.owningRelationship as T

            member.owningRelationship = relationship

            if(member.elementId === null)
                member.generateUUID()

            addElement(member).also {
                if(it !== member)
                    return it.owningRelationship as T
            }
        }
        if (owningElement.isLibraryElement)
            relationship.isLibraryElement = true

        if (relationship.elementId == null)
            relationship.generateUUID()

        // Specializations and subclasses thereof; updates if a similar specialization exists
        // As several specialization elements might exist, we check if there are some that are similar, i.e.
        // - have the same qualified name for the general class
        // - have the same id for the general class.
        if (relationship is Specialization) {
            relationship.model = this
            @Suppress("UNCHECKED_CAST")
            val foundSpecializations = owningElement.ownedRelationship.filter { it.javaClass == relationship.javaClass } as List<Specialization>
            foundSpecializations.forEach { found ->
                @Suppress("UNCHECKED_CAST")
                if (relationship.general == found.general )  // id is equal after name resolution/loading
                    return found as T
                if (relationship.general is Unresolved) {
                    val resolved = if (relationship !is Redefinition)
                        (relationship.owningNamespace?.resolve((relationship.general as Unresolved).relativeName!!) )?.member<Type>()
                    else
                        (relationship.owningNamespace as Type).resolve((relationship.general as Unresolved).relativeName!!)?.member<Feature>()

                    if ( resolved?.escapedName() == found.general.escapedName() )
                        @Suppress("UNCHECKED_CAST")
                        return found as T
                }
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
     * Adds new elements to the session. The elements are organized under the
     * root given as the second parameter. The existing root is maintained,
     * the root passed as the given parameter is just for easier analysis.
     * @param newElements collection of elements that will be cloned and added.
     */
    override fun import(newElements: Collection<ElementDAO>) {
        /** Resolves every Unresolved item in a list via its id */
        fun resolveUUIDs(xs : MutableList<Element>)
        {
            val iter = xs.listIterator()
            while(iter.hasNext()) {
                val cur = iter.next()

                if(cur !is Unresolved)
                    continue

                assert(cur is UnresolvedElement) // the other types aren't applicable

                val id = cur.id
                val res = if(id === null) global else get(id)

                if(res === null)
                {
                    status.error("Import contains reference to undefined element ID '$id'", kind = ERROR_UNRESOLVED_NAME)
                    iter.remove()
                    continue
                }

                iter.set(res)
            }
        }
        data class NewElement(val existing : Boolean, val data : ElementDAO, val element : Element)

        // 1. add elements to model
        val added = newElements.map { dao ->
            val id = dao.elementId
            val existing = get(id)
            NewElement(existing !== null, dao, existing ?: dao.toElement().also {
                repo.elements[id] = it
                it.model = this
            })
        }

        // 2. fix relationships (and set ownING relations)
        added.filter { !it.existing }.forEach { (_,dao,rel) ->
            if(rel !is Relationship)
                return@forEach

            // initialized by toElement()
            resolveUUIDs(rel.source)
            resolveUUIDs(rel.target)

            // fix ownership of relation itself
            if(rel.owningRelatedElement is Unresolved)
            {
                // have to read ID from DAO
                assert((rel.owningRelatedElement as Unresolved).id === null)
                val id = (dao.owningNamespace ?: TODO("Invalid DAO")).id
                // should never clobber source, except on invalid DAO (unresolved ID)
                rel.owningRelatedElement = if(id === null) global else get(id) ?: run {
                    status.error("Owner of element has undefined element id '$id'", kind = ERROR_UNRESOLVED_NAME)
                    global
                }
            }

            // fix ownership of actually owned elements
            if(rel is OwningMembership)
            {
                assert(rel.memberElement.owningRelationship === null)
                rel.memberElement.owningRelationship = rel
            }
        }

        // 3. fix ownED relations
        for((_,dao,element) in added)
        {
            if(element is Relationship && element.owner === global && element !in global.ownedRelationship)
            {
                // this element mustn't be index-sensitive
                global.ownedRelationship.add(element)
            }

            val toAdd = dao.ownedRelationship.map { get(it.id!!) as Relationship }

            // only need to reconcile indices for path-based UUIDs
            if(element.isTransient || !element.isLibraryElement || element.ownedRelationship.isEmpty())
            {
                element.ownedRelationship.addAll(toAdd)
                continue
            }

            // the set of already included UUIDs
            val present = element.ownedRelationship.mapNotNull { it.elementId }.toHashSet()

            val conflict = element.ownedRelationship.mergeWith(toAdd, {
                // import only new elements
                it.elementId!! !in present
            }, {
                // only preserve indices when relevant to path
                it !is OwningMembership || (it.target.firstOrNull()?.escapedName() !== null)
            })

            if(conflict !== null)
            {
                throw IllegalStateException("Conflicting elements for path ${element.path()}/$conflict; " +
                        "imported ${toAdd[conflict]} but already have ${element.ownedRelationship[conflict]}")
            }
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
        checkConsistency(repo.elements.values, checkForNoTransients = false)
        val exportCollection = mutableListOf<ElementDAO>()

        // create a collection for export, leaving out global and any
        repo.elements.values.forEach { element ->

            if (element is Unresolved)
                logger.error("Unresolved source ${element.qualifiedName?:element.path()} in export; the export will contain null pointers.")

            if (element is Relationship) {
                element.source.filter { it is Unresolved}.forEach {
                    logger.error("Unresolved source ${(it as Unresolved).relativeName} in export; the export will contain null pointers.")
                }
                element.target.filter { it is Unresolved}.forEach {
                    logger.error("Unresolved target ${(it as Unresolved).relativeName} in export; the export will contain null pointers.")
                }
            }

            if (element != global)
                exportCollection.add(element.toDAO())
        }

        for (element in exportCollection) {
            if (element.owner?.id == global.elementId) element.owner = null
        }
        return exportCollection.map { Data(payloadElementSnapshot = it) }
    }
}

/**
 * Returns a list of all elements of Type T
 * @param T type for which elements will be filtered.
 */
inline fun <reified T: Element> Session.getAllOfClass(): List<T> =
    get().filterIsInstance<T>()
