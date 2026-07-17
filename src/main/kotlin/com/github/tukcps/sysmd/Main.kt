package com.github.tukcps.sysmd

import com.github.tukcps.sysmd.ui.viewmodel.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder


/**
 * Where SysMD persists all its Settings.
 */
var settings: Settings = Settings()
val logger: Logger = LoggerFactory.getLogger("SysMD Notebook")

suspend fun main(args: Array<String>) {

    val headless = "headless" in args
    // Launches Spring Boot Backend
    val springJob = CoroutineScope(Dispatchers.IO).launch {
        SpringApplicationBuilder(
            SysMdRunner::class.java,
        ).headless(headless)
            .run(*args)
    }

    // Launches SysMD Notebook
    if (!headless)
        SysMDNotebook.showUI(args)
    else
        springJob.join()
}

/**
 * After starting the backend, notify the UI to remove the splash screen
 * and show the Notebook UI.
 */
@SpringBootApplication
class SysMdRunner: CommandLineRunner {

    override fun run(vararg args: String) {
        SysMDNotebook.launchingFinished()
    }
}
