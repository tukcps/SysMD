package com.github.tukcps.sysmd.rest.entities.interchange

import io.github.tukcps.sysmlv2.interchange.InterchangeProjectUsage
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


/**
 * Interchange project file information following the SysML v2 standard.
 * To permit some interoperability of InterchangeProject and Project via API,
 * it has a UUID that shall be equal to the id via the API.
 * @param name The name of the project, mandatory.
 * @param version A version string, i.e. for semantic versioning.
 * @param description A description of the project.
 * @param license A license by which the project content may be used.
 * @param maintainer A list of the project's maintainers.
 * @param website IRI of a website for the project.
 */
@Serializable
open class InterchangeProject(
    override var name: String,
    var version: String = "*",
    override var description: String? = null,
    var license: String? = null,
    var maintainer: MutableList<String>? = null,
    var website: Url? = null,
    var topic: MutableList<String>? = null,
    var usage: MutableList<InterchangeProjectUsage>? = mutableListOf()
): ProjectBase {
    /**
     * The id allows us to tag InterchangeProjects that are persisted in an API project.
     * NOT STANDARD.
     */
    override var id: Uuid = Uuid.random()

    constructor(interchangeProject: InterchangeProject): this(
        name = interchangeProject.name,
        description = interchangeProject.description,
        version = interchangeProject.version,
        license = interchangeProject.license,
        maintainer = interchangeProject.maintainer,
        website = interchangeProject.website,
        topic = interchangeProject.topic,
        usage = interchangeProject.usage
    )
}
