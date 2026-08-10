package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.ImportImplementation
import com.github.tukcps.sysmd.model.sysml.Expose
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

abstract class ExposeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : Expose, ImportImplementation(model, elementId = elementId)
{
    override var isImportAll: Boolean = TODO()

    /**
     * Either the imported Membership's element or the imported Namespace.
     */
    override val importedElement: Element
        get() = TODO("Not yet implemented")

    override fun importedMemberships(
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> {
        TODO("Not yet implemented")
    }

    override var visibility: Import.VisibilityKind = TODO()
}
