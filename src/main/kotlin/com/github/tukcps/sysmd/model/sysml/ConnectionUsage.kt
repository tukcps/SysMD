package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.AssociationStructure

interface ConnectionUsage: ConnectorAsUsage, PartUsage {
    val connectionDefinition: MutableList<AssociationStructure>
}