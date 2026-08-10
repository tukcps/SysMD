package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.DataType

interface AttributeUsage : Usage {

    val attributeDefinition: MutableList<DataType>
    override val isReference: Boolean

}