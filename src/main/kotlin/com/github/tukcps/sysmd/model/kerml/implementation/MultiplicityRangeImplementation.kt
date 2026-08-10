package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.MultiplicityRange
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class MultiplicityRangeImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    name: String? = "cardinality",
    shortName: String? = null,
): MultiplicityRange, MultiplicityImplementation(
    model,
    elementId = elementId, name, shortName
) {
    override fun clone() = MultiplicityRangeImplementation(model).also { it.updateFrom(this) }
}