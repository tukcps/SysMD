package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Classifier

interface Definition: Classifier {
    val isVariation: Boolean
}