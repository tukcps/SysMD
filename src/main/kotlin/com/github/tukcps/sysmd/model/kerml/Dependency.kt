package com.github.tukcps.sysmd.model.kerml


interface Dependency : Relationship {
    var client: MutableList<Resolved<Element>>
    var supplier: MutableList<Resolved<Element>>
}