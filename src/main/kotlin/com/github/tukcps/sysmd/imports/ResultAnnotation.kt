package com.github.tukcps.sysmd.imports

import com.github.tukcps.sysmd.ui.viewmodel.CellViewModel

class ResultAnnotation(
    val cell: CellViewModel,
    val lineNumber: Int,
    val newMinValue: Double,
    val newMaxValue: Double,
    val conflictMinValue : Boolean,
    val conflictMaxValue : Boolean
)