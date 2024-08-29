package com.github.tukcps.sysmd.services.session

import com.fasterxml.uuid.Generators
import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.sysmd.cspsolver.DiscreteSolver
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.compiler.loadLibrary
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.createKerMLIntrospectionLibrary
import com.github.tukcps.sysmd.services.getRelationshipsTo
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmlv2.entities.ElementDAO
import java.util.*


/**
 * The Session is a container in which we work with a Project with a concrete model.
 * A model consists of instances of the elements that are a kind of Element.
 * The instances are saved in a repository 'repo' that implements fast access via
 * id and along relationships by hashmaps.
 */
interface Session {    // TODO: Refactor to ProjectSession? KerMLSession? ...?
    val id: UUID
    var project: ProjectData
    var files: MutableList<String>

    // Error messages and settings
    val status: SessionStatus
    val settings: SessionSettings

    /** global is an imaginary package that holds all root elements */
    // TODO: Refactor to root
    val global: Namespace

    /** any is the superclass of all non-classified things */
    // TODO: Refactor any to anything
    val any: Anything

    /** data contains data structures that represent the model and support efficient access, i.e. caches */
    val repo: Repository

    /** Whether the KerML libraries shall be generated resp. loaded */
    val loadKerML: Boolean

    /** specific information from the discrete solver */
    var dSolver: DiscreteSolver
    val builder: DDBuilder

    /**
     * Adds new elements to the session.
     * The existing root namespace is maintained.
     * @param newElements collection of elements that will be added.
     */
    fun import(newElements: Collection<ElementDAO>)

    /**
     * @return Returns the element of the session, and the root of the ownership tree (global)
     * as a Pair, but not the temporary elements.
     */
    fun export(): ProjectData

    /** @return Returns all elements in the session, including temporary elements, Any, Global. */
    fun get(): Collection<Element>

    /** Gets an element by its id */
    operator fun get(elementId: UUID): Element?

    /**
     * Creates a new element in a namespace that becomes owner of the element.
     * The new element can be of arbitrary subtype of ElementBase; i.e., ValueFeature, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, its fields will be updated.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    fun <T: Element> create(element: T, owner: Element): T

    /**
     * Like 'create', but the created element is marked as transient.
     * The new element can be of arbitrary subtype of ElementBase; i.e., ValueFeature, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, its fields will be updated.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    fun <T: Element> createTransient(element: T, owner: Namespace): T

    /**
     * Creates a new element in a namespace that becomes owner of the element.
     * The new element can be of arbitrary subtype of ElementBase; i.e., Expression, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, the existing one will
     * be deleted, including all its owned elements, and be replaced with the element given as argument.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    fun <T: Element> createOrReplace(element: T, owner: Element): T

    /**
     * Loads the usages into the model
     */
    fun loadUsage()

    /**
     * Deletes an element by reference. It also removes the hasA relationship in the according
     * parent element, and also all owned elements.
     * @param element the element to be deleted.
     */
    fun delete(element: Element): Element?

    /**
     * Gets the subclasses of an element. If the element is an instance, it will also consider
     * subclasses of the instance's class.
     */
    fun getSubclasses(element: Element): Collection<Classifier>

    /**
     * Gets the subtypes of an element. If the element is an instance, it will also consider
     * subclasses of the instance's class.
     */
    fun getSubtypes(element: Type): Collection<Type>

    /**
     * Ends a session without saving it.
     */
    fun endSession()

    /**
     * Removes all elements that are not related to textual models.
     */
    fun reset()


    /**
     * Used to model the path to a not-yet-included or not yet existing owner of an Element.
     * The reference consists of two parts: first, a namespace, and
     * second, relative to the namespace, a path in line with QualifiedName
     * conventions.
     * @param startOfPath the reference to a namespace where a path starts.
     * @param path the path, relative to the namespace to the element.
     */
    data class UnresolvedElement(
        var element:     Element,
        var path:        String? = null,
        var startOfPath: Element,
    )


    /**
     * Adds an element where the ownership is not yet identified by an ID.
     * It is identified by a qualified name and/or feature chain or a reverence or mix thereof.
     * These can in some cases only be identified correctly after all features and
     * inheritance information are initialized.
     * @param element the element to be added to an owner
     * @param startOfOwnerPath an element
     * @param path a qualified name relative to startOfPath
     */
    fun addUnownedElement(element: Element, path: String? = null, startOfOwnerPath: Element=global)
    fun getUnownedElements(): List< UnresolvedElement >
    fun dropUnownedElement(element: Element)
    fun updateUnownedElements(unownedElement: Element, existingElement: Element)

