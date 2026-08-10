package com.github.tukcps.sysmd.rest.entities.api.services

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import kotlin.uuid.Uuid

/**
 * The element navigation service API.
 * Note that in the standard, the commit id is not nullable.
 * Status: Compliant with 1-2025
 */
interface ElementNavigationService {

    /**
     * Gets all elements of a project by project and commit id.
     * In extension to the standard, we accept nullable commit -> the head of default-branch
     */
    fun getElements(project: Project, commit: Commit? = null): Collection<ElementDataIF>

    /**
     * Gets a concrete element of a project and commit by id.
     * In extension to the standard, we accept null commit -> head of default-branch.
     */
    fun getElementById(project: Project, commit: Commit? = null, elementId: Uuid): ElementDataIF?

    /**
     * Gets a concrete relationship by project, commit, related element id and direction
     */
    fun getRelationshipsByRelatedElement(project: Project, commit: Commit, elementId: Uuid, direction: String ) : Collection<ElementDataIF>

    /**
     * Gets the elements owned by the root namespace by project and commit id.
     */
    fun getRootElements(project: Project, commit: Commit): Collection<ElementDataIF>
}