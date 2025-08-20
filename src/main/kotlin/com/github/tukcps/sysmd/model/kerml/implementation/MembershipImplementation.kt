package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement
import com.github.tukcps.sysmd.model.kerml.UnresolvedNamespace
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
        get() = target.firstOrNull()?.declaredName
    override val memberShortName: String?
        get() = target.firstOrNull()?.declaredShortName

    override var owningRelatedElement: Element
        get() = source.first()
        set(value) { source = mutableListOf(value) }
}