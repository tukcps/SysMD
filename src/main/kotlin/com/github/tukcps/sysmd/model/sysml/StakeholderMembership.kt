package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.ParameterMembership

interface StakeholderMembership : ParameterMembership {
    val ownedStakeholderParameter: PartUsage
}
