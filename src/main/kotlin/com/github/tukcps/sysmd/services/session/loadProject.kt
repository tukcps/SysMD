package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import io.github.tukcps.sysmlv2.api.entities.ElementDAO

/**
 * Loads a complete project usage from the repository into the session.
 * The project is first searched in the repository; if not there, in
 * the local files.
 * The method imports *all* elements into the session.
 * @param projectName Name of the project that will be imported.
 * @param maxRunlevel Max runlevel for which initialize is done.
 */
fun ProjectSession.loadProject(
    projectName: String,
    maxRunlevel: Runlevel = Runlevel.NAMES_RESOLVED,
) {

    val loaded = SessionManager.projectService.getProjects().firstOrNull { it.name == projectName }?: return

    val elementData: Collection<ElementDAO> =
        loaded.data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
    import(elementData)
    initialize(Runlevel.minRunlevel(settings.runlevel, maxRunlevel))
}