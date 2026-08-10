package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.rest.entities.api.entities.*
import com.github.tukcps.sysmd.rest.entities.api.services.ChangeType
import com.github.tukcps.sysmd.rest.entities.api.services.ProjectDataVersioningService
import kotlin.uuid.Uuid

/**
 * There is simply no versioning in SysMD Notebook client.
 */
object SysMDProjectDataVersioningService: ProjectDataVersioningService {
    override fun getCommitById(project: Project, commitId: Uuid): Commit? { TODO("Not yet implemented") }
    override fun getCommitChange(project: Project, commit: Commit, changeTypes: Collection<ChangeType>): Collection<DataVersion> { TODO("Not yet implemented") }
    override fun getCommitChangeById(project: Project, commit: Commit, changeId: Uuid): DataVersion { TODO("Not yet implemented") }
    override fun getDefaultBranch(project: Project): Branch { TODO("Not yet implemented") }
    override fun createBranch(project: Project, branchName: String, head: Commit): Branch { TODO("Not yet implemented") }
    override fun createCommit(change: Collection<DataVersion>, branch: Branch?, previousCommits: Collection<Commit>, project: Project): Commit { TODO("Not yet implemented") }
    override fun createTag(project: Project, tagName: String, taggedCommit: Commit): Tag { TODO("Not yet implemented") }
    override fun deleteBranch(project: Project, branchId: Uuid): Branch? { TODO("Not yet implemented") }
    override fun deleteTag(project: Project, tagId: Uuid): Tag? { TODO("Not yet implemented") }
    override fun getBranchById(project: Project, branchId: Uuid): Branch? { TODO("Not yet implemented") }
    override fun getBranches(project: Project): Collection<Branch> { TODO("Not yet implemented") }
    override fun getCommit(project: Project): Collection<Commit> { TODO("Not yet implemented") }
    override fun getHeadCommit(project: Project, branch: Branch?): Commit? { TODO("Not yet implemented") }
    override fun getTagById(project: Project, tagId: Uuid): Tag { TODO("Not yet implemented") }
    override fun getTaggedCommit(project: Project, tag: Tag): Commit { TODO("Not yet implemented") }
    override fun getTags(project: Project): Collection<Tag> { TODO("Not yet implemented") }
    override fun setDefaultBranch(project: Project, branchId: Uuid): Project { TODO("Not yet implemented") }
}