@file:Suppress("unused")
// Usage is also in backend.

package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.services.repositories.local.*
import io.github.tukcps.sysmlv2.api.services.ElementNavigationService
import io.github.tukcps.sysmlv2.api.services.ProjectDataVersioningService
import io.github.tukcps.sysmlv2.api.services.ProjectUsageService
import java.util.*

/**
 * The session manager keeps a list of all open sessions.
 * Each session allows exclusively editing a commit; no other sessions can be opened with this commit.
 * A session can be persisted or retrieved in a repository (commit).
 */
object SessionManager {

    /**
     * SysMD's implementation of the standard project service.
     */
    var projectService: SysMDProjectService = SysMDProjectService()
    var elementNavigationService: ElementNavigationService = SysMDElementNavigationService
    var projectUsageService: ProjectUsageService = SysMDProjectUsageService
    var projectDataVersioningService: ProjectDataVersioningService = SysMDProjectDataVersioningService

    /**
     * A map with all active sessions; hashmap of UId of commits and sessions working with it.
     */
    val sessions = hashMapOf<UUID, Session>()

    @Suppress("unused")
    fun getSession(id: UUID) = sessions[id]

    /**
     * Gets all collections and puts its ids in a collection.
     * @return a collection of all session's ids.
     */
    fun getAllSessions(): Collection<Session> = sessions.values

    /**
     * Starts a session; a session is for exclusive use by a single client.
     * @return SessionImplementation object
     */
    fun startSession(id: UUID = UUID.randomUUID()): Session = SessionImplementation(
        id = id,
    ).run {
        sessions[id] = this
        return this
    }


    /**
     * Starts a session and links it with a project.
     * @param project the project that will be run in the session by loadProject
     * @return the crated session
     */
    fun startSession(project: ProjectData): Session {
        val session = SessionImplementation(project = project)
        sessions[session.id] = session
        return session
    }

    /**
     * Executes a lambda 'block' in the context of a session.
     * @param id UUID of the session
     * @param block lambda that is executed in the context of the session
     */
    @Suppress("unused")
    fun<T> runInSession(id: UUID, block: Session.() -> T): T {
        val session = sessions[id] ?: throw Exception("No such session")
        session.run { return block() }
    }

    /**
     * runs a lambda in a session; no id, no persistence after the session ends.
     */
    inline fun runInSession(toRun: Session.() -> Unit) = SessionImplementation().run {
        toRun()
    }


    /**
     * Removes a mapping from the sessions and returns the entry or null
     * if the id was not in the keys.
     * @param id UId of the session.
     */
    fun kill(id: UUID) = sessions.remove(id)
}
