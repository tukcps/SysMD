package com.github.tukcps.sysmd.model.kerml

import java.util.*

interface Membership: Relationship {
    val memberId: UUID?
        get() = memberElement.elementId
    val memberName: String?
    val memberShortName: String?

    val memberElement: Element
        get() = target.firstOrNull()!!.ref!!

    val visibility: Import.VisibilityKind
}