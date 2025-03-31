package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.util.SimpleName

open class OwningMembershipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "OwningMembership",
): OwningMembership, MembershipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): OwningMembership {
        return OwningMembershipImplementation().also {
            source = Resolved.copyOfIdentityList(source)
            target = Resolved.copyOfIdentityList(target)
        }
    }
}