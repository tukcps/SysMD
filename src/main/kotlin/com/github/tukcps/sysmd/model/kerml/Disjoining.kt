package com.github.tukcps.sysmd.model.kerml

interface Disjoining: Relationship {
    var typeDisjoined: Type  // Source
    var disjoiningType: Type // Target
}