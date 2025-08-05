package com.github.tukcps.sysmd.model.kerml

interface Specialization: Relationship {

    var general: Type
    var specific: Type

    override fun clone(): Specialization
    override fun updateFrom(template: Element)
}