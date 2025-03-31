package com.github.tukcps.sysmd.services.repositories.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.entities.ProjectUsage
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import io.github.tukcps.sysmlv2.interchange.Meta
import io.github.tukcps.sysmlv2.interchange.ProjectBase
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.*

/**
 * Data record of a project handled in SysMD.
 * It can be either an interchange file project or an api project.
 * The project can be either
 * - A project from the web or
 * - A project from a file (interchange project)
 */
class ProjectData(
    var project : ProjectBase,
    var directory: Path? = null,
    private var meta: Meta? = null,
): Project {

    override var id: UUID
        get() = project.id
        set(value) { project.id = value }

    override var name: String?
        get() = project.name
        set(value) { if (project is Project) (project as Project).name = value else (project as InterchangeProject).name = value?:""}

    override var created: OffsetDateTime
        get() = if ( project is Project ) (project as Project).created else meta?.created?:OffsetDateTime.now()
        set(value) { if (project is Project) (project as Project).created = value else meta?.created = value }

    override var alias: Collection<String>
        get() = if (project is Project) (project as Project).alias else listOf()
        set(value) { if (project is Project) (project as Project).alias = value }

    override var description: String
        get() = project.description?:""
        set(value) { if (project is Project) (project as Project).description = value else (project as InterchangeProject).description = value }

    var data: MutableList<CommitDataObject> = mutableListOf()

    /**
     * @return a list of files as persisted in .meta.json
     */
    fun getIndex(): List<File> {
        val files: MutableList<File> = mutableListOf()
        meta?.index?.values?.forEach {
            val file = directory?.resolve(it)?.toFile()
            if (file != null) {
                files.add(file)
            } else {
                logger.error("Inconsistency of .meta.json file index: File $it does not exist")
            }
        }
        return files
    }

    /**
     * @return a list of cells based on the file index
     */
    fun getCellIndex(): Map<String, Collection<ElementData>> {
        val files = getIndex()
        val result = hashMapOf<String, Collection<ElementData>>()
        files.forEach { file ->
            result[file.name] = file.getCells()
        }
        return result
    }

    /**
     * Adds a new (or already existing) file to the index.
     * @param key key of the index entry; in the standard the root namespace in the file.
     * @param fileName name of the file including extension (e.g. '.md', '.kerml', '.sysml'); either existing or it will be created.
     */
    fun addIndex(key: String, fileName: String): File? {
        val file = directory?.resolve(fileName)?.toFile() ?: return null
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("""
---
title: New file
name:  new
---

Write your model and documentation here. 

            """.trimIndent())
        }

        if (meta == null) {
            meta = Meta(
                index = hashMapOf(key to fileName),
                created = created,
            )
        }
        meta?.index?.set(key, file.name)
        saveToInterchangeFiles()
        return file
    }

    /**
     * Removes a file from the file index
     */
    fun removeFromIndex(file: String) {
        val newIndex = hashMapOf<String, String>()
        meta?.index?.forEach { key, value ->
            if (value != file) { newIndex[key] = value }
        }
        meta?.index = newIndex
    }

    fun clearIndex() = meta?.index?.clear()
    fun getElements() = data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
    fun getUsages() = data.filter { it.projectUsage != null }.mapNotNull { it.projectUsage }
    fun addUsage(usage: ProjectUsage) { data.add(Data(projectUsage = usage)) }

    /**
     * Saves a project into a project interchange file.
     * The directory is given by settings and derived from settings and project name,
     * project attribute 'directory'.
     */
    fun saveToInterchangeFiles() {
        try {
            if (directory != null) {
                Files.createDirectories(directory!!)
                val objectMapper = ObjectMapper().apply {
                    registerModule(KotlinModule.Builder().build())
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }.writerWithDefaultPrettyPrinter()

                val projectJson = directory!!.resolve(".project.json")
                val jsonForProject = objectMapper.writeValueAsString(
                    InterchangeProject(this.project as InterchangeProject).also {
                        it.id = this.project.id
                    }
                )
                Files.writeString(projectJson, jsonForProject)

                val metaFile = directory!!.resolve(".meta.json")
                val jsonForMeta = objectMapper.writeValueAsString(this.meta)
                Files.writeString(metaFile, jsonForMeta)

                logger.info("Project '$name' saved to interchange file.")
            } else
                logger.info("Project '$name' not saved to interchange file.")
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    fun updateIndexFilename(oldFilename: String, newFilename: String) {
        meta?.index?.keys?.forEach {
            if (meta?.index!![it] == oldFilename) {
                meta?.index!![it] = newFilename
            }
        }
        saveToInterchangeFiles()
    }

    companion object {
        /**
         * loads a project from a project interchange file.
         * @param directory directory in which the interchange project files are found.
         */
        fun fromInterchangeFiles(directory: Path): ProjectData? {
            try {
                val projectFile = directory.resolve(".project.json")?:return null
                val metaFile = directory.resolve(".meta.json")?:return null

                val projectJson = Files.readString(projectFile)
                val project =  objectMapper.readValue(projectJson, InterchangeProject("").javaClass)

                val metaJson = Files.readString(metaFile)
                val meta = objectMapper.readValue(metaJson, Meta::class.java)

                val projectData = ProjectData(project, directory, meta)
                return projectData
            } catch (e: Exception) {
                logger.error("Error loading project: ${e.message}", e)
                return null
            }
        }
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
            .registerModule( JavaTimeModule() )

        private val logger = LogManager.getLogger(ProjectData::class.java)!!
    }
}