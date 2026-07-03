package com.github.tukcps.sysmd.rest.controller

import com.github.tukcps.sysmd.configuration.OpenAPIConfig
import com.github.tukcps.sysmd.rest.entities.requests.CodeRequest
import com.github.tukcps.sysmd.rest.entities.requests.ProjectMetaRequest
import com.github.tukcps.sysmd.rest.entities.response.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.readText
import com.github.tukcps.sysmd.ui.toUriString
import com.github.tukcps.sysmd.ui.writeText
import io.github.tukcps.sysmlv2.api.entities.responseModels.ElementResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.apache.logging.log4j.LogManager
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid
import kotlin.io.path.Path as KotlinPath


/**
 * Implements the endpoints for the session service
 *
 * - `GET /session`
 *    + Returns a list of all session-id
 *
 * - `POST /session`
 *    + Creates a new session for a project with given ID
 *
 * - `DELETE /session/ID`
 *    + kills a session
 *
 * - `POST /session/ID/files/`
 *    + Bitmap -> Boolean
 *
 * - `GET /session/ID/project/ID/icon`
 *    + ProjectId -> Bitmap
 *
 * - `GET /session/ID/files/`
 *    + filename -> Bitmap
 *
 * - `GET /session/ID/meta`
 *    + returns meta-info including all source file names (filename, text)
 *
 * - `PUT /session/ID/meta`
 *    + save meta-info including all source files (filename, text)
 *
 * - `GET /session/ID/cells/(cells)`
 *    + returns list of all cells
 *
 * - `POST /session/ID/model/(coderequest)`
 *    + code -> Session Status ; Compiles source code
 *
 * - `POST /session/ID/commit/(cells)`
 *    + Saves/commits the session to version control
 */

@Tag(name = OpenAPIConfig.SESSION_RESOURCE)
@RestController
class SessionController {

