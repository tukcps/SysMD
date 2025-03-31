package com.github.tukcps.sysmd.services.repositories.local

import io.github.tukcps.sysmlv2.api.services.ProjectDataVersioningService
import io.github.tukcps.sysmlv2.api.entities.*
import io.github.tukcps.sysmlv2.api.services.ChangeType
import java.util.*


/**
 * There is simply no versioning in SysMD Notebook client.
 */
object SysMDProjectDataVersioningService: ProjectDataVersioningService {
    override fun getCommitById(project: Project, commitId: UUID): Commit? { TODO("Not yet implemented") }
    override fun getCommitChange(project: Project, commit: Commit, changeTypes: Collection<ChangeType>): Collection<DataVersion> { TODO("Not yet implemented") }
    override fun getCommitChangeById(project: Project, commit: Commit, changeId: UUID): DataVersion { TODO("Not yet implemented") }
    override fun getDefaultBranch(project: Project): Branch { TODO("Not yet implemented") }
    override fun createBranch(project: Project, branchName: String, head: Commit): Branch { TODO("Not yet implemented") }
    override fun createCommit(change: Collection<DataVersion>, branch: Branch?, previousCommits: Collection<Commit>, project: Project): Commit { TODO("Not yet implemented") }
    override fun createTag(project: Project, tagName: String, taggedCommit: Commit): Tag { TODO("Not yet implemented") }
    override fun deleteBranch(project: Project, branchId: UUID): Branch? { TODO("Not yet implemented") }
    override fun deleteTag(project: Project, tagId: UUID): Tag? { TODO("Not yet implemented") }
    override fun getBranchById(project: Project, branchId: UUID): Branch? { TODO("Not yet implemented") }
    override fun getBranches(project: Project): Collection<Branch> { TODO("Not yet implemented") }
    override fun getCommit(project: Project): Collection<Commit> { TODO("Not yet implemented") }
    override fun getHeadCommit(project: Project, branch: Branch?): Commit? { TODO("Not yet implemented") }
    override fun getTagById(project: Project, tagId: UUID): Tag { TODO("Not yet implemented") }
    override fun getTaggedCommit(project: Project, tag: Tag): Commit { TODO("Not yet implemented") }
    override fun getTags(project: Project): Collection<Tag> { TODO("Not yet implemented") }
    override fun setDefaultBranch(project: Project, branchId: UUID): Project { TODO("Not yet implemented") }
}