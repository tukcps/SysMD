package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData

import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified


/**
 * The payload of a commit
 */
interface CommitData : Identified, ElementDataIF {
    var qualifiedName: String?
    var ownedAnnotation: MutableList<Identified>
}
