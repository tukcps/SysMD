@file:Suppress("unused")
// Usage is also in backend.

package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.rest.entities.api.services.ElementNavigationService
import com.github.tukcps.sysmd.rest.entities.api.services.ProjectDataVersioningService
import com.github.tukcps.sysmd.rest.entities.api.services.ProjectUsageService
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmd.services.session.implementation.ProjectSessionImplementation
import com.github.tukcps.sysmd.services.session.implementation.SessionServiceImplementation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The session manager keeps a list of all open sessions.
 * Each session allows exclusively editing a commit; no other sessions can be opened with this commit.
 * A session can be persisted or retrieved in a repository (commit).
 */
@OptIn(ExperimentalUuidApi::class)
object SessionManager {

    /**
     * SysMD's implementation of the standard project service.
     */
    var projectService: SysMDProjectService = SysMDProjectService()
    var elementNavigationService: ElementNavigationService = SysMDElementNavigationService
    var projectUsageService: ProjectUsageService = SysMDProjectUsageService
    var projectDataVersioningService: ProjectDataVersioningService = SysMDProjectDataVersioningService
    var sessionService: SessionService = SessionServiceImplementation()

    /**
     * A map with all active sessions; hashmap of UId of commits and sessions working with it.
     */
    private val sessions = hashMapOf<Uuid, ProjectSession>()

    fun getSession(id: Uuid) = sessions[id]

    /**
     * Gets all collections and puts its ids in a collection.
     * @return a collection of all session's ids.
     */
    fun getAllSessions(): Collection<ProjectSession> = sessions.values

    /**
     * Starts a session; a session is for exclusive use by a single client.
     * @param id a UUID that is given by the caller; default a random UUID
     * @return Session with either a random UUID or a UUID given as parameter.
     */
    @Deprecated("A session must always be associated with a Project. A simple session can be created by SesssionImplementation()")
    fun createSession(id: Uuid = Uuid.random()): Session = TODO()

    /**
     * Starts a session and links it with a project.
     * @param project the project that will be run in the session
     * @param libraries libraries that are to be loaded
     * @param runlevel whether and how far to compile/run the project
     * @return the crated session
     */
    fun createSession(
        project: ProjectData,
        vararg libraries: String,
        runlevel: Runlevel = Runlevel.NAMES_RESOLVED,
    ): ProjectSession {
        val session = ProjectSessionImplementation(
            project = project,
            libraries = libraries,
            runlevel = runlevel
        )
        sessions[session.id] = session
        return session
    }


    /**
     * Removes a mapping from the sessions and returns the entry or null
     * if the id was not in the keys.
     * @param id UId of the session.
     */
    fun kill(id: Uuid) = sessions.remove(id)

    /** Libraries for KerML sessions */
    val KERML_LIBRARIES = listOf("Base", "ScalarValues", "Ranges", "ISQ", "Quantities",
        "Objects", "Links", "Occurrences", "Performances").toTypedArray()

    /** Libraries for SysML (and KerML) sessions */
    val SYSML_LIBRARIES = KERML_LIBRARIES + listOf("Items", "Ports", "Parts", "Actions", "Calculations",
        "Attributes", "Constraints", "Requirements", "Interfaces", "States", "Connections", "Signals",
        "VerificationCases").toTypedArray()

}
