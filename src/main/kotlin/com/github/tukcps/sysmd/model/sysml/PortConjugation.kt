package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Conjugation

interface PortConjugation : Conjugation {

    val conjugatedPortDefinition: ConjugatedPortDefinition
    var originalPortDefinition: PortDefinition
}
