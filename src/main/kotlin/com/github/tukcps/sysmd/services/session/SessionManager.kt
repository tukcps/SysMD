package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.loadDefaultProjects
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmlv2.api.ElementNavigationService
import com.github.tukcps.sysmlv2.api.ProjectDataVersioningService
import com.github.tukcps.sysmlv2.api.ProjectService
import com.github.tukcps.sysmlv2.api.ProjectUsageService
import java.util.*

/**
 * The session manager keeps a list of all open sessions.
 * Each session allows exclusively editing a commit; no other sessions can be opened with this commit.
 * A session can be persisted or retrieved in a repository (commit).
 */
object SessionManager {

    /**
     * The repository in which data will be persisted.
     * Dependency injection, e.g., in Spring can create own repository;
     * then, this reference is overwritten by a more sophisticated service.
     */
    var projectService: ProjectService = SysMDProjectService
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
    @Suppress("unused")
    fun getAllSessions(): Collection<UUID> {
        val response = mutableListOf<UUID>()
        sessions.forEach {
            response.add(it.key)
        }
        return response
    }

    /**
     * Starts a session; a session is for exclusive use by a single client.
     * @return SessionImplementation object
     */
    fun startSession(): Session = SessionImplementation().run {
        sessions[id] = this
        loadDefaultProjects()
        return this
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
     * Starts a session, executes a lambda for testing purposes.
     * Just a shortcut for testing; don't use it for production code!!
     * The session is terminated after the test has run.
     * The parameters permit setting up test cases:
     * @param usages projects that are loaded into the test session prior to the test.
     * @param catchExceptions allows disabling catching exceptions in the parser and other methods.
     * @param loadKerML whether to create the KerML library classes or to have a completely bare model for test.
     * @param initialize whether to run initialize or not.
     * @param test a lambda with the test.
     */
    inline fun testSession(vararg usages: String,
                           catchExceptions: Boolean = true,
                           initialize: Boolean = true,
                           loadKerML: Boolean = true,
                           test: SessionImplementation.() -> Unit
    ) = SessionImplementation(loadKerML=loadKerML).run {
        project = ProjectData(name = "testSession")
        sessions[id] = this
        settings.catchExceptions = catchExceptions
        settings.initialize = initialize
        usages.forEach { loadProject(it) }
        test()
        sessions.remove(id)
        settings.initialize = initialize
    }


    /**
     * runs a lambda in a session; no id, no persistence after the session ends.
     */
    inline fun runInSession(toRun: Session.() -> Unit) = SessionImplementation().run {
        toRun()
    }


    /**
     * writes the session data into the DB and thereby creates a new revision
     * @param session a Session
     * @param projectId the id of the owning project
     * @param description the description of the commit
     * @return the UUID of the created commit
     */
    fun commitSession(session: Session, projectId: UUID, name: String = "", description: String = ""): UUID {
        return projectDataVersioningService.commit(
            name = name,
            owningProject = projectId,
            description = description,
            payload = session.export().data,
            previousCommit = null,
        )
    }

    /**
     * Removes a mapping from the sessions and returns the entry or null
     * if the id was not in the keys.
     * @param id UId of the session.
     */
    fun kill(id: UUID) = sessions.remove(id)
}
