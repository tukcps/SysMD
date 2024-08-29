@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.ButtonSelection
import com.github.tukcps.sysmd.ui.composables.InputField
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.SettingsTabItem
import com.github.tukcps.sysmd.ui.viewmodel.*


/**
 * The overall settings dialog window.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsDialog(openDialog: MutableState<Boolean>) {
    val tabs = listOf(
        SettingsTabItem.Rendering,
        SettingsTabItem.Login,
        SettingsTabItem.Solver,
        SettingsTabItem.Agenda
    )
    val selectedIndex: MutableState<Int> = remember { mutableStateOf(0) }
    importSettings()

    DialogWindow(
        onCloseRequest = { openDialog.value = false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(600.dp, 400.dp)),
        title = "Settings",
        resizable = false
    ) {
        Surface {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SettingsTabMenu(tabs, selectedIndex = selectedIndex)
                }

                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        loadSettings()
                        tabs[selectedIndex.value].screen()
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.height(50.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                modifier = Modifier.padding(end = 15.dp),
                                border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                                onClick = { openDialog.value = false }
                            ) { Text("Cancel") }
                            TextButton(
                                modifier = Modifier.padding(start = 15.dp, end = 20.dp),
                                border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                                onClick = {
                                    openDialog.value = false
                                    storeSettings()
                                    exportSettings()
                                }
                            ) { Text("Save") }
                            TextButton(
                                modifier = Modifier.padding(end = 15.dp),
                                border = BorderStroke(1.dp, AppTheme.colors.iconBlue),
                                onClick = { tabs[selectedIndex.value].reset() }
                            ) { Text("Default") }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LoginScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Local projects", fontWeight = FontWeight.Bold)
        Row {
            Text("Directory: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = settingsViewModel.dataFolder.value,
                onValueChange = { settingsViewModel.dataFolder.value = it },
                singleLine = true,
                check = { true }
            )
        }

        Row {
            Text("SystemC Exports: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = settingsViewModel.systemCExportFolder.value,
                onValueChange = { settingsViewModel.systemCExportFolder.value = it },
                singleLine = true,
                check = { true }
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Text("Web projects", fontWeight = FontWeight.Bold)
        Row {
            Text("URL of backend: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = settingsViewModel.baseURI.value,
                onValueChange = { settingsViewModel.baseURI.value = it },
                singleLine = true,
                check = { true }
            )
        }
        Row {
            Text("Port: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = settingsViewModel.port.value,
                onValueChange = { settingsViewModel.port.value = it },
                singleLine = true,
                check = { true }
            )
        }

        Row {
            Text("User name: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = settingsViewModel.username.value,
                onValueChange = { settingsViewModel.username.value = it },
                singleLine = true,
                check = { true }
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        InputSettingsItem(label = "Password", settingsViewModel.password, width = 180.dp, isPassword = true)
    }
}

@Composable
fun SolverScreen() {
    val d = settingsViewModel

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputSettingsItem(
            label = "Initialization Nr.",
            text = d.initializationNumber,
            width = 70.dp,
            isNumber = true,
            maxLength = 3
        )
        Spacer(modifier = Modifier.height(50.dp))
        InputSettingsItem(
            label = "Propagation Nr.",
            text = d.propagationNumber,
            width = 70.dp,
            isNumber = true,
            maxLength = 3
        )
    }
}

@Composable
fun RenderingScreen() {
    val d = settingsViewModel

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        val colorMode = remember {
            mutableStateOf( when(settings.colorStyle) {
                "dark" -> "Dark mode"
                "light" -> "Light mode"
                else -> "System mode"
            })
        }

        Text("Color mode", fontWeight = FontWeight.Bold)
        Row {
            Column(modifier = Modifier.width(200.dp)) {
                ButtonSelection(colorMode, "Dark mode") { settings.colorStyle = "dark" }
                ButtonSelection(colorMode, "Light mode") { settings.colorStyle = "light" }
                ButtonSelection(colorMode, "System mode") { settings.colorStyle = "system" }
            }
            Column(Modifier.padding(all = 10.dp)) {
                Text(text = """The color mode defines the choice of colors in the application. The new color mode will be active after restart. """.trimIndent(),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Cache size for images", fontWeight = FontWeight.Bold)
        Row {
            Text("Cache size for images: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = d.images.value,
                onValueChange = { d.images.value = it },
                singleLine = true,
                check = { it.firstOrNull() in '0' .. '9' }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text("Tab size in editor", fontWeight = FontWeight.Bold)
        Row {
            Text("Tab size in editor: ", style = MaterialTheme.typography.bodyMedium)
            InputField(
                modifier = Modifier.fitMaxWidth().padding(end = 12.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                value = d.tabSize.value,
                onValueChange = { d.tabSize.value = it },
                singleLine = true,
                check = { it.firstOrNull() in '0' .. '9' }
            )
        }
    }
}


@Composable
fun AgendaScreen() {
    val d = settingsViewModel

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CheckSettingsItem(label = "Expert mode", checked = d.agendaExpertMode)
    }
}

@Composable
fun CheckSettingsItem(
    label: String,
    checked: MutableState<Boolean> = mutableStateOf(false),
) {
    Row(
        modifier = Modifier
            .width(500.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = label,
            fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
            modifier = Modifier
                .padding(horizontal = 10.dp)
        )
        Checkbox(
            checked = checked.value,
            onCheckedChange = { checked.value = it }
        )
    }
}

@Composable
fun InputSettingsItem(
    label: String,
    text: MutableState<String> = mutableStateOf(""),
    width: Dp = 100.dp,
    isNumber: Boolean = false,
    isPassword: Boolean = false,
    maxLength: Int = 3,
) {
    Row(
        modifier = Modifier.width(500.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(width)
                .padding(top = 10.dp)
        ) {
            var passwordVisibility: Boolean by remember { mutableStateOf(false) }

            if (isPassword) {
                OutlinedTextField(
                    value = text.value,
                    onValueChange = { value ->
                        if (isNumber) {
                            if (value.length <= maxLength) {
                                text.value = value.filter { it.isDigit() }
                            }
                        } else {
                            text.value = value
                        }
                    },
                    modifier = Modifier.requiredHeight(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        // backgroundColor = Color.Transparent
                    ),
                    singleLine = true,
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisibility)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        IconButton(onClick = {
                            passwordVisibility = !passwordVisibility
                        }) {
                            Icon(imageVector = image, "password visibility")
                        }
                    }
                )
            } else {
                OutlinedTextField(
                    shape = RoundedCornerShape(20),
                    label = { Text("text") },
                    placeholder = { Text("text") },
                    value = text.value,
                    onValueChange = { value ->
                        if (isNumber) {
                            if (value.length <= maxLength) {
                                text.value = value.filter { it.isDigit() }
                            }
                        } else {
                            text.value = value
                        }
                    },
                    modifier = Modifier.requiredHeight(64.dp),
                    // colors = TextFieldDefaults.colors(
                    //    focusedIndicatorColor = Color.Transparent,
                    //    unfocusedIndicatorColor = Color.Transparent,
                        // backgroundColor = Color.Transparent
                    //),
                    singleLine = true
                )
            }
        }
    }
}