    /**
     * A shortcut that reads a SysMD-Model into the model.
     */
    operator fun String.unaryPlus()
}


/**
 * This class implements a model representation for use in the frontend SysMD compiler.
 * The model class holds all information read from a SysMD file.
 * It should be more or less a subset of the backend repository and lower-level service functionality.
 */
class SessionImplementation(
    override val id: UUID = UUID.randomUUID(),
    override val status: SessionStatus = SessionStatus(),
    override var settings: SessionSettings = SessionSettings(),
    override var builder: DDBuilder = DDBuilder(),
    override val repo: Repository = Repository(),
    override var loadKerML: Boolean = true
): Session {

    override var dSolver = DiscreteSolver(this)

    // The KerML Project interchange data for this session
    override var project = ProjectData(name="no name given", description = "")

    // The local files that belong to this project; all of them must be loaded
    override var files: MutableList<String> = mutableListOf()

    /**
     * We have one special package "Global" that is the highest level package.
     * It can be accessed by the reference global.
     */
    override val global = NamespaceImplementation(
        declaredShortName = "Global",   // TODO --> Refactor both names to null or ""?
        declaredName="Global").also {
        it.owner.ref = null
        it.model = this
        it.elementId = Generators.nameBasedGenerator().generate("Global")
    }

    /**
     * We have a single element "Any" that is the root of the inheritance tree.
     * It can be accessed by the reference anyElement.
     */
    override val any = Anything( // TODO --> Refactor to anything
        owner = Resolved(),
        model = this).also {
        it.elementId = Generators.nameBasedGenerator().generate("Base::Anything")
    }

    /** We set up initial libraries of KerML */
    init {
        // Hence, one cannot create them via the regular API.
        repo.elements[global.elementId] = global
        val baseLibrary = PackageImplementation(declaredName = "Base", isStandard = true, isLibraryElement = true)
        create(baseLibrary, global)
        create(any, baseLibrary)
        require(global === this[global.elementId])
        require(any.owner.ref == baseLibrary)

        if (loadKerML) {
            createKerMLIntrospectionLibrary()
            loadLibrary("Base.md", false)
            loadLibrary("Links.md", false)
            loadLibrary("Occurrences.md", false)
            loadLibrary("Objects.md", false)
            loadLibrary("ScalarValues.md", false)
            status.updates.clear()
            if (settings.initialize) initialize()
        }
    }


    /**
     * Remove all elements except any or global, and reset all internal data structures and states.
     */
    override fun reset() {
        try {
            // clean model
            repo.reset()
            global.ownedElement.clear()
            repo.elements[global.elementId] = global
            status.reset()

            // Now everything should be clean
            builder = DDBuilder()
            val baseLibrary = PackageImplementation(declaredName = "Base", isStandard = true)
            create(baseLibrary, global)
            create(any, baseLibrary)
            // New solver-related stuff
            dSolver = DiscreteSolver(this)

            if (loadKerML) {
                createKerMLIntrospectionLibrary()
                loadLibrary("Base.md", false)
                loadLibrary("Links.md", false)
                loadLibrary("Occurrences.md", false)
                loadLibrary("Objects.md", false)
                loadLibrary("ScalarValues.md", false)
                status.updates.clear()
                if (settings.initialize) initialize()
            }

            /** Caches of important types */
            repo.numberType = null
            repo.realType = null
            repo.booleanType = null
            repo.integerType = null
            repo.stringType = null
            repo.schedule.clear()
        } catch (e: Exception) {
            report("Error during reset; it is recommended to re-start SysMD!")
        }
    }


    override fun get(): Collection<Element> = repo.elements.values
    override fun get(elementId: UUID): Element? = repo.elements[elementId]

    override fun loadUsage() {
        project.getUsages().forEach {
            if(it is ProjectUsageData) {
                // println("    Loading usage $it.")
                try {
                    loadProject(it.resource.toString(), initialize = false, setProject = false)
                } catch (error: Exception) {
                    report("in usage ${it.resource}: " + (error.message ?: "unknown error"))
                }
            }
        }
    }

    /**
     * Adds new elements to the session. The elements are organized under the
     * root given as second parameter. The existing root is maintained,
     * the root passed as given parameter is just for easier analysis.
     * @param newElements collection of elements that will be cloned and added.
     */
    override fun import(newElements: Collection<ElementDAO>) {

        // we first separate root and other elements
        val import = mutableListOf<ElementDAO>()
        val roots = mutableListOf<ElementDAO>()
        newElements.forEach{newElement ->
            if (newElement.owner?.id == null)
                roots += newElement
            else
                import += newElement
        }

        // Add root elements of import to global of session; if there, overwrite.
        roots.forEach { root ->
            create(root.toElement(), global)
        }

        // Add all imported elements to the model, unless already there.
        import.forEach { element ->
            if (repo.elements[element.elementId] == null)
                repo.elements[element.elementId] = element.toElement()
        }

        // resolve uid -> ref, superclass null -> any, add model ref,
        resolveIDs()
    }


    /**
     * Sets the references based on elementId and also sets the model to this.
     */
    private fun resolveIDs() {
        // resolve uid -> ref, superclass null -> any, add model ref,
        get().forEach { element ->
            // set model to this
            element.model = this

            // resolve owner
            if (element != global) {
                element.owner.ref = get(
                    element.owner.id ?: throw SysMDError("Missing id in owner of ${element.qualifiedName}")
                )
            }

            // resolve ID to reference of owned elements.
            element.ownedElement.forEach { owned ->
                val resolved = get(owned.id!!)
                if (resolved != null) {
                    resolved.owner.ref = element
                    owned.ref = resolved
                } else
                    report("could not resolve owned element id of ${element.elementType} ${element.qualifiedName}")
            }

            // resolve ID to reference relationship's  sources and targets
            when(element) {
                is Relationship -> {
                    element.source.forEach { source ->
                        source.ref = if (source.id != null) get(source.id!!) else null
                    }
                    element.target.forEach { target ->
                        target.ref = if (target.id != null) get(target.id!!) else null
                    }
                    if (element is Specialization && element.general.id == null) {
                        element.general.id = any.elementId
                        element.general.ref = any
                    }
                    if (element is Import) {
                        if (element.importedNamespace.id != null)
                            element.importedNamespace.ref = get(element.importedNamespace.id!!) as Namespace?
                    }
                }
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
    override fun export(): ProjectData {
        checkConsistency(repo.elements.values, checkForNoTransients = false)

        val exportCollection = mutableListOf<ElementDAO>()

        // create a collection for export, leaving out global and any
        repo.elements.values.forEach {
            if (it != global && it != any)
                exportCollection.add(it.toDAO())
        }

        // remove transient elements, from both list of elements *and* list of owned elements.
        val transient = mutableSetOf<UUID>()
        // repo.elements.values.forEach { if (it.isTransient) { transient.add(it.elementId) } }
        exportCollection.removeIf { it.elementId in transient }
        for (element in exportCollection) {
            element.ownedElements.removeIf { it.id in transient }
            element.source?.removeIf { it.id in transient}
            element.target?.removeIf { it.id in transient}
            if (element.owner?.id == global.elementId) element.owner = null
        }
        project.data.clear()
        project.data.addAll(exportCollection.map { Data(payloadElementSnapshot = it) })
        return project
    }

    /**
     * Creates an element owned by an element that is a namespace;
     * if an element with the same elementId is already in the model,
     * the existing element updated, and the updated existing element is returned.
     * @param element The element to be created. It must have a valid identification.
     * @param owner The element in which the property will be created.
     * @return the created property with UId field set.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : Element> create(element: T, owner: Element): T {

        // Must be done at time of creation of elements as UUID are used before
        // If Element is a library element, assign a UUID v5
        //  if (owner is Namespace && owner.isLibraryElement)
        //    element.elementId = Generators.nameBasedGenerator().generate(owner.qualifiedName+"::"+element.qualifiedName)

        // If Element with same elementId already exists in a model --> update the existing element and return it.
        val existingElement = get(element.elementId)
        if (existingElement != null) {
            existingElement.updateFrom(element)
            if (existingElement.updated)
                status.updates[existingElement.elementId] = "updated: '${existingElement.qualifiedName}'"
            return existingElement as T
        }


        // If identification by name exists, it must be unique in the namespace
        if (element.name != null || element.shortName != null) {
            var existingWithSameName: Element? = null
            if (element.name != null)
                existingWithSameName = owner.getOwnedElement(element.name!!)
            if (existingWithSameName == null && element.declaredShortName != null)
                existingWithSameName = owner.getOwnedElement(element.declaredShortName!!)

            if (existingWithSameName != null) {
                // Quite annoying ... -- should be optional for debugging. Not by default.
                // reportInfo(found, "Overwritten '${element.identification.toName()}' with new information.")
                if (existingWithSameName.javaClass != element.javaClass) {
                    throw SysMDError(
                        element = element,
                        message = "'${element.qualifiedName}' changes the type of the element '${existingWithSameName.qualifiedName}; will lead to problems.'; suggestion: reset for complete update.")
                    // delete(existingWithSameName)
                    // return create(element, owner)
                }
                existingWithSameName.updateFrom(element)
                if (existingWithSameName.updated)
                    status.updates[existingWithSameName.elementId] = "updated: '${existingWithSameName.qualifiedName}'"
                return existingWithSameName as T
            }
        }

        // Import must not be duplicate
        if (element is Import) {
            element.model = this
            val imports = owner.getOwnedElementsOfType<Import>()
            val duplicate = imports.find {
                it.importedNamespace.str == element.importedNamespace.str // && it.importedMemberName == it.importedMemberName
            }
            if (duplicate != null ) return element
        }

        // Multiplicity must not be duplicate; just update
        if (element is Multiplicity) {
            element.model = this
            val found = owner.getOwnedElementOfType<Multiplicity>()
            if (found != null) {
                found.updateFrom(element)
                return found as T
            }
        }

        // Specializations and subclasses thereof; updates if a similar specialization exists
        if (element is Specialization) {
            element.model = this
            val foundSpecializations = owner.getOwnedElementsOfType<Specialization>()
            foundSpecializations.forEach { found ->
                if (element.javaClass == found.javaClass && element.general.str == found.general.str) { // && element.general.id == found.general.id) {
                    return found as T
                }
            }
        }

        // Annotation
        if (element is Annotation) {
            val found = owner.getOwnedElementsOfType<Annotation>()
            found.forEach {
                if (it.annotatedElement == element.annotatedElement)
                    return it as T
            }
        }

        // Create a membership in owning namespace
        element.setOwner(owner)
        repo.elements[element.elementId] = element

        return element
    }

    /**
     * Creates or, if an element with the same *name* exists, replaces an element.
     * If an element with the same *elementId* exists, it will be updated.
     * This changes the elementId of the element.
     * @param element the element to be created.
     * @param owner the element that shall become owner.
     * @return the element created.
     */
    override fun <T : Element> createOrReplace(element: T, owner: Element): T {
        val existingElement = owner.getOwnedElement(element.declaredName, element.declaredShortName)
        return if (existingElement == null)
            create(element, owner)
        else {
            delete(existingElement)
            val created = create(element, owner)
            resolveIDs()
            created
        }
    }

    override fun getUnownedElements(): List<Session.UnresolvedElement> = repo.unownedElements

    override fun dropUnownedElement(element: Element) {
        repo.unownedElements.removeIf { it.element === element }
    }

    /**
     * If there is a UUID assigned in a compiled input file by the compiler, and
     * one is already existing, the old UUID must be replaced prior to merging
     * or overwriting the existing element data with the new one and dropping the new UUID.
     * This routine replaces the old UUID with the already-existing one in all not-yet-imported
     * elements.
     * @param unownedElement the elementID of the element that already exists with other UUID
     * @param existingElement the elementId ot the existing element that replaces the old one.
     */
    override fun updateUnownedElements(unownedElement: Element, existingElement: Element) {
        repo.unownedElements.forEach {
            // Replace it in the owner of the unowned element
            if (it.element.owner.id == unownedElement.elementId) {
                it.element.owner.id = existingElement.elementId
                it.element.owner.ref = existingElement
                it.startOfPath=existingElement
                it.path = null
            }

            // Replace it in the start of path ...
            if (it.startOfPath.elementId == unownedElement.elementId) {
                it.startOfPath = existingElement
            }

            // Replace it in relationships that are resolved later ...
            if (it.element is Relationship) {
                (it.element as Relationship).source.forEach { source ->
                    if (source.id == unownedElement.elementId) {
                        source.id = existingElement.elementId
                        source.ref = existingElement
                    }
                }
                (it.element as Relationship).target.forEach { target ->
                    if (target.id == unownedElement.elementId) {
                        target.id = existingElement.elementId
                        target.ref = existingElement
                    }
                }
            }
        }
    }

    /** Just removes the session from the existing sessions */
    override fun endSession() {
        SessionManager.kill(this.id)
    }

    /**
     * Deletes an element with a given reference and all owned elements.
     * Also updates ownedElements of the owner.
     * @param element reference to element to be deleted
     */
    override fun delete(element: Element): Element? {
        if (element in setOf(global, any)) return null
        val owner = element.owner.ref
        val toDelete = mutableListOf<Resolved<Element>>()
        element.ownedElement.forEach { toDelete.add(it) }
        toDelete.forEach {
            delete(it.ref!!)
        }
        owner?.ownedElement?.removeIf {it.id == element.elementId }
        val result = repo.elements.remove(element.elementId)
        checkConsistencyOfBuilders()
        checkConsistency(repo.elements, global.elementId, "remove")
        return  result
    }


    /** Gets all subclasses of a class; requires an initialized model. */
    override fun getSubclasses(element: Element): Collection<Classifier> {
        val result = mutableListOf<Classifier>()

        val relations = getRelationshipsTo(element, "*")
        relations.forEach {
            if (it is Specialization) {
                if (it.target[0].ref === element && it.source[0].ref is Classifier)
                    result.add(it.source[0].ref as Classifier)
            }
        }
        return result
    }

    /**
     * Gets all subtypes of a type; requires an initialized model.
     * @param element type from which subtypes will be searched
     * @return collection of subtypes
     */
    override fun getSubtypes(element: Type): Collection<Type> {
        val result = mutableListOf<Type>()

        val relations = getRelationshipsTo(element, "*")
        relations.forEach {
            if (it is Specialization) {
                if (it.target[0].ref === element && it.source[0].ref is Type)
                    result.add(it.source[0].ref as Type)
            }
        }
        return result
    }


    /**
     * Creates a transient element that will NOT be persisted.
     * It nevertheless must have an id.
     * @param element A property with a user-defined and unique id that will be added to the session cache.
     * @param owner The element to which the property belongs.
     */
    override fun <T: Element> createTransient(element: T, owner: Namespace): T {
        element.isTransient = true

        // If identification by name exists, it must be unique in namespace
        if (element.declaredName != null || element.declaredShortName != null) {
            var found: Element? = null
            if (element.declaredName != null)
                found = owner.getOwnedElement(element.declaredName!!)
            if (found == null && element.declaredShortName != null)
                found = owner.getOwnedElement(element.declaredShortName!!)

            if (found != null) {
                // Quite annoying ... -- should be optional for debugging. Not by default.
                // reportInfo(found, "Overwritten '${element.identification.toName()}' with new information.")
                if (found.javaClass != element.javaClass) {
                    report(element, "'${element.qualifiedName}' cannot change the type of the element '${found.qualifiedName}'; suggestion: reset for complete update.")
                }
                found.updateFrom(element)
                if (found.updated)
                    status.updates[found.elementId] = "updated: '${found.qualifiedName}'"
                @Suppress("UNCHECKED_CAST")
                return found as T
            }
        }

        // Import must not be duplicate
        if (element is Import) {
            element.model = this
            val imports = owner.getOwnedElementsOfType<Import>()
            val duplicate = imports.find {
                it.importedNamespace.str == element.importedNamespace.str // && it.importedMemberName == it.importedMemberName
            }
            @Suppress("UNCHECKED_CAST")
            if (duplicate != null ) return duplicate as T
        }

        // Multiplicity must not be duplicate; just update
        if (element is Multiplicity) {
            element.model = this
            val found = owner.getOwnedElementOfType<Multiplicity>()
            if (found != null) {
                found.updateFrom(found)
                @Suppress("UNCHECKED_CAST")
                return found as T
            }
        }

        if (element is Specialization) {
            element.model = this
            val found = owner.getOwnedElementsOfType<Specialization>().firstOrNull { it::class == element::class }
            if (found != null) {
                found.updateFrom(found)
                @Suppress("UNCHECKED_CAST")
                return found as T
            }
        }

        element.model = this
        repo.elements[element.elementId] = element
        element.owner.ref = owner
        element.owner.id = owner.elementId
        owner.ownedElement.add(Resolved(element))
        element.isTransient = true

        return element
    }

    /**
     * Adds an element to the unowned elements, without checks.
     * @param element element for which the owner-relationship is only defined by a qualified name
     * @param path the qualified name of the owner, starting from startOfPath
     * @param startOfOwnerPath the element where the qualified-name-like path starts
     */
    override fun addUnownedElement(element: Element, path: String?, startOfOwnerPath: Element) {
        repo.unownedElements.add(Session.UnresolvedElement(startOfPath=startOfOwnerPath, path=path, element=element))
    }


    /** parses a line on which the unary operator + is applied; DSL for testing purpose */
    override operator fun String.unaryPlus() =
        loadSysMD(this)

    override fun toString(): String {
        return "Session { ${project.name}, $status }"
    }
}

/**
 * Returns a list of all elements of Type T
 * @param T type for which elements will be filtered.
 */
inline fun <reified T: Element> Session.getAllOfClass(): List<T> =
     get().filterIsInstance<T>().toMutableList()
