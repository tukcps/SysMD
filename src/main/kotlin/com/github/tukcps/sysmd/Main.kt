package com.github.tukcps.sysmd

import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.viewmodel.Settings
import org.jetbrains.skia.Image
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.builder.SpringApplicationBuilder
import java.awt.Color
import java.awt.Window
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.SwingConstants

var settings: Settings = Settings()
val logger: Logger = LoggerFactory.getLogger("SysMD Notebook")

fun main(args: Array<String>) {

    SplashScreen.show("headless" in args)

    SpringApplicationBuilder(
        SysMdRunner::class.java,
    )   .headless("headless" in args)
        .run(*args)
}

/**
 * Displays the SysMD logo.
 * Must be turned off by application once started by setting
 * splashScreen.isVisible to false.
 * Based on simple Java Swing.
 */
object SplashScreen {

    private var splashScreen: JFrame? = null

    fun show(headless: Boolean) {
        if (!headless) {
            splashScreen = JFrame("SysMD Notebook Splash Screen")
            splashScreen?.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
            splashScreen?.setSize(1000, 800)
            splashScreen?.setType(Window.Type.UTILITY) // remove from taskbar
            splashScreen?.isUndecorated = true         // remove menu bar
            splashScreen?.background = Color(0, 0, 0, 0)
            val pic = ImageIcon(SessionManager::class.java.classLoader.getResource("SplashScreen.png"))
            val lab = JLabel(pic, SwingConstants.CENTER)
            splashScreen?.add(lab, SwingConstants.CENTER)
            splashScreen?.pack()
            splashScreen?.setLocationRelativeTo(null)
            splashScreen?.isVisible = true
        }
    }

    fun hide() {
        splashScreen?.isVisible = false
    }
}

/**
 * Helper function to access drawable ressource from Spring Boot in a
 * robust wway
 */
fun loadPainter(path: String): Painter {
    val bytes = object {}.javaClass
        .getResourceAsStream(path)
        ?.readBytes()
        ?: error("Resource not found: $path")

    return BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
}
