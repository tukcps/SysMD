package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.SimpleName

open class MembershipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    memberElement: Element = UnresolvedElement(),
    membershipOwningNamespace: Namespace = UnresolvedNamespace(),
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Public,
    elementType: String = "Membership",
) : RelationshipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = membershipOwningNamespace,
    target = mutableListOf(memberElement),
    source = mutableListOf(membershipOwningNamespace),
    elementType = elementType
), Membership {

    override val memberName: String?
        get() = target.firstOrNull()?.name

    override val memberShortName: String?
        get() = target.firstOrNull()?.shortName

    override var owningRelatedElement: Element
        get() = source.first()
        set(value) { source = mutableListOf(value) }
}