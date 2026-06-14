package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels


import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.rest.entities.api.entities.Identity
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * The Project response.
 */
@Serializable
class ProjectResponse(
    @SerialName("@id")
    override var id: Uuid,
    @SerialName("@type")
    var type: String = "Project",
    val name: String? = null,
    var description: String? = null,
    var defaultBranch: Identified? = null,
    var created: Instant? = null
): Identity {

    constructor(project: Project): this(
        id = project.id,
        name = project.name,
        defaultBranch = TODO(),
        description = project.description,
        created = project.created
    )
}