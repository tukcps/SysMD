package com.github.tukcps.sysmd

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.exports.UcbDataPack
import com.github.tukcps.sysmd.generated.resources.Res
import com.github.tukcps.sysmd.generated.resources.SysMD_Icon
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.rest.RESTRepository
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.*
import com.github.tukcps.sysmd.ui.composables.elementToSystemC
import com.github.tukcps.sysmd.ui.composables.menuState
import com.github.tukcps.sysmd.ui.composables.selectedElement
import com.github.tukcps.sysmd.ui.diagram.showStateDiagramImagePopup
import com.github.tukcps.sysmd.ui.dialogs.SaveDialog
import com.github.tukcps.sysmd.ui.dialogs.SettingsDialog
import com.github.tukcps.sysmd.ui.dialogs.UserControlBoard
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.importSettings
import com.github.tukcps.sysmd.ui.viewmodel.loadSettings
import org.jetbrains.compose.resources.painterResource
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.system.ApplicationHome
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories


/**
 * In the main application with UI:
 * - Setting the session credentials (user, password, server) for REST
 * - Reading a SysMD file after selecting the file into model with fileReader
 * - Interactively browsing and editing the model.
 * - Writing the model to the server via REST API
 * Note, that when starting from IntelliJ, the project settings must set the JVM to Java 16.
 */
@SpringBootApplication
open class SysMdRunner: CommandLineRunner {

    /**
     * Searches for directory 'install' in which we have files to be copied to
     * installation home folder.
     */
    private fun getLocationOfInstall(): String {
        val workingDir = System.getProperty("user.dir")
        val appHome = ApplicationHome(SysMdRunner::class.java).dir.parent

        if (File("$workingDir/install").exists() && File("$workingDir/install").isDirectory)
            return "$workingDir/install"
        else if (File("$appHome/install").exists() && File("$appHome/install").isDirectory)
            return "$appHome/install"
        else {
            logger.error("Unable to access location for SysMD installation files (app home: $appHome, user dir: $workingDir)")
            return ""
        }
    }

    /**
     * This function copies parts of the resources to the SysMD directory to make them
     * available for the users.
     */
    private fun copyInstallToSysMdDataFolder() {
        try {
            val install = getLocationOfInstall()
            // Use the target path from settings, and copy the source into that folder
            val target = File(settings.dataFolder)
            Path(target.path).createDirectories()
            File(install).copyRecursively(target, overwrite = true)
            
        } catch (exception: Exception) {
            logger.error("Installing files failed: ${exception.message}")
        }
    }

    /**
     * Installation script ...
     * We copy some files from resources to the user-directory.
     * This is then used as the main repository where all files lie.
     */
    private fun init() {
        AppTheme.version = SessionManager::class.java.classLoader.getResource("version")?.readText() ?: "(unknown version)"
        importSettings()
        copyInstallToSysMdDataFolder()
    }


    override fun run(vararg args: String) {
        val session = SessionManager.startSession()
        RESTRepository.internalSessionId = session.id
        SplashScreen.hide()

        if ("headless" !in args)
            try {
                init()
                logger.info("Starting SysMD ${AppTheme.version} with options '$args'")
                val sysMdViewModel = SysMDViewModel(session)
                application {
                    val showDialog = remember { mutableStateOf(false) }
                    val showSettingsDialog = sysMdViewModel.showSettingsDialog
                    val openUCB = remember { mutableStateOf(false) }
                    val tabs = sysMdViewModel.tabsViewModel

                    loadSettings()  //actively initiate loading of the settings as otherwise the settings are only loaded after the settings window has been opened

                    val colors = when (settings.colorStyle) {
                        "dark" -> DarkColors
                        "light" -> LightColors
                        else -> if (isSystemInDarkTheme()) DarkColors else LightColors
                    }
                    AppTheme.colors.onWarning = if (colors == DarkColors) dark_onWarning else light_onWarning
                    AppTheme.colors.warningContainer =
                        if (colors == DarkColors) dark_WarningContainer else light_WarningContainer
                    AppTheme.colors.warning = if (colors == DarkColors) dark_Warning else light_Warning
                    AppTheme.colors.onInfo = if (colors == DarkColors) dark_onInfo else light_onInfo
                    AppTheme.colors.info = if (colors == DarkColors) dark_Info else light_Info
                    AppTheme.colors.infoContainer = if (colors == DarkColors) dark_InfoContainer else light_InfoContainer

                    MaterialTheme(colorScheme = colors) {
                        Window(
                            onCloseRequest = {
                                tabs.editorTabs.forEach { tab ->
                                    if (tab.elementEdited.value) showDialog.value = true
                                }

                                if (RESTRepository.onlineState.value)
                                    try {
                                        RESTRepository.deleteSession()
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }

                                if (!showDialog.value)
                                    exitApplication()
                            },
                            title = "SysMD Notebook",
                            state = rememberWindowState(
                                width = 1280.dp,
                                height = 768.dp,
                                position = WindowPosition(alignment = Alignment.Center),
                            ),
                            icon = painterResource(Res.drawable.SysMD_Icon)
                        ) {
                            MenuBar(sysMdViewModel)
                            MainView(sysMdViewModel)
                            if (showDialog.value)
                                SaveDialog(showSaveDialog = showDialog,
                                    onSave = { sysMdViewModel.tabsViewModel.save(); exitApplication() },
                                    onDrop = { exitApplication() }
                                )
                            if (showSettingsDialog.value)
                                SettingsDialog(showSettingsDialog)
                            if (showNoSuchFileWarning.value)
                                displayWarningNoSuchFileOrDirectory(showNoSuchFileWarning)
                            if (showDirectoryWarning.value)
                                displayWarningDirectoryAccess(showDirectoryWarning, fileURIToOpen)
                            if (showOpenFileExternalWarning.value)
                                displayWarningOpenFileWithDefaultSystemProgram(showOpenFileExternalWarning, fileURIToOpen)
                            if (menuState.deleteFileClicked.value)
                                displayWarningFileDeletion(menuState.deleteFileClicked) {}//todo - check if needed
                            if (menuState.systemCexportClicked.value) {
                                var ucbData: UcbDataPack? = null
                                val systemcExporter = Exporter()
                                try {
                                    systemcExporter.analyzeSysMD(elementToSystemC?.value!!)
                                    ucbData = systemcExporter.getUCBData()
                                    openUCB.value = true
                                } catch (e: Exception) {
                                    menuState.systemCexportClicked.value = false
                                    logger.error("Problem with analyzing the SysMD tree: ${e.localizedMessage}")
                                }
                                if (openUCB.value) {
                                    UserControlBoard(ucbData!!, systemcExporter, menuState.systemCexportClicked, openUCB)
                                }
                            }

                            if (menuState.renderClicked.value){
                                val selectedElementValue = selectedElement?.value
                                if(selectedElementValue is StateUsage){
                                    val diagramsPath =  sysMdViewModel.sessionState.value.project!!.directory!!.resolve("diagrams")
                                    showStateDiagramImagePopup(selectedElementValue, diagramsPath, menuState.renderClicked)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Issue in SysMD Notebook: $e")
            }
    }
}