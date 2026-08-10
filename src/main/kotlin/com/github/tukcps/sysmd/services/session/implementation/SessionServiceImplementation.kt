package com.github.tukcps.sysmd.services.session.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.rest.entities.interchange.InterchangeProject
import com.github.tukcps.sysmd.rest.entities.interchange.Meta
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmd.services.session.ProjectSession
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionService
import com.github.tukcps.sysmd.services.session.SessionStatus
import com.github.tukcps.sysmd.ui.listChildNames
import com.github.tukcps.sysmd.ui.readBytes
import com.github.tukcps.sysmd.ui.writeBytes
import com.github.tukcps.sysmd.ui.writeText
import com.github.tukcps.sysmd.services.util.JsonSupport
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class SessionServiceImplementation : SessionService {

    /**
     * @return A collection with all available Sessions.
     */
    override fun getAllSessions(): Collection<ProjectSession> =
        SessionManager.getAllSessions()

    /**
     * @return A session by its id.
     */
    override fun getSession(id: Uuid): ProjectSession? =
        SessionManager.getSession(id)

    /**
     * Opens a new session for working with a project.
     * @param project An existing project.
     * @return The session created for working with the project.
     */
    override fun createSession(project: ProjectData): ProjectSession =
        SessionManager.createSession(project = project)

    /**
     * Returns the .meta.json, i.e., in particular the index of the source-files of the project.
     * @param session the session with the project
     * @return keys and values of the project's index
     */
    override fun getMeta(session: Uuid): Meta? {
        val session = getSession(session)
        val project = session?.project
        return project?.meta
    }

    /**
     * Creates (or replaces) the .meta.json
     * @param session The session with the project.
     * @param meta .meta with Key and values of the project's index.
     */
    override fun putMeta(
        session: Uuid,
        meta: Meta
    ): Boolean {

        // Set it in current session
        val session = getSession(session)
        val project = session?.project
        val directory = project?.directory
        project?.meta = meta

        // Persist it in files
        try {
            if (directory != null) {
                SystemFileSystem.createDirectories(directory)
                val jsonForProject = JsonSupport.json.encodeToString(
                    InterchangeProject(project.project as InterchangeProject).also { it.id = project.id }
                )
                val projectJson = Path(directory, ".project.json")
                projectJson.writeText(jsonForProject)

                val metaFileJson = Path(directory, ".meta.json")
                val jsonForMeta = JsonSupport.json.encodeToString(meta)
                metaFileJson.writeText(jsonForMeta)
                logger.info("Project '${project.name}' saved to interchange files.")
            } else
                logger.error("Project '${project?.name}' has no interchange-project directory: .meta.json and .project.json not saved")
        } catch (e: Exception) {
            logger.error("Error saving project '${project?.name}'", e)
        }
        return project != null
    }

    /**
     * Gets the cells for a notebook for editing the code and doc of a project.
     * @return A map Path (of e.g., file) -> Doc or Rep Elements for Notebook
     */
    override fun getCells(session: Uuid, tabName: String): List<ElementData>? =
        SessionManager.getSession(session)?.project?.getCells(tabName)

    /**
     * Gets the cells for a notebook for editing the code and doc of a project.
     * @param session The session, from which the data will be taken.
     * @return A Map tab-name --> List of cells with Doc or Rep Elements for Notebook UI
     */
    override fun getCells(session: Uuid): LinkedHashMap<String, List<ElementData>>? =
        SessionManager.getSession(session)?.project?.getCells()

    /**
     * Puts the cells for a notebook for editing the code and doc of a project.
     * @param session The session id with the project to be edited.
     * @param cells A map Path (of e.g., file) -> Doc or Rep Elements for Notebook.
     * @return true, if ok.
     */
    override fun putCells(session: Uuid, tabName: String, cells: List<ElementData>): Boolean {
        val project = getSession(session)?.project
        if (project is ProjectData) {
            val markdown = toMarkdownString(cells)
            val path = project.directory?.let { Path(it, tabName) }
            path?.writeText(markdown)
            return true
        } else {
            logger.error("Failed to save cells. ")
            return false
        }
    }


    /**
     * Gets supplementary files of the project, e.g., pictures or whatever.
     * NOT the code files that are in the index.
     * @return A list with all file names.
     */
    override fun getFiles(session: Uuid): List<String>? {
        val session = SessionManager.getSession(session)
        val project = session?.project
        val files = project?.directory?.let { Path(it, "Files") }
        return files?.listChildNames()
    }

    /**
     * @return A byte array with the file's content; null, if not existing.
     */
    override fun getFile(session: Uuid, name: String): ByteArray? {
        val session = SessionManager.getSession(session)
        val imagePath = session?.project?.directory?.let { Path(it, "Files", name) }
        return imagePath?.readBytes()
    }

    /**
     * Gets an icon of a project without considering the session.
     * @param projectId The ID of the project
     * @return A ByteArray with the file contents.
     */
    override fun getIcon(projectId: Uuid): ByteArray? {
        val projectDir = SessionManager.projectService.getProjectById(projectId)?.directory
        val imagePath = projectDir?.let { Path(projectDir, "Files", "icon.png") }
        return imagePath?.readBytes()
    }

    /**
     * @param session
     * @param content A byte array with the file's content.
     * @return True, if written.
     */
    override fun putFile(
        session: Uuid,
        name: String,
        content: ByteArray
    ): Boolean {
        try {
            val session = SessionManager.getSession(session)
            val filesDirectory = session?.project?.directory?.let { Path(it, "Files") }

            // Create directory if it doesn't exist
            if (filesDirectory == null || !SystemFileSystem.exists(filesDirectory)) {
                SystemFileSystem.createDirectories(filesDirectory!!)
            }

            // Use original filename or fallback
            Path(filesDirectory, name).writeBytes(content)
            return true
        } catch (e: Exception) {
            logger.error("Failed to save file $name", e)
            return false
        }
    }

    /**
     * @param fileName The name of the file to be deleted.
     * @return True, if deleted.
     */
    override fun deleteFile(
        session: Uuid,
        fileName: String
    ): Boolean {
        val session = SessionManager.getSession(session)
        val file = session?.project?.directory?.let { Path(it, "Files", fileName) }
        if (file != null) {
            SystemFileSystem.delete(file, mustExist = false)
            return true
        } else {
            return false
        }
    }

    /**
     * Updates a new model in abstract representation by a given piece of code.
     * @param sessionId The session with the project.
     * @param code The code from which the abstract representation will be created.
     * @param language Either SysML or KerML.
     * @param namespace The namespace to which the elements generated are added.
     * @return The session status that eventually contains error messages, or null if session not found.
     */
    override fun updateModel(
        sessionId: Uuid,
        code: String,
        language: Language,
        namespace: String?,
        runlevel: Runlevel
    ): SessionStatus? {
        val session = SessionManager.getSession(sessionId) ?: return null

        with (session) {
            val elements = when (language) {
                Language.SYS_MD -> SysMD(this).parse(code, namespace)
                Language.KerML  -> KerML(this).parse(code, namespace)
                Language.SYS_ML -> SysMLv2(this).parse(code, namespace)
                else            -> { this.status.error("Unexpected language: $language"); emptyList() }
            }
            import(elements, namespace)
            initialize(runlevel)
            return status
        }
    }

    /**
     * Gets all elements of the session's abstract representation model.
     * @param session The session with the model.
     */
    override fun getAllElements(session: Uuid): List<ElementData>? =
        SessionManager.getSession(session)?.get()?.map { it.toElementData() }

    /**
     * Gets a for a list of IDs the respective elements as a map id -> ElementData.
     * @param session UI of the session. Null is returned if the session is not found.
     * @param byId IDs of the elements to be retrieved.
     * @return A map of element ID -> ElementData, or null if there is no such ID.
     */
    override fun getElementsByUuid(
        session: Uuid,
        byId: List<Uuid>
    ): Map<Uuid, ElementData?>? {
        val session = getSession(session) ?: return null
        val result = HashMap<Uuid, ElementData?>()
        for (id in byId) {
            result[id] = session[id]?.toElementData()
        }
        return result
    }

    /**
     * Gets a for a namespace qualified name and a qualified name the respective elements as a map id -> ElementData
     * @param session UI of the session. Null is returned if the session is not found.
     * @param namespace The namespace where name resolution starts.
     * @param qualifiedName The qualified name that is resolved in the namespace.
     * @return The element data membership (or null, if not found), the owned member, and the session status (or null, if the session was not found)
     */
    override fun getElementsByQualifiedName(session: Uuid, namespace: QualifiedName, qualifiedName: QualifiedName): Triple<ElementData?, ElementData?, SessionStatus>? {
        val session = getSession(session) ?: return null
        val namespace = session.global.resolve(namespace) as? Namespace
        val membership = namespace?.resolve(qualifiedName) as OwningMembership?
        return Triple(membership?.toElementData(), membership?.ownedMemberElement?.toElementData(), session.status)
    }

    /**
     * Returns all elements that are owned by an element of the abstract representation.
     * @param session The session with the project.
     * @param owner The element in which owned elements will be searched.
     * @return The owner's owned elements.
     */
    override fun getOwnedElements(
        session: Uuid,
        owner: Uuid
    ): List<ElementData>? {
        val session = SessionManager.getSession(session)
        val owner = session?.get(owner)
        return owner?.ownedElement?.map { it.toElementData() }
    }

    /**
     * Gets all subtypes of a type.
     * @param session Uuid of the session.
     * @param type: Uuid of the type.
     */
    override fun getSubtypes(
        session: Uuid,
        type: Uuid
    ): List<ElementData>? {
        val session = SessionManager.getSession(session)
        val generic = session?.get(type)
        return if (generic is Type) {
            generic.subtypes.map { it.toElementData() }
        } else
            null
    }

    /**
     * Gets all variables of the solver.
     * Before, an update of the model is needed by updateModel
     * and a suitable runlevel at least VARIABLES.
     * @param session Uuid of the session.
     */
    override fun getVariables(session: Uuid): List<Variable>? {
        SessionManager.getSession(session)?.run {
            return solver.getVariables()
        }
        return null
    }
}