package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.UUID

open class MembershipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    override val visibility: Import.VisibilityKind = Import.VisibilityKind.Public,
    elementType: String = "Membership",
) : RelationshipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
), Membership {
    override val memberName: String?
        get() = target.firstOrNull()?.ref?.declaredName
    override val memberShortName: String?
        get() = target.firstOrNull()?.ref?.declaredShortName
}