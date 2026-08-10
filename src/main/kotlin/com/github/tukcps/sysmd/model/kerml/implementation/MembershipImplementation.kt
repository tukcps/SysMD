package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class MembershipImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    memberElement: Element = UnresolvedElement(model),
    membershipOwningNamespace: Namespace = UnresolvedNamespace(model),
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Public,
) : RelationshipImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = membershipOwningNamespace,
    source = mutableListOf(membershipOwningNamespace),
    target = mutableListOf(memberElement),
), Membership {

    override val memberName: String?
        get() = target.firstOrNull()?.name

    override val memberShortName: String?
        get() = target.firstOrNull()?.shortName

    override var owningRelatedElement: Element
        get() = source.first()
        set(value) { source = mutableListOf(value) }

    override fun clone(): Membership = MembershipImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        membershipOwningNamespace = membershipOwningNamespace,
        memberElement = memberElement
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        check(template is Membership)
        this.visibility = template.visibility
        super.updateFrom(template)
    }
}