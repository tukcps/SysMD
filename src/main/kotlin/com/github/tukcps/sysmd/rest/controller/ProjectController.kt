package com.github.tukcps.sysmd.rest.controller

import com.github.tukcps.sysmd.configuration.OpenAPIConfig
import com.github.tukcps.sysmd.rest.entities.response.ExceptionResponse
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import io.github.tukcps.sysmlv2.api.entities.requestModels.ProjectRequest
import io.github.tukcps.sysmlv2.api.entities.responseModels.ProjectResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest
import java.util.*


/**
 * The project controller provides an api for simple version management.
 * It handles the projects following the SysML v2 REST API endpoints:
 * - POST /projects: JSON → ID
 * - GET /projects: → Array of Project-Infos (Name, branches, …)
 * - GET /projects/ID: → Project
 * - PUT /projects/ID: JSON w/ ID → Status
 * - DEL /projects/id: ID → Status
 */
@Tag(name = OpenAPIConfig.PROJECT_RESOURCE)
@ControllerAdvice
@RestController
class ProjectController {
    /**
     * POST /projects
     * Creates a new project from given JSON and returns its id.
     * @return Project with id set.
     */

    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Creates a new project and returns its id.",
        description = "Creates a new project. The id of the created project is returned in the response."
    )
    @PostMapping(path = ["/projects"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun postProject(
        @RequestBody @Valid requestModel: ProjectRequest
    ): ResponseEntity<ProjectResponse> {
        val persistedProject = projectService.createProject(name = requestModel.name, description = requestModel.description, defaultBranch = null)
        val response = ResponseEntity(ProjectResponse(persistedProject), HttpStatus.CREATED)
        logger.info("Accessed endpoint POST /projects with project name: " + requestModel.name)
        return response
    }

    /** Return type of get all projects */
    class ProjectResponseList : ArrayList<ProjectResponse>()

    /**
     * GET /projects
     * Gets a list of all projects
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets all projects.")
    @GetMapping(path = ["/projects"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllProjects(): ResponseEntity<ProjectResponseList> {
        val projects = ProjectResponseList()
        projectService.getProjects().forEach {
            projects.add(ProjectResponse(it))
        }
        val response = ResponseEntity(projects, HttpStatus.OK)
        logger.info("Accessed endpoint GET /projects.")
        return response
    }

    /**
     * GET /projects/ID
     * Gets a project by id.
     */
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Gets a project by its id.")
    @GetMapping(path = ["/projects/{projectId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getProjectById(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: UUID
    ): ResponseEntity<ProjectResponse> {
        val project = projectService.getProjectById(projectId)!!
        val response = ResponseEntity(ProjectResponse(project), HttpStatus.OK)
        logger.info("Accessed endpoint GET /projects by ID.")
        return response
    }

    /**
     * Delete /projects/ID
     * Deletes a project by id
     */
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @Operation(summary = "Deletes a project by its id.")
    @DeleteMapping(path = ["/projects/{projectId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteProjectById(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: UUID
    ): ResponseEntity<ProjectResponse> {
        val project = projectService.deleteProject(projectId)
        val response = if (project != null)
            ResponseEntity(ProjectResponse(project), HttpStatus.OK)
        else
            ResponseEntity(ProjectResponse(), HttpStatus.NOT_FOUND)
        logger.info("Accessed endpoint DELETE /projects/$projectId.")
        return response
    }


    /**
     * PUT /projects/ID
     * Updates a project by id
     */
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = [
        "http://localhost:3000",
        "http://localhost:4200",
        "http://cps-testing.cs.rptu.de",
        "https://cps-testing.cs.rptu.de"
    ])
    @Operation(summary = "Updates a project by its id.")
    @PutMapping(path = ["/projects/{projectId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProjectById(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: UUID,
        @RequestBody requestModel: ProjectRequest
    ): ResponseEntity<ProjectResponse> {
        projectService.updateProject(
            projectId,
            requestModel.name,
            requestModel.description,
        )
        val response = ResponseEntity(ProjectResponse(projectService.getProjectById(projectId)!!), HttpStatus.OK)
        logger.info("Accessed endpoint PUT /projects/$projectId.")
        return response
    }

    /**
     * Exception handler for malformed requests
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ExceptionResponse> {
        val exceptionResponse = ExceptionResponse(message = ex.message)
        return ResponseEntity(exceptionResponse, null, HttpStatus.BAD_REQUEST)
    }

    /**
     * Global/static constants
     */
    companion object {
        private val logger = LogManager.getLogger(ProjectController::class.java)
    }
}