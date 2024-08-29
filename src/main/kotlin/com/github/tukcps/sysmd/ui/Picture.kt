package com.github.tukcps.sysmd.ui

import androidx.compose.ui.graphics.ImageBitmap

data class Picture (
    var source: String = "",
    var name: String = "",
    var image: ImageBitmap,
    var width: Int = 0,
    var height: Int = 0,
    var id: Int = 0,
)