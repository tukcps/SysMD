package com.github.tukcps.sysmd.model.sysml

interface ConjugatedPortDefinition : PortDefinition {

    val originalPortDefinition: PortDefinition
    val ownedPortConjugator: PortConjugation

    override fun effectiveName(): String?

}
