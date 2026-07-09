package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tukcps.sysmd.compiler.scanner.Token.Definitions.keywords
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.DarkColors
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.styles.LightColors
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString


// Defines Icon Style to use to be the same in the whole GUI
var MyIcons = Icons.Filled


/**
 * Entity for serialization of settings.
 */
class Settings {
    //description is unused otherwise
    @Suppress("unused")
    var colorStyle: String = "system"
    var dataFolder: String = System.getProperty("user.home") + "/SysMD"
    var imagesToCache = 20

    //var textSize: TextSize = TextSize.NORMAL
    @JsonIgnore
    var defaultHighlightStyle: SpanStyle = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Normal, fontFamily = Fonts.jetbrainsMono)
    var useDefaultHighlight = false

    @JsonIgnore
    var keywordHighlighting: HashMap<String, SpanStyle> = keywords.mapValuesTo(hashMapOf(), transform = {
            SpanStyle(
                color = Color.Blue,
                fontFamily = Fonts.jetbrainsMono,
                fontSize = AppTheme.fontSize,
            )
        })
    var heightWhenCollapsed: Int = 100
    var tabSize:Int = 4

    /**
     * Settings of the REST connection.
     * In package rest.
     */
     class RestSettings {
        var filename = ""
        var baseURI  = "localhost"
        var entryURI = "/agila-server"
        var port     = "8080"
        var username = "admin@cps.de"
        var password = "admin"
    }
    val rest = RestSettings()

    var initializationNumber: Int = 2
    var propagationNumber: Int = 100
    var agendaExpertMode = false
}

val mapper = ObjectMapper()

fun importSettings() {
    val sysMDFolder = System.getProperty("user.home") + "/SysMD"
    val json: String?
    try {
        val input = SystemFileSystem.source(Path("${sysMDFolder}/settings.json"))
        json = input.buffered().use { it.readString() }
        settings = mapper.readValue(json, Settings::class.java)
    } catch (_: IOException) {
        logger.info("Couldn't find settings '${sysMDFolder}/settings.json', using defaults and create new settings file.")
        exportSettings()
    }
}


fun exportSettings() {
    val sysMDFolder = System.getProperty("user.home") + "/SysMD"
    val data = mapper.writeValueAsString(settings)
    try {
        val path = Path("${sysMDFolder}/settings.json")
        if (! SystemFileSystem.exists(path)) {
            logger.info("No settings.json file in $sysMDFolder; creating a new")
            SystemFileSystem.sink(path).use {  }
        }
        SystemFileSystem.sink(path).buffered().use { it.writeString(data) }
    } catch (_: Exception) {
        logger.error("Error exporting settings")
    }
}


@Composable
fun colorMode() = when (settings.colorStyle) {
    "dark" -> DarkColors
    "light" -> LightColors
    else -> if (isSystemInDarkTheme()) DarkColors else LightColors
}
