package com.github.tukcps.sysmd.model.kerml

interface OwningMembership: Membership
{
    /** The element that becomes an owned member of `membershipOwningNamespace` */
    var ownedMemberElement : Element
        get() = memberElement
        set(value) { memberElement = value }

    override fun clone() : OwningMembership
}