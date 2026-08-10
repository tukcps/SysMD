package com.github.tukcps.sysmd.services.repositories.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.rest.entities.api.entities.CommitDataObject
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import com.github.tukcps.sysmd.rest.entities.api.entities.ProjectUsage
import com.github.tukcps.sysmd.rest.entities.interchange.InterchangeProject
import com.github.tukcps.sysmd.rest.entities.interchange.Meta
import com.github.tukcps.sysmd.rest.entities.interchange.ProjectBase
import com.github.tukcps.sysmd.services.util.JsonSupport
import com.github.tukcps.sysmd.ui.writeText
import kotlinx.datetime.Instant
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import kotlin.uuid.Uuid


/**
 * Data record of a project handled in SysMD.
 * It can be either an interchange file project or an api project.
 * The project can be either
 * - A project from the web or
 * - A project from a file (interchange project)
 *
 * @param project Common data for web- and file-projects
 * @param directory Path to the interchange project.
 * @param meta Information from interchange-project, like index.
 */
class ProjectData(
    var project : ProjectBase,          // Either Interchange- or Web-Record
    var directory: Path? = null,        // In case of interchange project
    var meta: Meta? = null,             // In case of interchange project
): Project {

    override var id: Uuid
        get() = project.id
        set(value) { project.id = value }

    override var name: String?
        get() = project.name
        set(value) { if (project is Project) (project as Project).name = value else (project as InterchangeProject).name = value?:""}

    override var created: Instant
        get() = if (project is Project) (project as Project).created
            else meta?.created?: Instant.DISTANT_FUTURE
        set(value) {
            if (project is Project) (project as Project).created = value
            else meta?.created = value
        }

    override var alias: Collection<String>
        get() = if (project is Project) (project as Project).alias else listOf()
        set(value) { if (project is Project) (project as Project).alias = value }

    override var description: String
        get() = project.description?:""
        set(value) { if (project is Project) (project as Project).description = value else (project as InterchangeProject).description = value }

    var data: MutableList<CommitDataObject> = mutableListOf()

    /**
     * @return a list of files as persisted in .meta.json's index as values.
     */
    fun getIndexedFiles(): List<Path> {
        val paths: MutableList<Path> = mutableListOf()
        meta?.index?.values?.forEach {
            val path = Path(directory?:Path(""),it)
            if (SystemFileSystem.metadataOrNull(path)?.isRegularFile == true) {
                paths.add(path)
            } else {
                logger.error("Inconsistency of .meta.json file index: File $it does not exist")
            }
        }
        return paths
    }

    /**
     * @return a list of cells based on the file index
     */
    fun getCells(): LinkedHashMap<String, List<ElementData>> {
        val files = getIndexedFiles()
        val result = linkedMapOf<String, List<ElementData>>()
        for (file in files) {
            result[file.name] = file.getCells()
        }
        return result
    }

    /**
     * @return a list of cells based on the file index
     */
    fun getCells(file: String): List<ElementData>? {
        val paths = getIndexedFiles()
        if (file !in paths.map { it.name }) return null
        return paths.first { it.name == file }.getCells()
    }

    /**
     * Adds a new (or already existing) file to the index.
     * @param key key of the index entry; in the standard the root namespace in the file.
     * @param fileName name of the file including extension (e.g. '.md', '.kerml', '.sysml'); either existing or it will be created.
     */
    @Deprecated("Use function in services")
    fun addIndex(key: String, fileName: String): Path {
        if (meta == null) {
            meta = Meta(index = linkedMapOf(key to fileName), created = created)
        }
        meta?.index?.set(key, fileName)

        val path = directory?.let { Path(it, fileName) }
        path?.let {
            if (!SystemFileSystem.exists(it)) {
                path.writeText("""
    ---
    title: New file "$fileName"
    subtitle:  -- Subtitle -- 
    author: Author's names 
    ---
    
    - The file is in the folder `$path`. 
    - You can **rename** or **delete** the file via the left pane in the respective project. 
    - Write your model and documentation here 
        - Edit a cell by double-clicking on it or left of it, 
        - Add a cells by clicking on the space between or below cells, or on the "+" left of it.
        - Choose kind and syntax of a cell you edit by choosing "Language" on top of the cell. 
    
                """.trimIndent())
            }
        }

        saveToInterchangeFiles()
        return Path(directory!!,fileName)
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
    @Deprecated("Use service function for setting meta and project data instead (???)")
    fun saveToInterchangeFiles() {
        try {
            if (directory != null) {
                SystemFileSystem.createDirectories(directory!!)

                val projectJson = Path(directory!!, ".project.json")
                SystemFileSystem.sink(projectJson).buffered().use { sink ->
                    val jsonForProject = JsonSupport.json.encodeToString(
                        InterchangeProject(this.project as InterchangeProject)
                        .also { it.id = this.project.id }
                    )
                    sink.writeString(jsonForProject)
                }

                val metaFileJson = Path(directory!!, ".meta.json")
                val jsonForMeta = Json.encodeToString(this.meta)
                SystemFileSystem.sink(metaFileJson).buffered().use { sink ->
                    sink.writeString(jsonForMeta)
                }
                logger.info("Project '$name' saved to interchange file.")
            } else
                logger.info("Project '$name' not saved to interchange file.")
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    companion object {
        /**
         * loads a project from a project interchange file.
         * @param directory directory in which the interchange project files are found.
         */
        fun fromInterchangeFiles(directory: Path): ProjectData? {
            try {
                val projectFile = Path(directory, ".project.json")
                val metaFile = Path(directory,".meta.json")
                if (SystemFileSystem.metadataOrNull(projectFile)?.isRegularFile != true) return null
                if (SystemFileSystem.metadataOrNull(metaFile)?.isRegularFile != true) return null

                val projectJson = SystemFileSystem.source(projectFile).buffered().use { it.readString() }
                val project = try {
                    JsonSupport.json.decodeFromString<InterchangeProject>(projectJson)
                } catch (e: Exception) {
                    objectMapper.readValue(projectJson, InterchangeProject::class.java)
                }

                val metaJson = SystemFileSystem.source(metaFile).buffered().use { it.readString() }
                val meta = JsonSupport.json.decodeFromString<Meta>(metaJson)

                val projectData = ProjectData(project, directory, meta)
                return projectData
            } catch (e: Exception) {
                logger.error("Error loading project: ${e.message}", e)
                return null
            }
        }

        private val objectMapper: ObjectMapper = jacksonObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .registerModule(com.github.tukcps.sysmd.configuration.JacksonKotlinUuidConfig.createModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        private val logger = LogManager.getLogger(ProjectData::class.java)!!
    }
}