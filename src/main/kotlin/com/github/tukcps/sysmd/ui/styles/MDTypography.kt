package com.github.tukcps.sysmd.ui.styles

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The fonts and settings that are used for the Markdown Rendering are
 * persisted here.
 * The space above is reduced to bare minimum because we have the large clickable space there
 * anyhow.
 */
object MDTypography {

    /** H1 Heading */
    object H1 {
        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp
        )
        val spaceAbove = 1.dp
        val spaceBelow = 8.dp
    }

    object H2 {
        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp
        )
        val spaceAbove = 1.dp
        val spaceBelow = 8.dp
    }

    object H3 {
        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp
        )
        val spaceAbove = 1.dp
        val spaceBelow = 6.dp
    }

    object H4 {
        val style = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp
        )
        val spaceAbove = 1.dp
        val spaceBelow = 2.dp
    }

    /** Regular text */
    object BodyMedium {
        val style =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 21.sp,
            )
    }
}