package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.util.QualifiedName

/**
 * An Import that imports a specific member of an imported namesapce.
 */
interface MembershipImport: Import {
    var importedMembership: Membership
}