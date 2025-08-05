package com.github.tukcps.sysmd.services.session

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.cspsolver.DiscreteSolver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.OwningMembershipImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmd.services.resolve.resolve
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.*
import kotlin.reflect.full.isSubclassOf


/**
 * This class implements a model representation for use in the frontend SysMD compiler.
 * The model class holds all information read from a SysMD file.
 * It should be more or less a subset of the backend repository and lower-level service functionality.
 */
class SessionImplementation(
    override val id: UUID = UUID.randomUUID(),
    override val libraries: MutableList<String> = mutableListOf("SysMLLibraries"),
    override val status: SessionStatus = SessionStatus(),
    override var settings: SessionSettings = SessionSettings(),
    override var builder: DDBuilder = DDBuilder(),
    override val repo: Repository = Repository(),
    override val astNodes: MutableMap<UUID, AstNode> = HashMap(),
    override var project: ProjectData? = null
): Session {

    override var dSolver = DiscreteSolver(this)

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
        val baseLibrary = addOwnedMember(PackageImplementation(declaredName = "Base", isStandard = true, isLibraryElement = true), global)
        addOwnedMember(anything, baseLibrary)

        require(global === this[global.elementId!!])
        require(baseLibrary.ownedElement.size == 1)
        require(anything.owner == baseLibrary)

        status.updatedValues.clear()
        loadLibraries()

        if (project != null) {
            val elementData: Collection<ElementDAO> = project!!.data
                .filter { it.payloadElementSnapshot != null }
                .mapNotNull { it.payloadElementSnapshot }

            import(elementData)
        }

        if (settings.initialize)
            initialize(1)
    }


    /**
     * Remove all elements except any or global, and reset all internal data structures and states.
     */
    override fun reset() {
        try {

            // clean model
            repo.reset()
            global.ownedRelationship.clear()
            anything.subtypes.clear()
            repo.elements[global.elementId!!] = global
            status.reset()

            // Now everything should be clean
            initialize()
            // New solver-related stuff
            dSolver = DiscreteSolver(this)

            loadLibraries()
            status.updatedValues.clear()
            if (settings.initialize)
                initialize(1)

            /** Caches of important types */
            repo.numberType = null
            repo.realType = null
            repo.booleanType = null
            repo.integerType = global.resolve<DataType>("ScalarValues::Integer")
            repo.stringType = null
            repo.schedule.clear()
        } catch (_: Exception) {
            status.fatal("Error during reset; it is recommended to re-start SysMD!")
        }
    }

    /**
     * Adds an element to the repo with the hashmap from all the uuids to its elements.
     * If an element with the same elementId exists, it will be updated
     * @param element the element to be added.
     */
    fun <T: Element> addElement(element: T): T {
        element.model = this
        if (element.elementId == null)
            status.error("Attempt to add Element without elementId")
        else {
            if (element.elementId in repo.elements.keys) {
                if (get(element.elementId!!)!!::class.isSubclassOf(element::class)) {
                    repo.elements[element.elementId]!!.updateFrom(element)
                    status.updatedValues[element.elementId!!] = "updated: '${element.qualifiedName}'"
                } else
                    status.error("Attempt to update existing, incompatible element ${element.escapedName()}")
            }
            else
                repo.elements[element.elementId!!] = element
        }
        @Suppress("UNCHECKED_CAST")
        return repo.elements[element.elementId] as T
    }

    override fun get(): Collection<Element> = repo.elements.values
    override fun get(elementId: UUID): Element? = repo.elements[elementId]

    /**
     * Adds an owned member to a namespace.
     * The method creates an owning membership relationship and gives the element an elementId if it is still null.
     * @param element the element to be added; must not be an owned relationship, then use addOwnedRelationship
     * @param namespace the namespace to which the element will be added via a membership
     */
    override fun <T : Element> addOwnedMember(element: T, namespace: Namespace, index: Int): T {
        require(element is Namespace || element is Annotation || element is Dependency || element !is Relationship)

        element.model = this

        if (element.name != null || element.shortName != null) {
            val exists = namespace.getOwned<Element>(element.name?:element.shortName!!)
            if (exists != null) {
                if (exists::class.isSubclassOf(element::class)) {
                    exists.updateFrom(element)
                    status.updatedValues[exists.elementId!!] = "updated: '${exists.qualifiedName}'"
                    @Suppress("UNCHECKED_CAST")
                    return exists as T
                } else {
                    status.fatal("Attempt to update existing, incompatible element ${element.escapedName()}")
                }
            }
        }
        val owningMembership = OwningMembershipImplementation(membershipOwningNamespace = namespace, memberElement = element)
        element.owningRelationship = owningMembership
        addOwnedRelationship(owningMembership, namespace)
        if (element.elementId == null) {
            require(element.model != null)
            element.generateUUID()
        }
        return addElement(element)
    }

    /**
     * Method that adds an owned relationship to an element.
     * @param owningElement the element to be added; if null, the source of the relationship is used as the owning element
     * @param relationship the relationship to be added as the owned related element
     */
    override fun <T: Relationship> addOwnedRelationship(relationship: T, owningElement: Element?): T {

        val element = owningElement ?: relationship.source.first()

        relationship.model = this
        relationship.owningRelatedElement = element
        if (relationship is OwningMembership) {
            relationship.ownedElement.add(relationship.target.first())
        }
        if (element.isLibraryElement)
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
            val foundSpecializations = element.ownedRelationship.filter { it.javaClass == relationship.javaClass } as List<Specialization>
            foundSpecializations.forEach { found ->
                @Suppress("UNCHECKED_CAST")
                if (relationship.general == found.general )  // id is equal after name resolution/loading
                    return found as T
                if (relationship.general is Unresolved) {
                    val resolved = if (relationship !is Redefinition)
                            (relationship.owningNamespace?.resolve<Type>((relationship.general as Unresolved).relativeName!!) )
                        else
                            (relationship.owningNamespace as Type).resolve<Feature>((relationship.general as Unresolved).relativeName!!)
                    if ( resolved?.escapedName() == found.general.escapedName() )
                        @Suppress("UNCHECKED_CAST")
                        return found as T
                }
            }
        }

        val added = addElement(relationship)

        if (added == relationship)
            element.ownedRelationship.add(relationship)

        return added
    }

    /**
     * Deletes all owned relationships that satisfy a condition
     * @param owner the element that owns the relationships to be deleted.
     * @param condition a lambda expression; if it is satisfied, an owned relationship will be deleted
     */
    override fun deleteOwnedRelationship(
        owner: Element,
        condition: (Relationship) -> Boolean
    ) {
        owner.ownedRelationship.forEach { relationship ->
            if ( condition(relationship) )
                owner.ownedRelationship.remove(relationship)
        }
    }

    /**
     * Loads all usages of a project.
     * For this purpose, the method gets the usages and calls the method loadProject.
     */
    override fun loadUsages() {
        project?.getUsages()?.forEach {
            if(it is ProjectUsageData) {
                try {
                    loadProject(it.resource.toString(), initialize = false, setProject = false)
                } catch (error: Exception) {
                    status.fatal("Error in usage ${it.resource}: " + (error.message ?: "unknown error"))
                }
            }
        }
    }

    /**
     * Adds new elements to the session. The elements are organized under the
     * root given as the second parameter. The existing root is maintained,
     * the root passed as the given parameter is just for easier analysis.
     * @param newElements collection of elements that will be cloned and added.
     */
    override fun import(newElements: Collection<ElementDAO>) {

        // Add all elements to built-in hashmap
        val added = mutableListOf<ElementDAO>()

        // val existing = newElements.filter { it.elementId in repo.elements.keys}
        newElements.forEach { dao ->
            if (dao.elementId !in repo.elements.keys) {
                val element = dao.toElement()
                repo.elements[dao.elementId] = element
                element.model = this
                added.add(dao)
            }
        }

        // Replace source's and target's ids against references
        added.forEach { dao ->
            val element = get(dao.elementId)

            if ( element is Relationship) {
                element.source.clear()
                element.target.clear()
                dao.source?.forEach { id -> element.source.add(get(id.id!!)!!) }
                dao.target?.forEach { id -> element.target.add(get(id.id!!)!!) }
            }

            if (element is Relationship && element !is Namespace) {
                element.owningRelatedElement = get(dao.owningNamespace?.id?:global.elementId!!)!!
                element.owningRelatedElement.ownedRelationship.add(element)
            } else {
                if (dao.owner?.id == null)
                    addOwnedMember(element!!, global)
                else {
                    element!!.owningRelationship = get(dao.owningRelationship?.id ?: dao.owner?.id!!) as Relationship
                    element.owningRelationship?.ownedElement?.add(element)
                }

                element.ownedRelationship = dao.ownedRelationship.map { get(it.id!!)!! as Relationship }.toMutableList()
            }
        }

        // For all elements: take the order of owned relationships
        newElements.forEach { dao ->
            val element = get(dao.elementId)!!
            element.ownedRelationship = dao.ownedRelationship.map { get(it.id!!)!! as Relationship }.toMutableList()
            if (element is Relationship) {
                element.source.clear()
                element.target.clear()
                dao.source?.forEach { id -> element.source.add(get(id.id!!)!!) }
                dao.target?.forEach { id -> element.target.add(get(id.id!!)!!) }
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

            if (element != global && !(element is OwningMembership && element.owningNamespace == global) )
                exportCollection.add(element.toDAO())
        }

        for (element in exportCollection) {
            if (element.owner?.id == global.elementId) element.owner = null
        }
        return exportCollection.map { Data(payloadElementSnapshot = it) }
    }

    /** Just removes the session from the existing sessions */
    override fun endSession() {
        SessionManager.kill(this.id)
    }

    /**
     * Deletes an element with a given reference and all owned elements.
     * Also updates ownedElements of the owner.
     * @param element reference to the element to be deleted
     */
    override fun delete(element: Element): Element? {

        when (element) {
            global -> return null
            anything -> return null
            is Relationship if (element !is Namespace) -> {
                for (r in element.ownedRelatedElement.toMutableList()) {
                    delete(r)
                }
            }
            else -> {
                for (e in element.ownedRelationship.toMutableList()) {
                    delete(e)
                }
            }
        }
        element.owningRelationship?.ownedElement?.remove(element)
        element.owningNamespace?.ownedRelationship?.remove(element)
        repo.elements.remove(element.elementId!!)
        return null
    }


    override fun toString(): String {
        return "Session { project=${project?.name}, $status }"
    }

    /**
     * Returns a list of all Feature's variables.
     * @param List with variables of all features
     */
    override fun getVariables(): List<Variable> {
        val variables = mutableListOf<Variable>()
        get().filterIsInstance<Feature>().forEach { if (it.variable != null) variables.add(it.variable!!) }
        return variables
    }
}

/**
 * Returns a list of all elements of Type T
 * @param T type for which elements will be filtered.
 */
inline fun <reified T: Element> Session.getAllOfClass(): List<T> =
    get().filterIsInstance<T>()
