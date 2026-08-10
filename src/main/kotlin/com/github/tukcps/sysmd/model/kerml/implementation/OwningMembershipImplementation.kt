package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class OwningMembershipImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    memberElement: Element = UnresolvedElement(model),
    membershipOwningNamespace: Namespace = UnresolvedNamespace(model),
    elementType: String = "OwningMembership",
): OwningMembership, MembershipImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    memberElement = memberElement,
    membershipOwningNamespace = membershipOwningNamespace,
) {
    override fun clone(): OwningMembership = OwningMembershipImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        memberElement = memberElement,
        membershipOwningNamespace = membershipOwningNamespace,
    )

    override fun toString(): String {
        fun describe(e : Element) = when(e) {
            is Unresolved -> "Unresolved(${e.relativeName ?: e.id})"
            else -> e.path()
        }

        return "[${elementType().name}] ${describe(owningRelatedElement)} owns ${describe(memberElement)}"
    }
}