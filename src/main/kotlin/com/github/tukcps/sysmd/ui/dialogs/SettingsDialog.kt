@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.ButtonSelection
import com.github.tukcps.sysmd.ui.helper.fitMaxWidth
import com.github.tukcps.sysmd.ui.styles.AppTheme
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
                            .padding(10.dp)
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
                                    if (settingsViewModel.allOk()) {
                                        openDialog.value = false
                                        storeSettings()
                                        exportSettings()
                                    }
                                }
                            ) { Text("  Save  ") }
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
        Text("Local projects", fontWeight = FontWeight.SemiBold)

        TextField(
            label =  { Text("SysMD directory")},
            placeholder = { Text("Path to directory with projects") },
            modifier = Modifier.padding(horizontal = 8.dp).scale(0.9F).fitMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.dataFolder.value,
            isError = !settingsViewModel.dataFolderOk.value,
            onValueChange = { settingsViewModel.setDataFolder(it) },
            singleLine = true
        )

        TextField(
            label = { Text("Model exports folder") },
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.systemCExportFolder.value,
            onValueChange = { settingsViewModel.systemCExportFolder.value = it },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Web projects", fontWeight = FontWeight.SemiBold)
        TextField(
            label = { Text("URL of backend: ") },
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.baseURI.value,
            onValueChange = { settingsViewModel.baseURI.value = it },
            singleLine = true,
        )

        TextField(
            label = { Text("URL") },
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.port.value,
            onValueChange = { settingsViewModel.port.value = it },
            singleLine = true,
        )

        TextField(
            label = { Text("User name") },
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.username.value,
            onValueChange = { settingsViewModel.username.value = it },
            singleLine = true,
        )

        InputSettingsItem(
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            label = "Password",
            settingsViewModel.password,
            isPassword = true
        )
    }
}

/**
 * Shown if tab solver is selected. 
 */
@Composable
fun SolverScreen() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        InputSettingsItem(
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            label = "Max. number of iterations in initialization",
            text = settingsViewModel.initializationNumber,
            isNumber = true,
            maxLength = 1
        )
        Spacer(modifier = Modifier.height(50.dp))
        InputSettingsItem(
            modifier = Modifier.fitMaxWidth().padding(horizontal = 8.dp).scale(0.9F),
            label = "Max. number of iterations in propagation",
            text = settingsViewModel.propagationNumber,
            isNumber = true,
            maxLength = 1
        )
    }
}

@Composable
fun RenderingScreen() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        val colorMode = remember {
            mutableStateOf( when(settings.colorStyle) {
                "dark" -> "Dark mode"
                "light" -> "Light mode"
                else -> "System mode"
            })
        }

        Text("Color mode", fontWeight = FontWeight.SemiBold)
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
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            label = { Text("Image cache size", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.fitMaxWidth().scale(0.9F),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.images.value,
            isError = !settingsViewModel.imagesOk.value,
            onValueChange = { settingsViewModel.setImages(it) },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            label = { Text("Tab size of editor: ", style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.scale(0.9F).fitMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
            value = settingsViewModel.tabSize.value,
            isError = !settingsViewModel.tabSizeOk.value,
            onValueChange = { settingsViewModel.setTabSize(it) },
            singleLine = true,
        )
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
        CheckSettingsItem(label = "Expert mode (t.b.d.)", checked = d.agendaExpertMode)
    }
}

@Composable
fun CheckSettingsItem(
    label: String,
    checked: MutableState<Boolean> = mutableStateOf(false),
) {
    Row(
        modifier = Modifier.width(500.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Text(
            text = label,
            fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Checkbox(
            modifier = Modifier.height(20.dp),
            checked = checked.value,
            onCheckedChange = { checked.value = it }
        )
    }
}

@Composable
fun InputSettingsItem(
    modifier: Modifier = Modifier,
    label: String,
    text: MutableState<String> = mutableStateOf(""),
    isNumber: Boolean = false,
    isPassword: Boolean = false,
    maxLength: Int = 3,
) {
    var passwordVisibility: Boolean by remember { mutableStateOf(false) }

    if (isPassword) {
        TextField(
            label = { Text(label)},
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
            modifier = modifier.requiredHeight(56.dp),
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
        TextField(
            label = { Text(label) },
            placeholder = { Text(text.value) },
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
            modifier = modifier.requiredHeight(64.dp),
            singleLine = true
        )
    }
}