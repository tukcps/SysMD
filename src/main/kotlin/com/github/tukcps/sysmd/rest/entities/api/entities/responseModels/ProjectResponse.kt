package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels


import com.github.tukcps.sysmd.model.datamodel.IdentifiedImplementation
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.rest.entities.api.entities.Identity
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * The Project response data transfer object.
 *
 * Modeled as an immutable data class to guarantee thread-safety and prevent side effects.
 */
@Serializable
data class ProjectResponse(
    @SerialName("@id")
    override var id: Uuid = Uuid.random(),

    @SerialName("@type")
    val type: String = "Project",

    val name: String? = null,
    val description: String? = null,

    // Provide the default implementation as a fallback directly in the primary constructor.
    // This ensures kotlinx.serialization uses it correctly if the field is missing in JSON.
    val defaultBranch: Identified? = IdentifiedImplementation(),

    val created: Instant? = null
) : Identity {

    companion object {
        /**
         * Robust factory method to map a core [Project] domain entity into a [ProjectResponse].
         * This replaces the error-prone secondary constructor.
         */
        fun from(project: Project): ProjectResponse = ProjectResponse(
            id = project.id,
            name = project.name,
            description = project.description,
            created = project.created,
            defaultBranch = IdentifiedImplementation()
        )
    }
}
