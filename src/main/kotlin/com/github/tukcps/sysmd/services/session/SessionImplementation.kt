package com.github.tukcps.sysmd.services.session

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.cspsolver.DiscreteSolver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.getRelationshipsTo
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmd.services.resolve.resolve
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.*


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
        it.owner.ref = null
        it.model = this
        it.elementId = Generators.nameBasedGenerator().generate("Global")
    }

    /**
     * We have a single element "Any" that is the root of the inheritance tree.
     * It can be accessed by the reference anyElement.
     */
    override val anything = Anything(model = this).also {
        it.elementId = Generators.nameBasedGenerator().generate("Base::Anything")
        it.owner = Resolved(global)
    }

    /**
     * Loads all KerML libraries into the model.
     */
    private fun loadLibraries() {
        libraries.forEach {
            loadLibrary(it)
        }
    }

    /** We set up initial libraries of KerML */
    init {
        // Hence, one cannot create them via the regular API.
        repo.elements[global.elementId!!] = global
        val baseLibrary = PackageImplementation(declaredName = "Base", isStandard = true, isLibraryElement = true)
        create(baseLibrary, global)
        create(anything, baseLibrary)
        require(global === this[global.elementId!!])
        require(anything.owner.ref == baseLibrary)

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
            global.ownedElement.clear()
            anything.subtypes.clear()
            repo.elements[global.elementId!!] = global
            status.reset()

            // Now everything should be clean
            builder = DDBuilder()
            val baseLibrary = PackageImplementation(declaredName = "Base", isStandard = true)
            create(baseLibrary, global)
            create(anything, baseLibrary)
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


    override fun get(): Collection<Element> = repo.elements.values
    override fun get(elementId: UUID): Element? = repo.elements[elementId]

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
            // set the model to this
            element.model = this

            // resolve the owner
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
                    status.fatal("could not resolve owned element id of ${element.elementType} ${element.qualifiedName}")
            }

            // resolve ID to reference relationship's sources and targets
            when(element) {
                is Relationship -> {
                    element.source.forEach { source ->
                        source.ref = if (source.id != null) get(source.id!!) else null
                    }
                    element.target.forEach { target ->
                        target.ref = if (target.id != null) get(target.id!!) else null
                    }
                    if (element is Specialization && element.general.id == null) {
                        element.general.id = anything.elementId
                        element.general.ref = anything
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
    override fun export(): Collection<CommitDataObject> {
        checkConsistency(repo.elements.values, checkForNoTransients = false)

        val exportCollection = mutableListOf<ElementDAO>()

        // create a collection for export, leaving out global and any
        repo.elements.values.forEach {
            if (it != global && it != anything)
                exportCollection.add(it.toDAO())
        }

        // remove transient elements from both list of elements *and* list of owned elements.
        val transient = mutableSetOf<UUID>()
        // repo.elements.values.forEach { if (it.isTransient) { transient.add(it.elementId) } }
        exportCollection.removeIf { it.elementId in transient }
        for (element in exportCollection) {
            element.ownedElement.removeIf { it.id in transient }
            element.source?.removeIf { it.id in transient}
            element.target?.removeIf { it.id in transient}
            if (element.owner?.id == global.elementId) element.owner = null
        }
        return exportCollection.map { Data(payloadElementSnapshot = it) }
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

        element.model = this

        // Must be done at the time of creation of elements as UUID are used before
        // If Element is a library element, assign a UUID v5
        //  if (owner is Namespace && owner.isLibraryElement)
        //    element.elementId = Generators.nameBasedGenerator().generate(owner.qualifiedName+"::"+element.qualifiedName)
        // If Element with the same elementId already exists in a model: update the existing element and return it.
        if (element.elementId == null) {
            if (!owner.isStandard && !owner.isLibraryElement && !element.isLibraryElement) {
                element.elementId = UUID.randomUUID()
            } else {
                val p = owner.path()
                val e = if (p.isNotEmpty())
                    "::${element.escapedName()?:owner.ownedElement.size.toString()}"
                else
                    element.escapedName()?:owner.ownedElement.size.toString()
                element.elementId = Generators.nameBasedGenerator().generate(p+e)
            }
        }

        val existingElement = get(element.elementId!!)
        if (existingElement != null) {
            existingElement.updateFrom(element)
            if (existingElement.updated)
                status.updatedValues[existingElement.elementId!!] = "updated: '${existingElement.qualifiedName}'"
            element.ownedElement.forEach { newOwned ->
                var ownedInExisting = false
                existingElement.ownedElement.forEach { existingOwned ->
                    if (existingOwned.id == newOwned.id) {
                        ownedInExisting = true
                    }
                }
                if (!ownedInExisting)
                    existingElement.ownedElement.add(newOwned)
            }
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
                }
                existingWithSameName.updateFrom(element)
                if (existingWithSameName.updated)
                    status.updatedValues[existingWithSameName.elementId!!] = "updated: '${existingWithSameName.qualifiedName}'"
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

        // Multiplicity must not be duplicate; update
        if (element is Multiplicity) {
            element.model = this
            val found = owner.getOwnedElementOfType<Multiplicity>()
            if (found != null) {
                found.updateFrom(element)
                return found as T
            }
        }

        // Specializations and subclasses thereof; updates if a similar specialization exists
        // As several specialization elements might exist, we check if there are some that are similar, i.e.
        // - have the same qualified name for the general class
        // - have the same id for the general class.
        if (element is Specialization) {
            element.model = this
            val foundSpecializations = owner.getOwnedElementsOfType<Specialization>()
            foundSpecializations.forEach { found ->
                if ( element.javaClass == found.javaClass // must be the same kind ...
                    &&  (( (element.general.str == found.general.str ) && element.general.str != null )     // string is equal before name resolution
                            || ((element.general.id == found.general.id) && element.general.id != null ) )  // id is equal after name resolution/loading
                )
                    return found as T
                if ( element.general.str == "Base::Anything" && foundSpecializations.first().javaClass == element.javaClass)
                    return foundSpecializations.first() as T
            }
        }

        // Annotation
        if (element is Annotation) {
            val found = owner.getOwnedElementsOfType<Annotation>()
            found.forEach {
                if ( (it.annotatedElement.str != null && it.annotatedElement.str == element.annotatedElement.str)
                    || (it.annotatedElement.ref != null) && (it.annotatedElement.ref?.escapedName() == element.annotatedElement.ref?.escapedName())
                    || (it.annotatedElement.id != null && (it.annotatedElement.id == it.annotatedElement.id))
                )
                    return it as T
            }
        }

        // Create a membership in owning namespace
        element.setOwner(owner)
        repo.elements[element.elementId!!] = element

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
     * If there is a UUID assigned in a compiled input file by the compiler,
     * and one is already existing, the old UUID must be replaced.
     * This must be done before merging or overwriting the existing element data with
     * the new one and dropping the new UUID.
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

            // Replace it at the start of the path ...
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
     * @param element reference to the element to be deleted
     */
    override fun delete(element: Element): Element? {
        if (element in setOf(global, anything)) return null
        val owner = element.owner.ref
        val toDelete = mutableListOf<Resolved<Element>>()
        element.ownedElement.forEach { toDelete.add(it) }
        toDelete.forEach {
            delete(it.ref!!)
        }
        owner?.ownedElement?.removeIf {it.id == element.elementId }
        val result = repo.elements.remove(element.elementId)
        checkConsistencyOfBuilders()
        checkConsistency(repo.elements, global.elementId!!, "remove")
        return  result
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
     * Adds an element to the unowned elements, without checks.
     * @param element element for which the owner-relationship is only defined by a qualified name
     * @param path the qualified name of the owner, starting from startOfPath
     * @param startOfOwnerPath the element where the qualified-name-like path starts
     */
    override fun addUnownedElement(element: Element, path: String?, startOfOwnerPath: Element) {
        repo.unownedElements.add(Session.UnresolvedElement(startOfPath=startOfOwnerPath, path=path, element=element))
    }

    override fun getNumberOfOwnedElements(path: String): Int {
        val number =  repo.unownedElements.filter {
            val start = if (it.startOfPath != global) it.startOfPath.escapedName() else ""
            val tail  = it.path
            val gen =
                if (start.isNullOrBlank()) tail else if (tail.isNullOrBlank()) start else "$start::$tail"
            gen == path
        }.size
        return number
    }

    override fun toString(): String {
        return "Session { project=${project?.name}, $status }"
    }

    /**
     * Returns a list of all variables of Type Feature that have a variable.
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
