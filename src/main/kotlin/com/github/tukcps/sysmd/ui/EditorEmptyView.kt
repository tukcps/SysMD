@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.styles.AppTheme

@Composable
fun EditorEmptyView() = Box(Modifier.fillMaxSize()) {
    Column(Modifier.align(Alignment.Center)) {


        Spacer(Modifier.height(50.dp))

        Text(
            """This is SysMD Notebook version ${AppTheme.version}""".trimMargin(),
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(6.dp)
        )

        Text(
            "No project in active session; select one in the navigation panel left.",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(6.dp)
        )

        Text(
            "Select \"SysMD Kickstart\" for a brief introduction.",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(6.dp)
        )

        Spacer(Modifier.height(50.dp))
    }
}