package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Interaction
import com.github.tukcps.sysmd.model.sysml.FlowUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class FlowUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : FlowUsage, ActionUsageImplementation(model,elementId = elementId)
{
    override val flowDefinition: MutableList<Interaction> = TODO()
    override val association: Association
        get() = TODO("Not yet implemented")
    override var from: MutableList<Element>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var to: MutableList<Element>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isDirected: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    /** The sources the relationship */
    override var source: MutableList<Element>
        get() = TODO("Not yet implemented")
        set(value) {}

    /** The targets of the relationship */
    override var target: MutableList<Element>
        get() = TODO("Not yet implemented")
        set(value) {}

    /** The owning element of this relationship */
    override var owningRelatedElement: Element
        get() = TODO("Not yet implemented")
        set(value) {}

    /** The owned elements */
    override var ownedRelatedElement: MutableList<Element>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isImplied: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun clone() = FlowUsageImplementation(model)
}
