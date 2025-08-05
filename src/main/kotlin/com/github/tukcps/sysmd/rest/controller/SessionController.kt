package com.github.tukcps.sysmd.rest.controller

import com.github.tukcps.sysmd.configuration.OpenAPIConfig
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.rest.entities.requests.CodeRequest
import com.github.tukcps.sysmd.rest.entities.requests.SessionIndexRequest
import com.github.tukcps.sysmd.rest.entities.response.*
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.SessionManager.startSession
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import io.github.tukcps.sysmlv2.api.entities.responseModels.ElementResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
import kotlin.io.path.writeText


/**
 * Implements the endpoints for the session service
 *
 * POST /session (Project ID -> Session ID)
 * GET /session ( -> All sessions)
 * DELETE /session/ID ( = reset)
 * POST /session/ID/files/ (file in PNG format)
 * GET /session/ID/files/ (file in PNG format)
 * GET /session/ID/index (returns all source files (filename, text) )
 * PUT /session/ID/index (save all source files (filename, text) )
 * (?) POST /session/ID/cells/(cells)  -> reset, reply with Tabs + Cells (?)
 * (?) GET /session/ID/cells/(cells)   -> reset, reply with Tabs + Cells (?)
 * POST /session/ID/sysml -> Session Status ; Compiles SysML v2 source code
 * POST /session/ID/kerml -> Session Status; Compiles KerML source code
 * POST /session/ID/sysmd -> Session Status; Compiles SysMD source code
 * POST /session/ID/commit/(cells)   -> Saves/commits the session
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
    @Operation(summary = "Gets all sessions.")
    @GetMapping(path = ["/session"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllSessions(): ResponseEntity<List<SessionResponse>> {
        val sessions = SessionManager.getAllSessions().map { SessionResponse(it.id, it.project?.name?:"") }
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
    fun loadProjectDataToSession(
        @RequestBody @Valid projectName: String
    ): ResponseEntity<String> {
        val project = projectService.getProjects().find { it.name == projectName }
        val session = startSession(project as ProjectData)
        // Open the tabs --> needed? Maybe not.
        project.getIndex().forEach { file ->
            session.loadSysMDFromFile(file, compile = true, initialize = 1)
        }
        val response = ResponseEntity(session.id.toString(), HttpStatus.CREATED)
        logger.info("Accessed endpoint POST /session")
        return response
    }

    /**
     * ***Loading a session ...***
     * - `GET /session/index`
     * - Gets a list of all files in a session's project
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Gets all model files in the index of a project.",
        description = """Gets for a session of a project first the index of the .meta file and
            for each indexed file, the content in the response.""")
    @GetMapping(path = ["/session/index"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getIndexedFiles(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<SessionIndexResponse> {
        val session = SessionManager.getSession(sessionId)
        val project = session?.project
        val index = project?.getIndex()
        val responseContent = SessionIndexResponse()
        index?.forEach {
            file -> responseContent.files += IndexEntry(file.name, file.readText())
        }
        val response = ResponseEntity(responseContent, HttpStatus.OK)
        return response
    }

    /**
     * **Compile a piece of code**
     * - `PUT /session/code`
     * - Compiles the code given in the path variable and runs the compiler.
     * - The code is _not_ saved, only compiled.
     * @param request A CodeRequest entity that consists of the code and the level
     * to which the compiler will analyze, from 0 (nothing) to 7 (constraint propagation).
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Compiles the code and adds generated elements to the model in the session.")
    @PutMapping(path = ["/session/code"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun compileCode(
        @RequestBody request: CodeRequest,
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID
    ): ResponseEntity<SessionStatusResponse> {
        val session = SessionManager.getSession(sessionId)

        TextualRepresentationImplementation(language = request.language, body = request.code).also { it.model = session }
            .compile()

        session!!.initialize(request.level)
        if (request.level > 6) { session.propagate() }

        return ResponseEntity(SessionStatusResponse(session.status), HttpStatus.OK)
    }


    /**
     * **Saving a session** ...
     * - `PUT /session/index`
     * - Puts all model files into the project-directory of the session and updates the index of a project.
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Puts all model files into a project. Old index and files are overwritten.")
    @PutMapping(path = ["/session/index"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun putIndexedFiles(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @RequestBody sessionIndexRequest: SessionIndexRequest
    ): ResponseEntity<SessionIndexResponse> {
        val session = SessionManager.getSession(sessionId)
        val project = session?.project
        project!!.clearIndex()
        val responseContent = SessionIndexResponse()
        sessionIndexRequest.files.forEach { file ->
            project.addIndex(file.filename, file.filename)
            val fileToWrite = project.directory?.resolve(file.filename)
            fileToWrite?.writeText(file.content)
        }
        val response = ResponseEntity(responseContent, HttpStatus.CREATED)
        return response
    }


    /**
     * **Get a list of all (documentation) names files related to a project**
     * -  i.e., pictures in a project.**
     *
     * `GET /session/files`
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all document file names.")
    @GetMapping(path = ["/session/files"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllFiles(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<List<String>> {
        val session = SessionManager.getSession(sessionId)
        val project = session?.project
        val files = project?.directory?.resolve("Files")?.toFile()?.listFiles()?.map { it.name }?: emptyList()
        val response = ResponseEntity(files, HttpStatus.OK)
        return response
    }

    /**
     * **Get a list of a project's cells
     * -  i.e., pictures in a project.**
     *
     * `GET /session/files`
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all document file names.")
    @GetMapping(path = ["/session/cells"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllCells(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<MutableMap<String, List<ElementResponse>>> {
        val session = SessionManager.getSession(sessionId)
        val project = session?.project
        val cellIndex = project?.getCellIndex()
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
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets a document file, typically a picture in .png format, by its name.")
    @GetMapping(path = ["/session/files/{name}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getFileByProjectAndName(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "Name of the file.", required = true) @PathVariable name: String,
    ): ResponseEntity<Resource>  {
        try {
            val session = SessionManager.getSession(sessionId)
            val imagePath = session?.project?.directory?.resolve("Files")?.resolve(name)
            val resource: Resource = UrlResource(imagePath!!.toUri())

            return if (resource.exists() && resource.isReadable) {
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
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Uploads a document file, typically a picture in .png format, to the project.")
    @PostMapping(path = ["/session/files"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "File to be uploaded.", required = true) @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        try {
            val session = SessionManager.getSession(sessionId)
            val filesDirectory = session?.project?.directory?.resolve("Files")

            // Create directory if it doesn't exist
            if (filesDirectory == null || !Files.exists(filesDirectory)) {
                Files.createDirectories(filesDirectory!!)
            }

            // Use original filename or fallback
            val filename = file.originalFilename ?: "default_file.png"
            val targetPath = filesDirectory.resolve(filename)
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
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all elements of a session.")
    @GetMapping(path = ["/session/elements"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllElements(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<ArrayList<ElementResponse>>  {
        try {
            val session = SessionManager.getSession(sessionId)

            val response =  if (session != null) {
                ResponseEntity.ok().body(session.get().map { ElementResponse(it.toDAO()) }.toCollection(ArrayList()))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(arrayListOf())
            }
            return response
        } catch (_: MalformedURLException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * **Get all elements in the session**
     * - `GET /session/elements`
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all variables of a solver run.")
    @GetMapping(path = ["/session/variables"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllVariables(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
    ): ResponseEntity<VariablesResponse>  {
        return try {
            val session = SessionManager.getSession(sessionId)

            if (session != null) {
                ResponseEntity.ok().body(VariablesResponse(session.getVariables()))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(VariablesResponse(emptyList<Variable>()))
            }
        } catch (_: MalformedURLException) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }


    /**
     * **Get all elements in the session**
     * - `GET /session/elements/$id/subtypes`
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all variables of a solver run.")
    @GetMapping(path = ["/session/elements/{elementId}/subtypes"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSubtypes(
        @RequestHeader(value = "SessionId", required = true) sessionId: UUID,
        @Parameter(description = "elementId of an element of kind type", required = true) @PathVariable elementId: UUID
    ): ResponseEntity<ArrayList<ElementResponse>>  {
        try {
            val session = SessionManager.getSession(sessionId)
            if (session != null) {
                val type = session[elementId]
                if (type is Type)
                return ResponseEntity.ok().body(type.subtypes.map { ElementResponse(it.toDAO()) }.toCollection(ArrayList()))
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(arrayListOf())
        } catch (_: MalformedURLException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }


    /**
     * Global/static constants
     */
    companion object {
        private val logger = LogManager.getLogger(SessionController::class.java)
    }
}
