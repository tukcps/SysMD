@file:Suppress("unused")
package com.github.tukcps.sysmd.rest.entities.api.entities

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import kotlin.uuid.Uuid

interface CommitDataObject {
    var id: Uuid
    var type: DataVersionType
    var payloadElementSnapshot: ElementDataIF? // not in standard; allows us faster operation
    var element: ElementDataIF?
    var projectUsage: ProjectUsage?

    /**
     * In a commit, we distinguish four cases:
     * - Change of an element
     * - Change of a relationship,
     * - Deleting an element from a commit,
     * - Project usage = reference to another project to be added to session.
     */
    enum class DataVersionType {
        ChangedElement, ChangedRelationship, DeletedInCommit, ProjectUsage
    }
}

/**
 * Filter function on Collections of CommitDataObject to find all snapshots.
 */
fun Collection<CommitDataObject>.getElements() = filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
fun Collection<CommitDataObject>.getUsages() = filter { it.projectUsage != null }.mapNotNull { it.projectUsage }