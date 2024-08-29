package com.github.tukcps.sysmd.ui.composables



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*


import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.github.tukcps.sysmd.ui.styles.AppTheme



enum class ChosenAgendaTab {
    ERRORS, OUTPUT
}


@Composable
fun AgendaTapView(
    AgendaTapModel: MutableState<ChosenAgendaTab>,
) {

    Row(Modifier.fillMaxWidth().background(AppTheme.colors.backgroundMedium),
        horizontalArrangement = Arrangement.Center
    ) {
        Row{
            Row(
                Modifier
                    .background(if (AgendaTapModel.value != ChosenAgendaTab.ERRORS) AppTheme.colors.backgroundMedium else AppTheme.colors.backgroundLightGray)
                    .padding(horizontal = 5.dp)
                    .align(Alignment.CenterVertically)
                    .clickable(remember(::MutableInteractionSource), indication = null)
                    {
                        AgendaTapModel.value = ChosenAgendaTab.ERRORS
                    }) {
                Icon(
                    Icons.Default.Error,
                    tint = Color.DarkGray,
                    contentDescription = "Error",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                )
                Text(
                    text = "Error",
                    color = Color.Black,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        Row(
            Modifier
                .background(if (AgendaTapModel.value != ChosenAgendaTab.OUTPUT) AppTheme.colors.backgroundMedium else AppTheme.colors.backgroundLightGray)
                .padding(horizontal = 5.dp)
                .align(Alignment.CenterVertically)
                .clickable(remember(::MutableInteractionSource), indication = null) {
                    AgendaTapModel.value = ChosenAgendaTab.OUTPUT
                }) {
            Icon(
                Icons.Default.Subtitles,
                tint = Color.DarkGray,
                contentDescription = "Output",
                modifier = Modifier
                    .size(20.dp)
                    .padding(2.dp)
            )
            Text(
                text = "Output",
                color = Color.DarkGray,
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}



