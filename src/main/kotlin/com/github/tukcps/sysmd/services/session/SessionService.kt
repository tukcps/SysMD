package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.rest.entities.interchange.Meta
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/**
 * The SessionService provides an API for controlling one or more Sessions in which
 * SysML or KerML models, and MD documents of a Project are edited, compiled, etc.
 * A Session always has a Project.
 */
@OptIn(ExperimentalUuidApi::class)
interface SessionService {

    /**
     * @return A list with all available Sessions.
     */
    fun getAllSessions(): Collection<ProjectSession>

    /**
     * @return A session by its id.
     */
    fun getSession(id: Uuid): ProjectSession?

    /**
     * Opens a new session for working with a project.
     * @param project An existing project.
     * @return The session created for working with the project.
     */
    fun createSession(project: ProjectData): ProjectSession

    /**
     * Returns the .meta.json, i.e., in particular the index of the source-files of the project.
     * @param session the session with the project
     * @return keys and values of the project's index
     */
    fun getMeta(session: Uuid): Meta?

    /**
     * Creates (or replaces) the .meta.json.
     * @param session The session with the project.
     * @param meta .meta with Key and values of the project's index.
     */
    fun putMeta(session: Uuid, meta: Meta): Boolean

    /**
     * Gets the cells for a notebook for editing the code and doc of a project.
     * @param session The session, from which the data will be taken.
     * @param tabName The name of the tab.
     * @return A List of cells with Doc or Rep Elements for Notebook UI
     */
    fun getCells(session: Uuid, tabName: String): List<ElementData>?

    /**
     * Gets the cells for a notebook for editing the code and doc of a project.
     * @param session The session, from which the data will be taken.
     * @return A Map tab-name --> List of cells with Doc or Rep Elements for Notebook UI
     */
    fun getCells(session: Uuid): LinkedHashMap<String, List<ElementData>>?

    /**
     * Updates the cells for a notebook for editing the code and doc of a project.
     * If an element is Null, the existing ElementData in the repository is not changed.
     * @param session The session with the project to be edited.
     * @param tabName The name of the tab/file.
     * @param cells A map  Doc or Rep Elements -> tabName for the Notebook.
     * @return The cells after updates.
     */
    fun putCells(session: Uuid, tabName: String, cells: List<ElementData>): Boolean

    /**
     * Gets supplementary files of the project, e.g., pictures or whatever.
     * NOT the code files that are in the index.
     * @return A list with all file names.
     */
    fun getFiles(session: Uuid): List<String>?

    /**
     * Gets content from the backend, e.g., a picture.
     * @param session id of the session.
     * @param name Name of the project's file, with extension.
     * @return A byte array with the file's content; null, if not existing.
     */
    fun getFile(session: Uuid, name: String): ByteArray?

    /**
     * Gets an icon of a project without considering the session.
     * @param projectId The ID of the project
     * @return A ByteArray with the file contents.
     */
    fun getIcon(projectId: Uuid): ByteArray?

    /**
     * Writes the content in the backend, e.g., a file of database.
     * @param session ID of the session.
     * @param name Name of the project's file, with extension.
     * @param content A byte array with the file's content.
     * @return True, if written.
     */
    fun putFile(session: Uuid, name: String, content: ByteArray): Boolean

    /**
     * @param fileName The name of the file to be deleted.
     * @return True, if deleted.
     */
    fun deleteFile(session: Uuid, fileName: String): Boolean

    /**
     * Updates a new model in abstract representation by a given piece of code.
     * The update is made by running the compiler and further actions defined by runlevel.
     * @param session The session with the project.
     * @param code The code from which the abstract representation will be created.
     * @param language Either SysML or KerML.
     * @param namespace The namespace that will be updated by adding the compiled code; null for Global.
     * @return The session status that eventually contains error messages.
     */
    fun updateModel(session: Uuid, code: String, language: Language, namespace: String?, runlevel: Runlevel): SessionStatus?

    /**
     * Gets all elements of the session's abstract representation model.
     * @param session The session with the model.
     */
    fun getAllElements(session: Uuid): List<ElementData>?

    /**
     * Gets a for a list of IDs the respective elements as a map id -> ElementData
     * @param session UI of the session. Null is returned if the session is not found.
     * @param byId IDs of the elements to be retrieved.
     * @return A map of element ID -> ElementData, or null if there is no such ID.
     */
    fun getElementsByUuid(session: Uuid, byId: List<Uuid>): Map<Uuid, ElementData?>?

    /**
     * Gets a for a namespace qualified name and a qualified name the respective elements as a map id -> ElementData
     * @param session UI of the session. Null is returned if the session is not found.
     * @param namespace The namespace where name resolution starts.
     * @param qualifiedName The qualified name that is resolved in the namespace.
     * @return The element data (or null, if not found), and the session status (or null, if the session was not found)
     */

    fun getElementsByQualifiedName(session: Uuid, namespace: QualifiedName, qualifiedName: QualifiedName): Triple<ElementData?, ElementData?, SessionStatus>?
    /**
     * Returns all elements that are owned by an element of the abstract representation.
     * @param session The session with the project.
     * @param owner The element, by its id, in which owned elements will be searched.
     * @return The owner's owned elements.
     */
    fun getOwnedElements(session: Uuid, owner: Uuid): List<ElementData>?

    /**
     * Gets all subtypes of a type.
     * @param session Uuid of the session.
     * @param type: Uuid of the type.
     */
    fun getSubtypes(session: Uuid, type: Uuid): List<ElementData>?

    /**
     * Gets all variables of the solver.
     * Before, an update of the model is needed by updateModel
     * and a suitable runlevel at least VARIABLES.
     * @param session Uuid of the session.
     */
    fun getVariables(session: Uuid): List<Variable>?

}