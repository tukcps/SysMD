package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmlv2.api.ProjectDataVersioningService
import com.github.tukcps.sysmlv2.entities.Branch
import com.github.tukcps.sysmlv2.entities.Commit
import com.github.tukcps.sysmlv2.entities.CommitDataObject
import com.github.tukcps.sysmlv2.entities.Tag
import java.util.*

object SysMDProjectDataVersioningService: ProjectDataVersioningService {
    /**
     * Simple dummy that writes a commit to the default branch of a project.
     */
    override fun commit(
        owningProject: UUID,
        name: String?,
        description: String?,
        payload: List<CommitDataObject>,
        previousCommit: UUID?
    ): UUID {
        // val commit  = CommitImplementation( )
        // projectsLoaded[name!!] = changes.toMutableList()
        return UUID.randomUUID() // NOTE: Now way to get it back in this dummy.
    }

    override fun getBranchById(projectId: UUID, branchId: UUID?): Branch = TODO("Not yet implemented")
    override fun getBranches(projectId: UUID): Collection<Branch>  = TODO("Not yet implemented")
    override fun getCommitById(projectId: UUID, commitId: UUID?): Commit = TODO("Not yet implemented")
    override fun getCommitChange() = TODO("Not yet implemented")
    override fun getCommitChangeById() = TODO("Not yet implemented")
    override fun getCommits(projectId: UUID): Collection<Commit> = TODO("Not yet implemented")
    override fun getDefaultBranch(projectId: UUID): UUID = TODO("Not yet implemented")
    override fun getExternalRelationships() = TODO("Not yet implemented")
    override fun getHeadCommit(projectId: UUID, branchId: UUID?): Commit = TODO("Not yet implemented")
    override fun getTagById(projectId: UUID, tagId: UUID): Tag = TODO("Not yet implemented")
    override fun getTags(projectId: UUID): Collection<Tag> = TODO("Not yet implemented")
    override fun setDefaultBranch(projectId: UUID, branchID: UUID) = TODO("Not yet implemented")
    override fun createBranch(projectId: UUID, branch: Branch) = TODO("Not yet implemented")
    override fun createCommit(projectId: UUID, commit: Commit): Commit = TODO("Not yet implemented")
    override fun deleteBranch(projectId: UUID, branchId: UUID): Boolean = TODO("Not yet implemented")
}