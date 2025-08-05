package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace
import com.github.tukcps.sysmd.model.util.SimpleName

open class OwningMembershipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    memberElement: Element = UnresolvedElement(),
    membershipOwningNamespace: Namespace = UnresolvedNamespace(),
    elementType: String = "OwningMembership",
): OwningMembership, MembershipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    memberElement = memberElement,
    membershipOwningNamespace = membershipOwningNamespace,
    elementType = elementType
) {
    override fun clone(): OwningMembership {
        return OwningMembershipImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            memberElement = memberElement,
            membershipOwningNamespace = membershipOwningNamespace,
        )
    }

    override fun toString() = "[OwningMembership] ${membershipOwningNamespace.escapedName()} owns ${memberElement.escapedName()}"
}