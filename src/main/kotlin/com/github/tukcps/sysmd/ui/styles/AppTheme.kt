package com.github.tukcps.sysmd.ui.styles

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Here, we collect settings for fonts, colours and layout.
 */
object AppTheme {
    var version: String = "?"
    val fonts: Fonts = Fonts()
    val colors: Colors = Colors()
    val code: Code = Code()

    // The default fontsize for the editor etc. in sp (scaled pixels)
    class Fonts(
        var medium: TextUnit = 14.sp,
    )
    var fontSize = 14.sp
    // For Linux another fixedLineHeight is needed to adapt the line numbers to the SysMD Code lines
    val fixedLineHeight = 19.sp

    /**
     * Colors for theme.
     */
    class Colors(
        val backgroundDark: Color =  Color(0xFFB0BEC5),  // Bluegray 200, darker gray
        val backgroundMedium: Color = Color(0xFFCFD8DC), // Bluegray 100, lighter gray
        val backgroundLight: Color = Color(0xFFECEFF1),  // Bluegray 50, even lighter gray
        val backgroundLightGray: Color = Color(0xFFFAFAFA), // Gray 50, very light
        val iconGreen: Color = Color(0xFF388E3C),
        val iconGray: Color = Color(0xFF546E7A), // Medium blue-gray
        val iconRed: Color = Color(0xFFC62828),
        val iconYellow: Color = Color(0xFFFDD835 ),
        val iconBlue: Color = Color(0xFF1565C0),
        val backgroundWarning: Color = Color(0xFFFFF9C4), // Yellow
        var onWarning: Color = light_onWarning,
        var warningContainer: Color = light_WarningContainer,
        var warning: Color = light_Warning,
        var onInfo: Color = light_onInfo,
        var info: Color = light_Info,
        var infoContainer: Color = light_InfoContainer
    )

    /**
     * Colors for syntax highlighting.
     */
    class Code(
        val simple: SpanStyle = SpanStyle(Color(0xFFA9B7C6)),
        val value: SpanStyle = SpanStyle(Color(0xFF6897BB)),
        val keyword: SpanStyle = SpanStyle(Color(0xFFCC7832)),
        val punctuation: SpanStyle = SpanStyle(Color(0xFFA1C17E)),
        val annotation: SpanStyle = SpanStyle(Color(0xFFBBB529)),
        val comment: SpanStyle = SpanStyle(Color(0xFF808080))
    )
}




fun Font(@Suppress("UNUSED_PARAMETER") name: String, res: String, weight: FontWeight, style: FontStyle): Font =
    androidx.compose.ui.text.platform.Font("font/$res.ttf", weight, style)

object Fonts {
    val jetbrainsMono = FontFamily(
        Font(
            "JetBrains Mono",
            "jetbrainsmono_regular",
            FontWeight.Normal,
            FontStyle.Normal
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_italic",
            FontWeight.Normal,
            FontStyle.Italic
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_bold",
            FontWeight.Bold,
            FontStyle.Normal
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_bold_italic",
            FontWeight.Bold,
            FontStyle.Italic
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_extrabold",
            FontWeight.ExtraBold,
            FontStyle.Normal
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_extrabold_italic",
            FontWeight.ExtraBold,
            FontStyle.Italic
        ),

        Font(
            "JetBrains Mono",
            "jetbrainsmono_medium",
            FontWeight.Medium,
            FontStyle.Normal
        ),
        Font(
            "JetBrains Mono",
            "jetbrainsmono_medium_italic",
            FontWeight.Medium,
            FontStyle.Italic
        )
    )
}