    /**
     * **Getting a list of all session ids**
     * - `GET /session`
     * @return a list of all active sessions (session id + project name)
     */
    @CrossOrigin
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all sessions (id and project in response).")
    @GetMapping(path = ["/session"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllSessions(): ResponseEntity<List<SessionResponse>> {
        val sessions = sessionService.getAllSessions().map { SessionResponse(it.id.toJavaUuid(), it.project.name?:"") }
        val response = ResponseEntity(sessions, HttpStatus.OK)
        return response
    }

    /**
     * **Starting a session**
     * - `POST /session/{projectName}`
     * - Creates a new session for working with a project and returns a new session id.
     * @return The id of the new session.
     */
    @CrossOrigin
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Creates a new session and returns its id.",
        description = "Creates a new session associated with a project." +
                "The project's name is given in the request body." +
                "The id of the created session is returned in the response."
    )
    @PostMapping(path = ["/session"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun createSession(
        @RequestBody @Valid projectName: String
    ): ResponseEntity<String> {
        val project = projectService.getProjects().find { it.name == projectName } ?:
            return ResponseEntity.notFound().build()

        val session = project.let {  sessionService.createSession(project) }
        logger.info("Accessed endpoint POST /session")
        return ResponseEntity(session.id.toString(), HttpStatus.CREATED)
    }

    /**
     * ***Loading a session ...***
     * - `GET /session/index`
     * - Gets a list of all files in a session's project
     */
    @CrossOrigin(allowedHeaders = ["SessionId"])
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Gets all model files in the index of a model interchange project.",
        description = """Gets for a session of a project of the first the index of the .meta file and
            for each indexed file, the content in the response.""")
    @GetMapping(path = ["/session/meta"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMeta(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<ProjectMetaResponse> {
        val session = sessionService.getSession(sessionId.toKotlinUuid())
        val project = session?.project
        val index = project?.getIndexedFiles()
        if (project == null) return ResponseEntity.notFound().build()
        val responseContent = ProjectMetaResponse(
            project.id,
            project.name!!,
            project.description
        )
        index?.forEach { file ->
            responseContent.index += IndexEntry(file.name, file.readText())
        }
        val response = ResponseEntity(responseContent, HttpStatus.OK)
        return response
    }

    /**
     * **Compile a list of cells, each given by a element data model of a textual representation.**
     * - `PUT /session/ID/model/(cells)`
     *    + Compiles the code given in the path variable and runs the compiler.
     *    + The code is _not_ saved, only compiled.
     * @param request A CodeRequest entity that consists of the code and the level
     * to which the compiler will analyze, from 0 (nothing) to 7 (constraint propagation).
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Compiles the code in the payload and adds generated elements to the model in the session.")
    @PutMapping(path = ["/session/model"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateModel(
        @RequestBody request: CodeRequest,
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID
    ): ResponseEntity<SessionStatusResponse> {
        val session = sessionService.getSession(sessionId.toKotlinUuid()) ?: return ResponseEntity.notFound().build()
        try {
            sessionService.updateModel(sessionId.toKotlinUuid(),
                request.body,
                Language.language[request.language] ?: Language.MARKDOWN,
                request.namespace,
                Runlevel.toRunlevel(request.runlevel) ?: Runlevel.NAMES_RESOLVED
            )
            return ResponseEntity(SessionStatusResponse(session.status), HttpStatus.OK)
        } catch(_: Exception) {
            return ResponseEntity(SessionStatusResponse(session.status), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    /**
     * **Saving meta-information of a session/project** ...
     * - `PUT /session/meta`
     *    + Transfers meta-information and project-information.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Puts all model files into a project. Old index and files are overwritten.")
    @PutMapping(path = ["/session/meta"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun putMeta(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @RequestBody projectMetaRequest: ProjectMetaRequest
    ): ResponseEntity<ProjectMetaResponse> {
        val session = sessionService.getSession(sessionId.toKotlinUuid())
            ?: return ResponseEntity.notFound().build()
        val project = session.project
        project.clearIndex()
        val responseContent = ProjectMetaResponse(
            project.id,
            project.name!!,
            project.description
        )
        projectMetaRequest.index.forEach { file ->
            project.addIndex(file.filename, file.filename)
            val fileToWrite = project.directory?.let { Path(it, file.filename) }
            fileToWrite?.writeText(file.content)
        }
        return ResponseEntity(responseContent, HttpStatus.CREATED)
    }


    /**
     * **Get a list of all (documentation) names files related to a project**
     * -  i.e., pictures in a project.**
     *
     * `GET /session/files`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all document file names.")
    @GetMapping(path = ["/session/files"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllFiles(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<List<String>> {
        val children = sessionService.getFiles(sessionId.toKotlinUuid())
        return if (children != null)
            ResponseEntity(children, HttpStatus.OK)
        else
            ResponseEntity(HttpStatus.NOT_FOUND)
    }

    /**
     * **Get a list of a project's cells
     * -  i.e., pictures in a project.**
     *
     * `GET /session/cells`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all document file names, including non-sysml files like pictures etc.")
    @GetMapping(path = ["/session/cells"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllCells(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<MutableMap<String, List<ElementResponse>>> {
        val session = sessionService.getSession(sessionId.toKotlinUuid())
        val project = session?.project
        val cellIndex = project?.getCells()
        val responseContent = mutableMapOf<String, List<ElementResponse>>()
        cellIndex?.forEach { (key, value) ->
            val elementsResponses = mutableListOf<ElementResponse>()
            elementsResponses.addAll( value.map { ElementResponse(it) })
            responseContent[key] = elementsResponses
        }

        val response = ResponseEntity(responseContent, HttpStatus.OK)
        return response
    }


    /**
     * **Get a file by name**
     * - `GET /session/files/NAME`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets a document file, typically a picture in .png format, by its name.")
    @GetMapping(path = ["/session/files/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFileByName(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "Name of the file.", required = true) @PathVariable name: String,
    ): ResponseEntity<Resource> {
        try {
            val session = sessionService.getSession(sessionId.toKotlinUuid())
                ?: return ResponseEntity.notFound().build()
            val imagePath = session.project.directory?.let { Path(it, "Files", name) }
            val resource: Resource? = imagePath?.toUriString()?.let { UrlResource(it) }

            return if (resource?.exists() == true && resource.isReadable) {
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/png")
                    .body(resource)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            }
        } catch (_: MalformedURLException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Post a file
     * `POST /session/files`
     */
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Uploads a document file, typically a picture in .png format, to the project.")
    @PostMapping(path = ["/session/files"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "File to be uploaded.", required = true) @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        try {
            val session = sessionService.getSession(sessionId.toKotlinUuid())
            val filesDirectory = session?.project?.directory?.let { Path(it, "Files") }

            // Create directory if it doesn't exist
            if (filesDirectory == null || !SystemFileSystem.exists(filesDirectory)) {
                SystemFileSystem.createDirectories(filesDirectory!!)
            }

            // Use original filename or fallback
            val filename = file.originalFilename ?: "default_file.png"
            val targetPath = KotlinPath(Path(filesDirectory.name, filename).toString())
            // Copy the file content
            Files.copy(file.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("filename" to filename))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to upload file: ${e.message}"))
        }
    }


    /**
     * **Get all elements in the session**
     * - `GET /session/elements`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all elements of a session.")
    @GetMapping(path = ["/session/elements"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllElements(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<ArrayList<ElementResponse>>  {
        try {
            val elements = sessionService.getAllElements(sessionId.toKotlinUuid())?.map { ElementResponse(it) }
            return if (elements != null) {
                ResponseEntity.ok().body(elements.toCollection(ArrayList()))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(arrayListOf())
            }
        } catch (_: MalformedURLException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * **Get all variables of the solver in the session**
     * - `GET /session/variables`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all variables of a solver run with computed values.")
    @GetMapping(path = ["/session/variables"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllVariables(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<VariablesResponse>  {
        return try {
            val response = sessionService.getVariables(sessionId.toKotlinUuid()) ?.let { VariablesResponse(it) }

            if (response != null) {
                ResponseEntity.ok().body(response)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(VariablesResponse(emptyList<VariableResponse>()))
            }
        } catch (_: MalformedURLException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }


    /**
     * **Get all specializations in the session**
     * - `GET /session/elements/$id/subtypes`
     */
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all specializations of a type.")
    @GetMapping(path = ["/session/elements/{elementId}/subtypes"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSubtypes(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "elementId of an element of kind Type", required = true) @PathVariable elementId: UUID
    ): ResponseEntity<ArrayList<ElementResponse>>  {
        try {
            val result = sessionService.getSubtypes(sessionId.toKotlinUuid(), elementId.toKotlinUuid())
                ?.map { ElementResponse(it) }
            return if (result != null) ResponseEntity.ok().body(result.toCollection(ArrayList()))
            else ResponseEntity.status(HttpStatus.NOT_FOUND).body(arrayListOf())
        } catch (_: MalformedURLException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all owned elements of an element.")
    @GetMapping(path = ["/session/elements/{elementId}/ownedelements"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getOwnedElements(
        @RequestHeader(value = "SessionId", required = true) sessionId: Uuid,
        @Parameter(description = "elementId of an element of kind Type", required = true) @PathVariable elementId: Uuid
    ): ResponseEntity<ArrayList<ElementResponse>>  {
        return try {
            val owned = sessionService.getOwnedElements(sessionId, elementId)
            return if (owned != null)
                ResponseEntity.ok().body(owned.map { ElementResponse(it) }.toCollection(ArrayList()))
            else
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(arrayListOf())
        } catch (_: MalformedURLException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Global/static constants
     */
    companion object {
        private val logger = LogManager.getLogger(SessionController::class.java)
    }
}
