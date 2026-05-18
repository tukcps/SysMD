package com.github.tukcps.sysmd.ui.helper


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }