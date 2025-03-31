package com.github.tukcps.sysmd.services.repositories.local

import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.ProjectUsage
import java.util.*

/**
 * Implementation of a Commit Data Object of SysML v2 API
 * @param element the Element DAO with the raw data diff
 * @param payloadElementSnapshot the element DAO with a payload snapshot (= no diff, complete data)
 * @param projectUsage a project usage reference
 */
class Data(
    override var element: ElementDAO? = null,
    override var payloadElementSnapshot: ElementDAO? = null,
    override var projectUsage: ProjectUsage? = null,
) : CommitDataObject {
    override var id: UUID = UUID.randomUUID()
    override var type: CommitDataObject.DataVersionType = if (projectUsage == null)
        CommitDataObject.DataVersionType.ChangedElement
    else
        CommitDataObject.DataVersionType.ProjectUsage

    override fun toString(): String = "Data (" +
            (if (payloadElementSnapshot != null) "Feature snapshot: ${payloadElementSnapshot!!.type} '${payloadElementSnapshot?.name?:payloadElementSnapshot?.shortName?:""}'" else "") +
            (if (element != null) "Feature diff: ${payloadElementSnapshot!!.type} '${payloadElementSnapshot?.name?:payloadElementSnapshot?.shortName?:""}'" else "") +
            (if (projectUsage != null) "Project usage: +$projectUsage" else "") + ")"
}