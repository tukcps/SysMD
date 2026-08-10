package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


open class PackageImplementation(
    model: Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    isStandard: Boolean = false,
): Package, NamespaceImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    init {
        this.isStandard = (owner?.isStandard == true) or isStandard
    }

    override fun updateFrom(template: Element) {
        isStandard = template.isStandard
        super.updateFrom(template)
    }

    override fun clone(): Package = PackageImplementation(model).also { klon ->
        klon.isStandard = isStandard
        klon.updateFrom(this)
    }
    override fun toString() = super.toString() + if (isStandard) " (standard library)" else ""
}
