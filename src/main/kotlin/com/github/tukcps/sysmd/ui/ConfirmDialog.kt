@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.ui.styles.AppTheme

/** Preview, requires plugin */
@Preview
@Composable
fun ConfirmPreview() = ConfirmDialog(true, "test dialog", {}, {} )


@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    text: String,
    onCloseRequest: () -> Unit ,
    onConfirm: () -> Unit
) {
    if (showDialog)
        DialogWindow(onCloseRequest = onCloseRequest,
            state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(300.dp, 180.dp)),
            title = "Confirmation",
            resizable = false)
        {
            Column(modifier = Modifier.fillMaxSize().padding(all = 5.dp)) {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(Icons.Default.Warning, text, tint = AppTheme.colors.iconRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(textAlign = TextAlign.Center, text = text)
                    Spacer(modifier = Modifier.width(15.dp))
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Spacer(modifier = Modifier.width(15.dp))
                    TextButton(
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { onConfirm(); onCloseRequest() }) { Text("Delete") }
                    Spacer(modifier = Modifier.width(15.dp))
                    TextButton(
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = onCloseRequest
                    ) { Text("Cancel") }
                }
            }
        }
}

@Composable
fun ConfirmInfoDialog(
    showCommitConfirmDialog: Boolean,
    text: String,
    onCloseRequest: () -> Unit ,
) {
    if (showCommitConfirmDialog) DialogWindow(onCloseRequest = onCloseRequest,
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(300.dp, 180.dp)),
        title = "Confirmation",
        resizable = false
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 5.dp),
            )
            {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ){
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        text,
                        tint = AppTheme.colors.iconGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(textAlign = TextAlign.Center, text = text)
                    Spacer(modifier = Modifier.width(15.dp))
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(15.dp))
                    TextButton(
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = onCloseRequest
                    ) { Text("OK") }
                }
            }
    }
}

@Composable
fun ConfirmAlertDialog(
    showCommitAlertDialog : Boolean,
    text: String,
    onCloseRequest: () -> Unit ,
) {
    if (showCommitAlertDialog) DialogWindow(onCloseRequest = onCloseRequest,
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(300.dp, 180.dp)),
        title = "Alert",
        resizable = false
    ) {
            Column(modifier = Modifier.fillMaxSize().padding(all = 5.dp)) {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Icon(Icons.Default.Warning, text, tint = AppTheme.colors.iconRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(textAlign = TextAlign.Center, text = text)
                    Spacer(modifier = Modifier.width(15.dp))
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(15.dp))
                    TextButton(
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = onCloseRequest
                    ) { Text("OK") }
                }
            }
    }
}