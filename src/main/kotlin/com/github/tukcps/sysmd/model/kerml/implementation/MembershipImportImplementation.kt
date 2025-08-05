package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.QualifiedName


/**
 * An Import is a Relationship between an importOwningNamespace in which one or more of the visible
 * Memberships of the importedNamespace become importedMemberships of the importOwningNamespace.
 * @param isImportAll If isImportAll = false (the default), then only public Memberships are considered "visible". If isImportAll =
 * true, then all Memberships are considered "visible", regardless of their declared visibility.
 * If no importedMemberName is given, then all visible Memberships are imported from the importedNamespace.
 * If isRecursive = true, then visible Memberships are also recursively imported from all visible ownedMembers
 * of the Namespace that are also Namespaces.
 * @param importedMemberName If an importedMemberName is given, then the Membership whose effectiveMemberName is that name is
 * imported from the importedNamespace, if it is visible.
 * @param isRecursive If isRecursive = true and the imported
 * memberElement is a Namespace, then visible Memberships are also recursively imported from that Namespace and
 * its owned sub-Namespaces.
 */
class MembershipImportImplementation(
    var importedMemberName: QualifiedName? = null,
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Private,
    override var isRecursive: Boolean = true,                            // False by default in SysMLv2
    override var isImportAll: Boolean = false,
    override var importedMembership: Membership = UnresolvedMembership(),
    elementType: String = "MembershipImport",
): MembershipImport, MembershipImplementation(
    elementType = elementType
) {
    override val importOwningNamespace: Namespace?
        get() = owningNamespace

    override fun importedMemberships(excluded: Set<Namespace>): MutableSet<Membership> {
        TODO("Not yet implemented")
    }


    override fun toString(): String = "MembershipImport { $importedMemberName $importedMemberName}"

    override fun clone() : MembershipImport{
        return MembershipImportImplementation(
            visibility = visibility,
            isRecursive = isRecursive,
            isImportAll = isImportAll
        ).also {
            importedMembership = importedMembership
            it.model = model
        }
    }


    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is MembershipImport) {
            importedMembership = template.importedMembership
            visibility = template.visibility
            isImportAll = template.isImportAll
            isRecursive = template.isRecursive
        }
    }
}