package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.github.tukcps.sysmd.compiler.scanner.Token.Definitions.keywords
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.styles.Fonts


class SettingsViewModel {
    val useDefault = mutableStateOf(false)
    val agendaExpertMode = mutableStateOf(false)
    val dataFolder = mutableStateOf("")
    val systemCExportFolder = mutableStateOf("")
    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val entryURI = mutableStateOf("")
    val baseURI = mutableStateOf("")
    val port = mutableStateOf("")
    val initializationNumber = mutableStateOf("")
    val propagationNumber = mutableStateOf("")
    val images = mutableStateOf("")
    val heightWhenCollapsed = mutableStateOf("")
    val tabSize = mutableStateOf("")
    val keyWordColor = mutableStateOf("")
    val defaultColor = mutableStateOf("")
    val fontWeight = mutableStateOf("")
    val fontFamily = mutableStateOf("")
    val colorList = listOf("Blue", "Green", "Red", "Yellow", "Cyan", "Magenta")
    val fontFamilyList = listOf("jetbrainsMono")
    val colorMap: HashMap<Color, String> = hashMapOf(
        Color.Blue to "Blue",
        Color.Green to "Green",
        Color.Red to "Red",
        Color.Yellow to "Yellow",
        Color.Cyan to "Cyan",
        Color.Magenta to "Magenta"
    )
    val fontFamilyMap: HashMap<FontFamily, String> = hashMapOf(
        FontFamily.Default to "jetbrainsMono",
    )
}

val settingsViewModel = SettingsViewModel()

fun loadSettings() {
    val key1 = settings.defaultHighlightStyle.color
    val key2 = settings.defaultHighlightStyle.fontFamily
    val key3 = settings.keywordHighlighing["Connector"]?.color

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
    settingsViewModel.defaultColor.value = settingsViewModel.colorMap[key1] ?: ""
    settingsViewModel.fontFamily.value = settingsViewModel.fontFamilyMap[key2] ?: ""
    settingsViewModel.keyWordColor.value = settingsViewModel.colorMap[key3] ?: ""
    settingsViewModel.agendaExpertMode.value = settings.agendaExpertMode
}

fun storeSettings() {

    fun findColor(color: String): Color {
        for ((key, value) in settingsViewModel.colorMap) {
            if (value == color) {
                return key
            }
        }
        return Color.Blue
    }

    fun findFontFamily(font: String): FontFamily {
        for ((key, value) in settingsViewModel.fontFamilyMap) {
            if (value == font) {
                return key
            }
        }
        return FontFamily.Default
    }

    val color1 = findColor(settingsViewModel.defaultColor.value)
    val color2 = findColor(settingsViewModel.keyWordColor.value)
    val fontFam = findFontFamily(settingsViewModel.fontFamily.value)
    val tmp = if (settingsViewModel.fontWeight.value == "" || settingsViewModel.fontWeight.value == "0") 1 else settingsViewModel.fontWeight.value.toInt()
    val fontWei = FontWeight(tmp)

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
    settings.defaultHighlightStyle = SpanStyle(color = color1, fontWeight = fontWei, fontFamily = fontFam)
    settings.keywordHighlighing = keywords.mapValuesTo(hashMapOf(), transform = { SpanStyle(color2) })
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
    settings.defaultHighlightStyle =
        SpanStyle(color = Color.Blue, fontWeight = FontWeight.Normal, fontFamily = Fonts.jetbrainsMono)
    settings.keywordHighlighing = keywords.mapValuesTo(hashMapOf(), transform = { SpanStyle(Color.Blue) })
    settings.heightWhenCollapsed = 100
    settings.tabSize = 4
    exportSettings()
    loadSettings()
}

fun resetGeneral() {
    settings.dataFolder = System.getProperty("user.home") + "/SysMD"
    exportSettings()
    loadSettings()
}

fun resetAgenda() {
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
