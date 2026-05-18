package com.github.tukcps.sysmd.model.kerml

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
interface Import: Relationship {

    enum class VisibilityKind {
        Public,
        Private,
        Protected
    }

    var visibility: VisibilityKind

    // Whether to recursively import Memberships from visible, owned sub-Namespaces.
    var isRecursive: Boolean              // False by default in SysML v2
    var isImportAll: Boolean

    val importOwningNamespace: Namespace?
        get() = owningNamespace

    /**
     * Either the imported Membership's element or the imported Namespace.
     */
    val importedElement: Element

    fun importedMemberships(
        excluded: Set<Namespace> = emptySet(),
        filter: Membership.() -> Boolean = { true }
    ): List<Membership>
}