package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.Resolved
import java.util.*

open class OwningMembershipImplementation(
    elementType: String = "OwningMembership",
): OwningMembership, MembershipImplementation(
    elementType = elementType
) {
    override fun clone(): OwningMembership {
        return OwningMembershipImplementation().also {
            it.elementId = UUID.randomUUID()
            source = Resolved.copyOfIdentityList(source)
            target = Resolved.copyOfIdentityList(target)
        }
    }
}