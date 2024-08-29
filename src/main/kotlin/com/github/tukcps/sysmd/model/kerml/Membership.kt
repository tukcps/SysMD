package com.github.tukcps.sysmd.model.kerml

import java.util.*

interface Membership: Relationship {
    val memberId: UUID?
        get() = memberElement.elementId
    var memberName: String?
    var memberShortName: String?

    val memberElement: Element
        get() = target.firstOrNull()!!.ref!!

    val visibility: Import.VisibilityKind
}