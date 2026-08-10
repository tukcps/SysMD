package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.FeatureTyping

interface ConjugatedPortTyping : FeatureTyping {

    var conjugatedPortDefinition: ConjugatedPortDefinition
    val portDefinition: PortDefinition

}
