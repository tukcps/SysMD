package com.github.tukcps.sysmd.model.kerml

interface LibraryPackage : Package {
    override var isStandard: Boolean
    fun libraryNamespace(): Namespace?
}
