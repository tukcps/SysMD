@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.viewmodel.MyIcons


@Composable
fun ButtonSelection( selected: MutableState<Boolean> , text: String) {
    Row(Modifier.padding(start = 10.dp).height(24.dp).fillMaxWidth().clickable { selected.value = !selected.value} ) {
        if (selected.value)
            Icon(MyIcons.RadioButtonChecked, text, modifier = Modifier.padding(horizontal = 5.dp).height(16.dp).align(
                Alignment.CenterVertically))
        else
            Icon(
                MyIcons.RadioButtonUnchecked, text, modifier = Modifier.padding(horizontal = 5.dp).height(16.dp).align(
                Alignment.CenterVertically))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 1.dp).align(Alignment.CenterVertically))
    }
}

/**
 * A selectable item, i.e., in a list of radio buttons.
 * @param selected String that identifies the button
 * @param text String that is displayed for explanation
 * @param onSelection Lambda that is executed upon selection
 */
@Composable
fun ButtonSelection( selected: MutableState<String>, text: String, onSelection: () -> Unit ) {
    Row(Modifier.padding(start = 10.dp, end = 10.dp).height(24.dp).fillMaxWidth().clickable { selected.value = text} ) {
        if (selected.value == text) {
            Icon(MyIcons.RadioButtonChecked, text, Modifier.padding(end = 10.dp).height(16.dp).align(Alignment.CenterVertically))
            onSelection()
        } else
            Icon(MyIcons.RadioButtonUnchecked, text, Modifier.padding(end = 10.dp).height(16.dp).align(Alignment.CenterVertically))
        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterVertically))
    }
}