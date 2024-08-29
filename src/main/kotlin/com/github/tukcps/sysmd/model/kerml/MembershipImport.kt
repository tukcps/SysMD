package com.github.tukcps.sysmd.model.kerml

/**
 * An Import that imports a specific member of an imported namesapce.
 */
interface MembershipImport: Import {
    var importedMemberName: Resolved<Element>
    override var importedNamespace: Resolved<Namespace>
}