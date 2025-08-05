package com.github.tukcps.sysmd.model.kerml

interface Membership: Relationship {
    // val memberElementId: UUID?
    /**
     * The name of the memberElement, relative to the membershipOwningNamespace
     */
    val memberName: String?

    /**
     * The short name of the memberElement, relative to the membershipOwningNamespace
     */
    val memberShortName: String?

    /** The owning namespace */
    var membershipOwningNamespace: Namespace
        get() = source.first() as Namespace
        set(value) { source = mutableListOf(value) }

    /** The member element */
    var memberElement: Element
        get() = target.first()
        set(value) { target = mutableListOf(value) }

    val visibility: Import.VisibilityKind
}