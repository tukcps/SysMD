package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.MembershipImport
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.model.util.UnresolvedMembership
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


/**
 * An Import is a Relationship between an importOwningNamespace in which one or more of the visible
 * Memberships of the importedNamespace become importedMemberships of the importOwningNamespace.
 * @param isImportAll If isImportAll = false (the default), then only public Memberships are considered "visible". If isImportAll =
 * true, then all Memberships are considered "visible", regardless of their declared visibility.
 * If no importedMemberName is given, then all visible Memberships are imported from the importedNamespace.
 * If isRecursive = true, then visible Memberships are also recursively imported from all visible ownedMembers
 * of the Namespace that are also Namespaces.
 * @param isRecursive If isRecursive = true and the imported
 * memberElement is a Namespace, then visible Memberships are also recursively imported from that Namespace and
 * its owned sub-Namespaces.
 */
class MembershipImportImplementation(model : Session,elementId : Uuid = Uuid.random())
    : MembershipImport, ImportImplementation(model, elementId = elementId)
{
    init {
        source = mutableListOf(UnresolvedNamespace(model))
        target = mutableListOf(UnresolvedMembership(model))
    }

    override val importedElement: Element
        get() = importedMembership

    val importedMembership: Membership
        get() = target.first() as Membership

    /**
     * Returns Memberships that are to become importedMemberships of the importOwningNamespace.
     * @param excluded used to handle the possibility of circular Import Relationships.
     * @return set of memberships that are imported.
     */
    @Suppress("UNCHECKED_CAST")
    override fun importedMemberships(
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> {

        if (importedMembership is Unresolved
            || (importedMembership is Namespace && importedMembership as Namespace in excluded)
            || !importedMembership.filter())
            return listOf()

        if (!isRecursive) {
            return listOf(importedMembership)
        } else {
            return listOf(importedMembership)
        }
    }

    override fun clone() : MembershipImport = MembershipImportImplementation(model).also {
        it.updateFrom(this)
    }
}