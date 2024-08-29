package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmlv2.entities.CommitDataObject
import com.github.tukcps.sysmlv2.entities.ElementDAO
import com.github.tukcps.sysmlv2.entities.ProjectUsage
import java.util.*

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