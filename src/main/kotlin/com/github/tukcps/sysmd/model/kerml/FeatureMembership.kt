package com.github.tukcps.sysmd.model.kerml

interface FeatureMembership: OwningMembership
{
    var ownedMemberFeature : Feature
        get() = ownedMemberElement as Feature
        set(value) { ownedMemberElement = value }

    var owningType : Type
        get() = membershipOwningNamespace as Type
        set(value) { membershipOwningNamespace = value }

    override fun clone() : FeatureMembership
}