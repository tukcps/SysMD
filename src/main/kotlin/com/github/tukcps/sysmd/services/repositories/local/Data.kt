package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.rest.entities.api.entities.CommitDataObject
import com.github.tukcps.sysmd.rest.entities.api.entities.ProjectUsage
import kotlin.uuid.Uuid

/**
 * Implementation of a Commit Data Object of SysML v2 API
 * @param element the Element DAO with the raw data diff
 * @param payloadElementSnapshot the element DAO with a payload snapshot (= no diff, complete data)
 * @param projectUsage a project usage reference
 */
class Data(
    override var element: ElementDataIF? = null,
    override var payloadElementSnapshot: ElementDataIF? = null,
    override var projectUsage: ProjectUsage? = null,
) : CommitDataObject {
    override var id: Uuid = Uuid.random()
    override var type: CommitDataObject.DataVersionType = if (projectUsage == null)
        CommitDataObject.DataVersionType.ChangedElement
    else
        CommitDataObject.DataVersionType.ProjectUsage

    override fun toString(): String = "Data (" +
            (if (payloadElementSnapshot != null) "Feature snapshot: ${payloadElementSnapshot!!.type} '${payloadElementSnapshot?.declaredName?:payloadElementSnapshot?.declaredShortName?:""}'" else "") +
            (if (element != null) "Feature diff: ${payloadElementSnapshot!!.type} '${payloadElementSnapshot?.declaredName?:payloadElementSnapshot?.declaredShortName?:""}'" else "") +
            (if (projectUsage != null) "Project usage: +$projectUsage" else "") + ")"
}