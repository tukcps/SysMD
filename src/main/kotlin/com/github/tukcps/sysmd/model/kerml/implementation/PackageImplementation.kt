package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.util.SimpleName


class PackageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    elementType: String = "Package"
): Package, NamespaceImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    init {
        this.isStandard = (owner?.isStandard == true) or isStandard
        this.isLibraryElement = (owner?.isStandard == true) or isLibraryElement
    }

    override fun clone(): Package = PackageImplementation().also { klon -> klon.updateFrom(this) }
    override fun toString() = super.toString() + if (isStandard) " (standard library)" else ""
}
