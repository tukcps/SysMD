package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.ParameterMembership

interface ActorMembership : ParameterMembership {
    val ownedActorParameter: PartUsage
}
