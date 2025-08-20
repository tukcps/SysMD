package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*


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
class MembershipImportImplementation(
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Private,
    override var isRecursive: Boolean = false,     // False by default in SysMLv2
    override var isImportAll: Boolean = false,
    owningRelatedElement: Element = UnresolvedNamespace(),
    elementType: String = "MembershipImport",
): MembershipImport, RelationshipImplementation(
    owningRelatedElement = owningRelatedElement,
    elementType = elementType
) {
    override val importOwningNamespace: Namespace?
        get() = owningNamespace

    @Suppress("UNCHECKED_CAST")
    override fun importedMemberships(excluded: Set<Namespace>): MutableSet<Membership> {
        val targetsResolved = target
            .filter { it !is Unresolved && it !in excluded }
            .map { it.owningRelationship }.toMutableSet() as MutableSet<Membership>
        return targetsResolved
    }

    override fun clone() : MembershipImport{
        return MembershipImportImplementation(
            visibility = visibility,
            isRecursive = isRecursive,
            isImportAll = isImportAll
        ).also {
            it.updateFrom(this)
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is MembershipImport) {
            visibility = template.visibility
            isImportAll = template.isImportAll
            isRecursive = template.isRecursive
        }
    }
}