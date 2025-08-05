package com.github.tukcps.sysmd.model.kerml

interface Conjugation: Relationship {
    var conjugatedType: Type
    var originalType: Type
}