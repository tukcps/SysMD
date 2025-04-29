@file:Suppress("LocalVariableName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.ui.styles.AppTheme
import java.awt.Desktop
import java.net.URI

@Composable
fun displayWarningDirectoryAccess(openDialog: MutableState<Boolean>,uri:MutableState<String>) {
    DialogWindow(
        onCloseRequest = { openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(350.dp, 200.dp)),
        title = "Opening Folder",
        resizable = false
    )   {
        Column(modifier = Modifier.fillMaxSize())
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning opening Directory",
                    tint = AppTheme.colors.iconRed,
                    modifier = Modifier.size(60.dp).padding(start = 20.dp)
                )
                Text(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp),
                    textAlign = TextAlign.Center,
                    text = "You are trying to open a directory. Do you want to open it anyway?"
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                TextButton(
                    modifier = Modifier.padding(end = 15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                    border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                    onClick = {
                        openDialog.value = false
                    }) { Text("No") }


                TextButton(
                    modifier = Modifier.padding(start = 15.dp, end = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                    border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                    onClick = {
                        openDialog.value = false
                        Desktop.getDesktop().browse(URI(uri.value))
                    }
                ) { Text("Open Anyway") }
            }
        }
    }
}

@Composable
fun displayWarningNoSuchFileOrDirectory(openDialog: MutableState<Boolean>) {
    DialogWindow(
        onCloseRequest = {openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(350.dp, 200.dp)),
        title = "No Such File or Directory",
        resizable = false) {
            Column(modifier = Modifier.fillMaxSize())
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = AppTheme.colors.iconRed,
                        modifier = Modifier.size(60.dp).padding(start = 20.dp)
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center, text = "No such file or directory exists."
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { openDialog.value = false }) { Text("Ok") }
                }
            }
        }
}

@Composable
fun displayWarningOpenFileWithDefaultSystemProgram(openDialog: MutableState<Boolean>, uri:MutableState<String>) {
    DialogWindow(
        onCloseRequest = {openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(350.dp, 200.dp)),
        title = "Filetype not Supported by SysMD",
        resizable = false)
    {
            Column(modifier = Modifier.fillMaxSize())
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AppTheme.colors.iconRed,
                        modifier = Modifier.size(60.dp).padding(start = 20.dp)
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center,
                        text = "This file is not supported by SysMD. Do you wish to open it with the system default program?"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = {
                            openDialog.value = false
                            Desktop.getDesktop().browse(URI(uri.value))
                        }) { Text("yes") }
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { openDialog.value = false }) { Text("no") }
                }
            }
        }
}

@Composable
fun displayWarningFileDeletion(openDialog: MutableState<Boolean>, deleteFunction:()->Unit) {
    DialogWindow(
        onCloseRequest = {openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(350.dp, 200.dp)),
        title = "Delete File",
        resizable = false)
    {
            Column(modifier = Modifier.fillMaxSize())
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AppTheme.colors.iconRed,
                        modifier = Modifier.size(60.dp).padding(start = 20.dp)
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center, text = "Are you sure, that you want to delete this file?"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = {
                            openDialog.value = false
                            deleteFunction()
                        }) { Text("yes") }
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { openDialog.value = false }) { Text("no") }
                }
            }
        }
}

@Composable
fun displayConnectionDialog(openDialog: MutableState<Boolean>, ServerAddress: MutableState<String>)
{
    //                Icon(
//                    Icons.Default.Warning,
//                    contentDescription = "Warning",
//                    tint = AppTheme.colors.iconRed,
//                    modifier = Modifier.size(60.dp).padding(start = 20.dp)
//                )
    DialogWindow(
        onCloseRequest =
    //                var text by remember { mutableStateOf(TextFieldValue("")) }
    //                var text by remember { mutableStateOf(TextFieldValue("")) }
                    {openDialog.value=false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(350.dp, 200.dp)),
        title = "Connect to Backend",
        resizable = false)  {
            Column(modifier = Modifier.fillMaxSize())
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    //                Icon(
                    //                    Icons.Default.Warning,
                    //                    contentDescription = "Warning",
                    //                    tint = AppTheme.colors.iconRed,
                    //                    modifier = Modifier.size(60.dp).padding(start = 20.dp)
                    //                )
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center, text = "URL/IP-Address"
                    )
                    //                var text by remember { mutableStateOf(TextFieldValue("")) }
                    TextField(
                        value = ServerAddress.value,
                        placeholder = { Text(text = "localhost") },
                        leadingIcon = { Icon(Icons.Default.Create, null) },
                        onValueChange = { ServerAddress.value = it; },
                        singleLine = true,
                        textStyle = TextStyle(lineHeight = 28.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center, text = "Port"
                    )
                    //                var text by remember { mutableStateOf(TextFieldValue("")) }
                    TextField(
                        value = ServerAddress.value,
                        placeholder = { Text(text = "8080") },
                        leadingIcon = { Icon(Icons.Default.Create, null) },
                        onValueChange = { ServerAddress.value = it; },
                        singleLine = true,
                        textStyle = TextStyle(lineHeight = 28.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = {
                            openDialog.value = false
                        }) { Text("Connect to Server") }
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.backgroundMedium),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { openDialog.value = false }) { Text("Cancel") }
                }
            }
        }
}

