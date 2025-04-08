package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.exceptions.Issue
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
    importingNamespace: Resolved<Element> = Resolved(),
    importedNamespace: Resolved<Namespace> = Resolved(),
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Private,
    override var isRecursive: Boolean = true,              // False by default in SysMLv2
    override var isImportAll: Boolean = false,
    elementType: String = "NamespaceImport"
): NamespaceImport, RelationshipImplementation(
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    source = mutableListOf(importingNamespace),
    target = mutableListOf(importedNamespace),
    elementType = elementType
) {
    override val importOwningNamespace: Namespace?
        get() = owningNamespace

    @Suppress("UNCHECKED_CAST")
    override var importedNamespace: Resolved<Namespace>
        get() = try { target[0] as Resolved<Namespace>
        } catch (e: Exception) { model?.status?.fatal("Problem with import", cause = e)
            Resolved(model!!.global) }
        set(value) { target[0].ref = value.ref; target[0].str = value.str; target[0].id = value.id }

    override fun toString(): String = "Import { importedNamespace = $importedNamespace }"

    override fun clone() : NamespaceImport {
        return NamespaceImportImplementation(
            visibility = visibility,
            isRecursive = isRecursive,
            isImportAll = isImportAll
        ).also {
            it.importedNamespace = Resolved(importedNamespace)
            it.model = model
        }
    }

    override fun resolveNames(): Boolean {
        updated = super.resolveNames() or updated
        if (source.size > 1)
            model?.status?.error("imports can have only a single source", kind = Issue.Kind.ERROR_SEMANTIC)
        if (importOwningNamespace !is Namespace)
            model?.status?.error("only Packages and Namespaces can import", kind = Issue.Kind.ERROR_SEMANTIC)
        if (target.size < 1)
            model?.status?.error("import: nothing imported", kind = Issue.Kind.ERROR_SEMANTIC)
        target.forEach {
            if(it.ref !is Namespace?)
                model?.status?.error("only Packages and Namespaces can be imported", kind = Issue.Kind.ERROR_SEMANTIC)
        }

        updated = importedNamespace.resolveIdentity(owningNamespace!!)
        if (importedNamespace.ref != null) {
            target[0] = importedNamespace
        }
        return updated
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Import) {
            importedNamespace = template.importedNamespace
            visibility = template.visibility
            isImportAll = template.isImportAll
            isRecursive = template.isRecursive
        }
    }
}