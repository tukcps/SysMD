package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * This file is just for quick testing.
 * Do not write anything important in here
 */
@Preview
@Composable
fun Preview() {
    val stateHorizontal = rememberScrollState(0)
    Column(Modifier
        .width(100.dp)
        .background(Color.Blue)
    )
    {
        BasicText("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest123",
            modifier = Modifier.background(Color.Red).horizontalScroll(stateHorizontal)
            ,
        )
        BasicText("this", modifier = Modifier.background(Color.Green))
        HorizontalScrollbar(adapter = rememberScrollbarAdapter(stateHorizontal))
    }
}
