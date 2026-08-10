package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.MetadataFeature

interface MetadataUsage : ItemUsage, MetadataFeature {
    val metadataDefinition: Metaclass?
}
