package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import io.github.tukcps.sysmlv2.api.entities.ElementDAO

/**
 * Loads a complete project usage from the repository into the session.
 * The project is first searched in the repository; if not there, in
 * the local files.
 * The method imports *all* elements into the session.
 * @param projectName Name of the project that will be imported.
 * @param initialize whether to also call initialize.
 * @param setProject whether to set the project metadata based on YAML cell
 */
fun Session.loadProject(
    projectName: String,
    initialize: Boolean = true,
    setProject: Boolean = true
) {

    val loaded = SessionManager.projectService.getProjects().firstOrNull { it.name == projectName }?: return

    val elementData: Collection<ElementDAO> =
        loaded.data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }

    import(elementData)
    if (setProject) {
        project = ProjectData(loaded)
    }
    // identify Qualified Names, etc.
    if (settings.initialize && initialize)
        initialize(1)
}