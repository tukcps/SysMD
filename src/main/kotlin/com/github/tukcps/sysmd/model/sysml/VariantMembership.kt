package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.OwningMembership

interface VariantMembership : OwningMembership {
    val ownedVariantUsage: Usage
}
