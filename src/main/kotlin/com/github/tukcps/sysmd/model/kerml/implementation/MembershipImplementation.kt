package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership

open class MembershipImplementation(
    override var memberName: String? = null,
    override var memberShortName: String? = null,
    override val visibility: Import.VisibilityKind = Import.VisibilityKind.Public,
    elementType: String = "Membership",
) : RelationshipImplementation(
    elementType = elementType
), Membership