package com.github.tukcps.sysmd.imports

import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel

class ResultAnnotation(val cell: TextualRepresentationViewModel,val lineNumber: Int,val newMinValue: Double,val newMaxValue: Double, val conflict_minValue : Boolean, val conflict_maxValue : Boolean) {
}