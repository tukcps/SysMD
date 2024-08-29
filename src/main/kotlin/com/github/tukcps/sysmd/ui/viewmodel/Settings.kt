package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tukcps.sysmd.compiler.scanner.Token.Definitions.keywords
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.Fonts
import java.io.File
import java.io.IOException
import java.io.InputStream

// Defines Icon Style to use to be the same in the whole GUI
var MyIcons = Icons.Filled


/**
 * Entity for serialization of settings.
 */
class Settings {
    //description is unused otherwise
    @Suppress("unused")
    var colorStyle: String = "system"
    private val description: String =
        //"TextSize: NORMAL SMALL LARGE " +
        "fontWeight: value in range(1,1000)---400 is Normal 700 is Bold " +
                "fontFamily: choose between Monospace, Cursive, SansSerif, Serif and Default " +
                "currentOs: for Win 11 you have to manually set this property!!" +
                "lineHeightMultiplier: depends on your screen resolution and maybe on OS -- 1.5f works for me on 1920x1080 and 3.0f on 2736x1824"

    //enum class TextSize { NORMAL, SMALL, LARGE }
    // Set user home as start - otherwise, users end up in mostly empty directory that cannot be left ...
    // User can then set it to less restricted directory.
    var dataFolder: String = System.getProperty("user.home") + "/SysMD"
    var projectFolder: String? = null
        get() = field ?:dataFolder

    var imagesToCache = 10

    //var textSize: TextSize = TextSize.NORMAL
    @JsonIgnore
    var defaultHighlightStyle: SpanStyle = SpanStyle(color = Color.Blue, fontWeight = FontWeight.Normal, fontFamily = Fonts.jetbrainsMono)
    var useDefaultHighlight = false

    @JsonIgnore
    var keywordHighlighing: HashMap<String, SpanStyle> = keywords.mapValuesTo(hashMapOf(), transform = {
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
        var confirm = false
        var filename = ""
        var baseURI  = "localhost"
        var entryURI = "/agila-server"
        var port     = "8080"
        var username = "admin@cps.de"
        var password = "admin"
    }
    val rest = RestSettings()

    var initializationNumber: Int = 2
    var propagationNumber: Int = 1
    var agendaExpertMode = false
}


val mapper = ObjectMapper()

fun importSettings() {
    val json: String?
    try {
        val inputStream: InputStream = File("sysmd_settings.json").inputStream()
        json = inputStream.bufferedReader().use { it.readText() }
        settings = mapper.readValue(json, Settings::class.java)
    } catch (e: IOException) {
        println("Could not open file sysmd_settings.json, using default-settings.")
    }
}


fun exportSettings() {
    val data = mapper.writeValueAsString(settings)
    try {
        File("sysmd_settings.json").writeText(data)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
