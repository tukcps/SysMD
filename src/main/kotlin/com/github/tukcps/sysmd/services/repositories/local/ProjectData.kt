package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmlv2.entities.CommitDataObject
import com.github.tukcps.sysmlv2.entities.InterchangeProject
import com.github.tukcps.sysmlv2.entities.Project
import com.github.tukcps.sysmlv2.entities.ProjectUsage
import java.io.File
import java.time.ZonedDateTime
import java.util.*

/**
 * Data model of a project in SysMD.
 * The project can be either
 * - A project from the web
 * - A project from a file (interchange project)
 * @param directory if from files, the directory in which the files are
 */
open class ProjectData(
    var directory: File? = null,
    override var id: UUID = UUID.randomUUID(),
    override var name: String,
    override var description: String = "",
    override var created: Date = Date.from(ZonedDateTime.now().toInstant()),
    override var alias: List<String> = mutableListOf(name),
    var data: MutableList<CommitDataObject> = mutableListOf(),
    open var files: MutableList<File> = mutableListOf()
) : Project, InterchangeProject(name=name) {
    override fun branchesIdList(): Collection<UUID> = TODO("Not yet implemented")
    override fun commitsIdList(): Collection<UUID>  = TODO("Not yet implemented")
    override fun defaultBranchId(): UUID = TODO("Not yet implemented")
    override fun headsIdList(): List<UUID>  = TODO("Not yet implemented")

    fun getElements() = data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
    fun getUsages() = data.filter { it.projectUsage != null }.mapNotNull { it.projectUsage }
    fun addUsage(usage: ProjectUsage) { data.add(Data(projectUsage = usage)) }

    constructor(project: Project): this(
        id = project.id,
        name = project.name,
        created = project.created,
        alias = project.alias
    )
}