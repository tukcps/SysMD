@file:Suppress("FunctionName")
package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.helper.hoverIndicator

/**
 * A simple input field that is more space economic than the Material Outline or TextField
 * Composable. It requires only a single line and can easily be adapted in size.
 * @param modifier
 * @param value the current input
 * @param onValueChange lambda to be called after change of value
 * @param textStyle Textstyle
 * @param text initial text that shall nudge user to enter data
 * @param singleLine whether only a single line shall be used; default
 * @param check lambda that checks whether input value is ok; if not, background will be red.
 */
@Composable
fun InputField(
    modifier: Modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
    text: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    check: (String) -> Boolean = { true },
    readOnly: Boolean = false
) {
    val textStyleMerged = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyLarge.fontSize).merge(textStyle)
    var ok: Boolean by remember { mutableStateOf(true) }
    fun isOK(input: String) { ok = check(input) }
    Box(modifier) {
        if (readOnly)
            Text(modifier = modifier.padding(all=0.dp), text = value, style = textStyleMerged, maxLines = if (singleLine) 1 else maxLines)
        else BasicTextField(
            value = value,
            onValueChange = { onValueChange(it); isOK(it) },
            modifier = if (ok) modifier.padding(all = 0.dp).fillMaxSize().hoverIndicator()
            else modifier.background(MaterialTheme.colorScheme.errorContainer).padding(all = 0.dp).fillMaxSize().hoverIndicator(),
            textStyle = if (ok) textStyleMerged else textStyleMerged.merge(color = MaterialTheme.colorScheme.error),
            singleLine = singleLine,
            maxLines = maxLines,
            readOnly = readOnly,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground)
        )
        if (value.isBlank())
            Text(text,
                modifier = modifier.padding(all = 0.dp).fillMaxSize(),
                style = textStyleMerged.merge(color = Color.Gray),
                maxLines = maxLines
            )
    }
}