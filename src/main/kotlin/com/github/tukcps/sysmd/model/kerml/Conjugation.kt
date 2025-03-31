package com.github.tukcps.sysmd.model.kerml

interface Conjugation: Relationship {
    val conjugated: Resolved<Element>?
    val type: Resolved<Type>?
}