package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature

interface Usage: Feature {
    val isVariation: Boolean
        get() = false // @Todo this should be replaced by real implementation
    val isReference: Boolean
        get() = !isComposite
}