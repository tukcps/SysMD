package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.SimpleName


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
class NamespaceImportImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    importingNamespace: Namespace = UnresolvedNamespace(),
    importedNamespace: Namespace = UnresolvedNamespace(),
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Private,
    override var isRecursive: Boolean = true,              // False by default in SysMLv2
    override var isImportAll: Boolean = false,
    elementType: String = "NamespaceImport"
): NamespaceImport, RelationshipImplementation(
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = importingNamespace,
    source = mutableListOf(importingNamespace),
    target = mutableListOf(importedNamespace),
    elementType = elementType
) {

    override val importOwningNamespace: Namespace?
        get() = owningNamespace

    override fun importedMemberships(excluded: Set<Namespace>): MutableSet<Membership> {
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override var importedNamespace: Namespace
        get() = target.first() as Namespace
        set(value) { target = mutableListOf(value) }

    override fun toString(): String = super<RelationshipImplementation>.toString() +
            if (visibility != Import.VisibilityKind.Public) ", " + visibility.toString() else "" +
            if (isRecursive) ", recursive" else "" +
            if (isImportAll) ", importAll" else ""

    override fun clone() : NamespaceImport {
        return NamespaceImportImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            importedNamespace = importedNamespace,
            visibility = visibility,
            isRecursive = isRecursive,
            isImportAll = isImportAll
        ).also {
            it.importedNamespace = importedNamespace
            it.model = model
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is NamespaceImport) {
            importedNamespace = template.importedNamespace
            visibility = template.visibility
            isImportAll = template.isImportAll
            isRecursive = template.isRecursive
        }
    }
}