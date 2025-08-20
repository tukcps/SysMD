package com.github.tukcps.sysmd.model.kerml


interface Comment : AnnotatingElement {
    override fun clone(): Comment
    override fun toString(): String
    var locale: String?
}
