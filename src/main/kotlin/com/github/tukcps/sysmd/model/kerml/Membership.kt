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

    /**
     * The member element
     * @return the member element cast to template class or null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Element> member() = memberElement as T?

    var visibility: Import.VisibilityKind

    @Deprecated("use memberElement.path()", replaceWith = ReplaceWith("memberElement.path()"))
    fun memberQualifiedName() = memberElement.path()

    override fun clone(): Membership
}