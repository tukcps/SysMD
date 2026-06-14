package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Request for a project.
 * @param name name of the project that must not be empty.
 * @param description String that describes the project.
 * @param defaultBranchName branch name, by default 'default'.
 */
@Serializable
data class ProjectRequest(
    @SerialName("name")
    val name: String,
    val description: String? = null,
    val defaultBranchName: String? = "default",
)