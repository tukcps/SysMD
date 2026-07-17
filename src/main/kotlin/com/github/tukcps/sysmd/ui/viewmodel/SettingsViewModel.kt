package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import com.github.tukcps.sysmd.settings

class SettingsViewModel {
    val useDefault = mutableStateOf(false)
    val agendaExpertMode = mutableStateOf(false)
    val dataFolder = mutableStateOf("")
    val dataFolderOk = mutableStateOf(true)
    val systemCExportFolder = mutableStateOf("")
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val entryURI = mutableStateOf("")
    val baseURI = mutableStateOf("")
    val port = mutableStateOf("")
    val initializationNumber = mutableStateOf("")
    val propagationNumber = mutableStateOf("")
    val images = mutableStateOf("")
    val imagesOk = mutableStateOf(true)
    val heightWhenCollapsed = mutableStateOf("")
    val tabSize = mutableStateOf("")
    val tabSizeOk = mutableStateOf(true)
    val fontWeight = mutableStateOf("")
    val fontFamily = mutableStateOf("")

    val fontFamilyMap: HashMap<FontFamily, String> = hashMapOf(FontFamily.Default to "jetbrainsMono")

    fun setTabSize(value: String) {
        val trimmed = value.trim()
        tabSizeOk.value = trimmed.all { it in '0'..'9' }
        tabSize.value = value
    }

    fun setImages(value: String) {
        val trimmed = value.trim()
        imagesOk.value = trimmed.all { it in '0'..'9' }
        images.value = trimmed
    }

    fun setDataFolder(value: String) {
        val trimmed = value.trim()
        dataFolderOk.value = trimmed.matches(Regex("^[^<>:\"/\\\\|?*]+$")) && trimmed != "." && trimmed != ".."
        dataFolder.value = trimmed
    }

    fun allOk(): Boolean = dataFolderOk.value && imagesOk.value && tabSizeOk.value
}

val settingsViewModel = SettingsViewModel()

fun loadSettings() {
    val key2 = settings.defaultHighlightStyle.fontFamily

    settingsViewModel.baseURI.value = settings.rest.baseURI
    settingsViewModel.entryURI.value = settings.rest.entryURI
    settingsViewModel.port.value = settings.rest.port
    settingsViewModel.username.value = settings.rest.username
    settingsViewModel.password.value = settings.rest.password

    settingsViewModel.initializationNumber.value = settings.initializationNumber.toString()
    settingsViewModel.propagationNumber.value = settings.propagationNumber.toString()

    settingsViewModel.dataFolder.value = settings.dataFolder
    settingsViewModel.systemCExportFolder.value = settings.dataFolder + "/SystemC_Exports"
    settingsViewModel.useDefault.value = settings.useDefaultHighlight
    settingsViewModel.images.value = settings.imagesToCache.toString()
    settingsViewModel.heightWhenCollapsed.value = settings.heightWhenCollapsed.toString()
    settingsViewModel.tabSize.value = settings.tabSize.toString()
    settingsViewModel.fontWeight.value = settings.defaultHighlightStyle.fontWeight.hashCode().toString()
    settingsViewModel.fontFamily.value = settingsViewModel.fontFamilyMap[key2] ?: ""
    settingsViewModel.agendaExpertMode.value = settings.agendaExpertMode
}

fun storeSettings() {
    settings.rest.entryURI = settingsViewModel.entryURI.value
    settings.rest.baseURI = settingsViewModel.baseURI.value
    settings.rest.port = settingsViewModel.port.value

    settings.rest.username = settingsViewModel.username.value
    settings.rest.password = settingsViewModel.password.value
    settings.dataFolder = settingsViewModel.dataFolder.value
    settings.initializationNumber =
        if (settingsViewModel.initializationNumber.value == "") 0 else settingsViewModel.initializationNumber.value.toInt()
    settings.propagationNumber = if (settingsViewModel.propagationNumber.value == "") 0 else settingsViewModel.propagationNumber.value.toInt()
    settings.imagesToCache = if (settingsViewModel.images.value == "") 0 else settingsViewModel.images.value.toInt()
    settings.heightWhenCollapsed =
        if (settingsViewModel.heightWhenCollapsed.value == "") 1 else settingsViewModel.heightWhenCollapsed.value.toInt()
    settings.tabSize = if (settingsViewModel.tabSize.value == "") 1 else settingsViewModel.tabSize.value.toInt()
    settings.useDefaultHighlight = settingsViewModel.useDefault.value
    settings.agendaExpertMode = settingsViewModel.agendaExpertMode.value
}

fun resetLogin() {
    settings.rest.baseURI = ""
    settings.rest.entryURI = ""
    settings.rest.username = ""
    settings.rest.password = ""
    exportSettings()
    loadSettings()
}

fun resetRendering() {
    settings.imagesToCache = 10
    settings.heightWhenCollapsed = 100
    settings.tabSize = 4
    exportSettings()
    loadSettings()
}

fun resetBoard() {
    settings.agendaExpertMode = false
    exportSettings()
    loadSettings()
}

fun resetSolver() {
    settings.initializationNumber = 4
    settings.propagationNumber = 4
    exportSettings()
    loadSettings()
}
