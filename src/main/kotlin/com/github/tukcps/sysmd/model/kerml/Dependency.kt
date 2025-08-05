package com.github.tukcps.sysmd.model.kerml


interface Dependency : Relationship {
    var client: MutableList<Element>
    var supplier: MutableList<Element>
}