package com.github.tukcps.sysmd.rest.entities.api.services
import com.github.tukcps.sysmd.rest.entities.api.entities.*
import kotlin.uuid.Uuid

enum class ChangeType { CREATED, UPDATED, DELETED, }

/**
 * Declaration of the ProjectDataVersioningService PIM API.
 * See documentation of SysML v2 API, p. 27.
 * Status: Incomplete, see below
 */
interface ProjectDataVersioningService {
    fun getCommit(project: Project): Collection<Commit>
    fun getHeadCommit(project: Project, branch: Branch?): Commit?
    fun getCommitById(project : Project, commitId : Uuid) : Commit?
    fun createCommit(change : Collection<DataVersion>, branch: Branch?, previousCommits: Collection<Commit>, project: Project): Commit
    fun getCommitChange(project : Project, commit: Commit, changeTypes: Collection<ChangeType>): Collection<DataVersion>
    fun getCommitChangeById( project : Project, commit : Commit, changeId : Uuid ): DataVersion
    fun getBranches(project : Project) : Collection<Branch>
    fun getBranchById(project : Project, branchId : Uuid): Branch?
    fun getDefaultBranch(project : Project): Branch
    fun setDefaultBranch(project : Project, branchId : Uuid ): Project
    fun createBranch(project : Project, branchName : String, head : Commit ): Branch
    fun deleteBranch(project : Project, branchId : Uuid ): Branch?
    fun getTags(project : Project): Collection<Tag>
    fun getTagById(project : Project, tagId : Uuid): Tag
    fun getTaggedCommit(project : Project, tag : Tag ): Commit
    fun createTag(project : Project, tagName : String, taggedCommit : Commit): Tag
    fun deleteTag(project : Project, tagId: Uuid): Tag?
    // fun mergeIntoBranch(baseBranch : Branch, commitsToMerge: Collection<Commit>, resolution: Collection<Data>, description: String?) : MergeResult
    // fun diffCommits(baseCommit : Commit, compareCommit: Commit, changeTypes: Collection<ChangeType>) : Collection<DataDifference>
}