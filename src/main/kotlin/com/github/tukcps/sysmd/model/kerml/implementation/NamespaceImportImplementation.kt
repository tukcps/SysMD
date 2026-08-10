package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.ErrorElement
import com.github.tukcps.sysmd.model.util.SimpleName
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
open class NamespaceImportImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    importingNamespace: Namespace = UnresolvedNamespace(model),
    importedNamespace: Namespace = UnresolvedNamespace(model),
): NamespaceImport, RelationshipImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = importingNamespace,
    source = mutableListOf(importingNamespace),
    target = mutableListOf(importedNamespace),
) {
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Public
    override var isRecursive: Boolean = false  // False by default in SysML v2
    override var isImportAll: Boolean = false

    override val importedElement: Namespace
        get() = target.first() as Namespace

    override fun importedMemberships(
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> {
        if (!isRecursive)
            return importedNamespace.visibleMemberships(excluded) { filter() }
        else {
            val found = importedNamespace.visibleMemberships(excluded) { filter() }
            if (found.isNotEmpty()) return found
            importedNamespace.ownedImport.forEach {
                val found = it.importedMemberships(excluded) { filter() }
                if (found.isNotEmpty()) return found
            }
        }
        return emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    override var importedNamespace: Namespace
        get() = target.firstOrNull() as? Namespace ?: ErrorElement(model, target.firstOrNull())
        set(value) { target = mutableListOf(value) }

    override fun toString(): String = super.toString() +
            if (visibility != Import.VisibilityKind.Public) ", $visibility" else "" +
            if (isRecursive) ", recursive" else "" +
            if (isImportAll) ", importAll" else ""

    override fun clone() : NamespaceImport = NamespaceImportImplementation(
        model,
        importedNamespace = importedNamespace,
    ).also { it.updateFrom(this) }


    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is NamespaceImport) {
            importedNamespace = template.importedNamespace
        }
    }
}