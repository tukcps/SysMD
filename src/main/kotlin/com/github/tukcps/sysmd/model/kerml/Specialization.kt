package com.github.tukcps.sysmd.model.kerml

interface Specialization: Relationship {

    var general: Resolved<Type>
    var specific: Resolved<Type>

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}