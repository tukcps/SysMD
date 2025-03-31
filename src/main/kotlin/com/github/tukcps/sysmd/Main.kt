package com.github.tukcps.sysmd

import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.viewmodel.Settings
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

    SplashScreen.show()

    SpringApplicationBuilder(
        SysMdRunner::class.java,
    )   .headless(false)
        .run(*args)

}

/**
 * Displays the SysMD logo.
 * Must be turned off by application once started by setting
 * splashScreen.isVisible to false.
 * Based on simple Java Swing.
 */
object SplashScreen {

    // Prevent splash screen during unit tests or for server mode ...
    private fun isRunningTest(): Boolean {
        try {
            Class.forName("org.junit.Test")
        } catch (e: ClassNotFoundException) {
            return false
        }
        return true
    }

    private var splashScreen: JFrame? = null

    fun show() {
        if (!isRunningTest()) {
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
