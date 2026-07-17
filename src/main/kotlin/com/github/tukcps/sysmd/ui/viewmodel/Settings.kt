package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.github.tukcps.sysmd.compiler.scanner.Token.Definitions.keywords
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.util.JsonSupport
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
import kotlinx.serialization.Serializable

// Defines Icon Style to use to be the same in the whole GUI
var MyIcons = Icons.Filled

/**
 * Settings of the REST connection.
 * In package rest.
 */
@Serializable
data class RestSettings(
    var filename: String =  "",
    var baseURI:  String  = "localhost",
    var entryURI: String  = "/agila-server",
    var port:     String  = "8080",
    var username: String  = "admin@cps.de",
    var password: String  = "admin"
)

/**
 * Entity for serialization of settings.
 */
@Serializable
data class Settings(
    var colorStyle:     String = "system",
    var dataFolder:     String = System.getProperty("user.home") + "/SysMD",
    var imagesToCache:  Int = 20,
    var heightWhenCollapsed: Int = 100,
    var tabSize:Int = 4,
    var initializationNumber: Int = 2,
    var propagationNumber: Int = 100,
    var agendaExpertMode: Boolean = false,
    val rest: RestSettings = RestSettings(),
    var useDefaultHighlight: Boolean = false
) {

    val defaultHighlightStyle by lazy {
        SpanStyle(
            color = Color.Blue,
            fontWeight = FontWeight.Normal,
            fontFamily = Fonts.jetbrainsMono
        )
    }

    val keywordHighlighting: Map<String, SpanStyle>
        get() = keywords.mapValues {
            SpanStyle(
                color = Color.Blue,
                fontFamily = Fonts.jetbrainsMono,
                fontSize = AppTheme.fontSize,
            )
        }
}

fun importSettings() {
    val sysMDFolder = System.getProperty("user.home") + "/SysMD"

    try {
        val source = SystemFileSystem.source(Path("$sysMDFolder/settings.json"))
        val text = source.buffered().use { it.readString() }
        settings = JsonSupport.decode(text)

    } catch (_: IOException) {

        logger.info(
            "Couldn't find settings '$sysMDFolder/settings.json'. Using defaults and creating a new settings file."
        )

        settings = Settings()
        exportSettings()
    }
}

fun exportSettings() {
    val sysMDFolder = System.getProperty("user.home") + "/SysMD"

    SystemFileSystem.createDirectories(Path(sysMDFolder))
    SystemFileSystem.sink(Path("$sysMDFolder/settings.json"))
        .buffered()
        .use { it.writeString(JsonSupport.encode(settings)) }
}

/**
 * Composable to query the color mode.
 */
@Composable
fun colorMode() = when (settings.colorStyle) {
    "dark"  -> DarkColors
    "light" -> LightColors
    else -> if (isSystemInDarkTheme()) DarkColors else LightColors
}


@Composable
fun colorMode() = when (settings.colorStyle) {
    "dark" -> DarkColors
    "light" -> LightColors
    else -> if (isSystemInDarkTheme()) DarkColors else LightColors
}
