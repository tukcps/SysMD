package com.github.tukcps.sysmd.rest.controller

import com.github.tukcps.sysmd.configuration.OpenAPIConfig
import com.github.tukcps.sysmd.model.datamodel.createFrom
import com.github.tukcps.sysmd.rest.CommitImplementation
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ElementResponse
import com.github.tukcps.sysmd.services.repositories.local.SysMDElementNavigationService
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import kotlin.uuid.Uuid

@RestController
@Tag(name = OpenAPIConfig.ELEMENT_RESOURCE) // , description = "Allows getting the elements of a project if open in a session.")
class ElementNavigationController {

    /**
     * Get all elements of a commit
     * @param projectId ID of the project
     * @param commitId ID of the commit
     * @return  list
     */
    @Operation(summary = "Gets all elements from the current commit.")
    @GetMapping(
        path = ["/projects/{projectId}/commits/{commitId}/elements"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAllElements(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: Uuid,
        @Parameter(description = "Id of the commit.", required = true) @PathVariable commitId: Uuid
    ): ResponseEntity<List<ElementResponse>> {
        val project = projectService.getProjectById(projectId)
        val response = if (project != null) {
            val commit = CommitImplementation()
            val elements = SysMDElementNavigationService.getElements(project, commit)
            val elementsResponse: List<ElementResponse> = elements.map { it.createFrom<ElementResponse>() }
            ResponseEntity(elementsResponse, HttpStatus.OK)
        } else
            ResponseEntity(mutableListOf(), HttpStatus.NOT_FOUND)
        logger.info("Accessed endpoint GET /projects/$projectId/commits/$commitId")
        return response
    }


    /**
     * Get all root elements of a commit
     * @param projectId ID of the project
     * @param commitId ID of the commit
     * @return List of root elements (those with an owner == null)
     */
    @Operation(summary = "Gets all root elements from the commit.")
    @GetMapping(
        path = ["/projects/{projectId}/commits/{commitId}/roots"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAllRoots(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: Uuid,
        @Parameter(description = "Id of the commit.", required = true) @PathVariable commitId: Uuid
    ): ResponseEntity<List<ElementResponse>> {
        val project = projectService.getProjectById(projectId)
        val response = if (project != null) {
            val commit = CommitImplementation()
            val elements = SysMDElementNavigationService.getRootElements(project, commit)
            val elementsResponse: List<ElementResponse> = elements.map { it.createFrom<ElementResponse>() }
            ResponseEntity(elementsResponse, HttpStatus.OK)
        } else
            ResponseEntity(mutableListOf(), HttpStatus.NOT_FOUND)
        logger.info("Accessed endpoint GET /projects/$projectId/commits/$commitId/roots")
        return response
    }


    /**
     * Gets an element by ID
     * @param projectId ID of the project
     * @param commitId ID of the commit
     * @param elementId ID of the element
     * @return element with id if found
     */
    @Operation(summary = "Gets an element by project, commit and its id.")
    @GetMapping(path = ["/projects/{projectId}/commits/{commitId}/elements/{elementId}"])
    fun getElementById(
        @Parameter(description = "Id of the project.", required = true) @PathVariable projectId: Uuid,
        @Parameter(description = "Id of the commit.", required = true) @PathVariable commitId: Uuid,
        @Parameter(description = "Id of the element.", required = true) @PathVariable elementId: Uuid
    ): ResponseEntity<ElementResponse> {
        val project = projectService.getProjectById(projectId)
        val response = if (project != null) {
            val commit = CommitImplementation()
            val element = SysMDElementNavigationService.getElementById(project, commit, elementId)
            if (element != null)
                ResponseEntity(element.createFrom<ElementResponse>(), HttpStatus.OK)
            else
                ResponseEntity(ElementResponse(), HttpStatus.NOT_FOUND)
        } else
            ResponseEntity(ElementResponse(), HttpStatus.NOT_FOUND)
        logger.info("Accessed endpoint GET /projects/$projectId/commits/$commitId/elements/$elementId")
            return response
    }

    companion object {
        private val logger = LogManager.getLogger(ElementNavigationController::class.java)
    }
}