package com.github.tukcps.sysmd

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.exports.UcbDataPack
import com.github.tukcps.sysmd.generated.resources.Res
import com.github.tukcps.sysmd.generated.resources.logo
import com.github.tukcps.sysmd.model.datamodel.toElement
import com.github.tukcps.sysmd.rest.RESTRepository
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.MainView
import com.github.tukcps.sysmd.ui.MenuBar
import com.github.tukcps.sysmd.ui.composables.elementToSystemC
import com.github.tukcps.sysmd.ui.composables.menuState
import com.github.tukcps.sysmd.ui.dialogs.*
import com.github.tukcps.sysmd.ui.rendering.fileURIToOpen
import com.github.tukcps.sysmd.ui.rendering.showDirectoryWarning
import com.github.tukcps.sysmd.ui.rendering.showNoSuchFileWarning
import com.github.tukcps.sysmd.ui.rendering.showOpenFileExternalWarning
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.colorMode
import com.github.tukcps.sysmd.ui.viewmodel.importSettings
import com.github.tukcps.sysmd.ui.viewmodel.loadSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.springframework.boot.system.ApplicationHome
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectories


/**
 * Displays the SysMD logo.
 * Must be turned off by application once started by setting
 * splashScreen.isVisible to false.
 * Based on simple Java Swing.
 */
object SysMDNotebook {

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
    private suspend fun init() {
        AppTheme.version = try { Res.readBytes("files/version.txt").decodeToString() } catch (_: Exception) { "(?)" }
        importSettings()
        copyInstallToSysMdDataFolder()
    }

    private val _isLaunching = MutableStateFlow(true)

    suspend fun showUI(args: Array<String>) {
        logger.info("Starting SysMD ${AppTheme.version} with options ${args.toList()}")
        init()
        val sysMdViewModel = SysMDViewModel()
        loadSettings()  //actively initiate loading of the settings as otherwise the settings are only loaded after the settings window has been opened

        application {

            val colors = colorMode()
            AppTheme.colors.onWarning = if (colors == DarkColors) dark_onWarning else light_onWarning
            AppTheme.colors.warningContainer = if (colors == DarkColors) dark_WarningContainer else light_WarningContainer
            AppTheme.colors.warning = if (colors == DarkColors) dark_Warning else light_Warning
            AppTheme.colors.onInfo = if (colors == DarkColors) dark_onInfo else light_onInfo
            AppTheme.colors.info = if (colors == DarkColors) dark_Info else light_Info
            AppTheme.colors.infoContainer = if (colors == DarkColors) dark_InfoContainer else light_InfoContainer

            var showSplashWindow by remember { mutableStateOf(true) }
            var showMainWindow by remember { mutableStateOf(false) }

            val showSettingsDialog = sysMdViewModel.showSettingsDialog
            val openUCB = remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                _isLaunching.collectLatest { launching ->
                    if (!launching) {
                        showSplashWindow = false
                        showMainWindow = true
                    }
                }
            }

            // While starting, show splash window
            if (showSplashWindow)  {
                val splashWindowState = rememberWindowState(
                    position = WindowPosition(Alignment.Center),
                    size = DpSize(400.dp, 400.dp)
                )

                Window(
                    onCloseRequest = ::exitApplication, state = splashWindowState, undecorated = true, resizable = false, transparent = true
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = "SysMD Logo",
                            modifier = Modifier.size(400.dp)
                        )
                    }
                }
            } else {
                MaterialTheme(colorScheme = colors) {
                    /** Shows a dialog before deleting a project, and if deleted, resets the (then invalid) session. */
                    Window(
                        onCloseRequest = {
                            if (sysMdViewModel.projectListViewModel.selectedProjectState.value?.unsavedChangesExist() == true)
                                sysMdViewModel.showSaveBeforeExitDialog.value = true
                            else {
                                if (RESTRepository.onlineState.value)
                                    try {
                                        RESTRepository.deleteSession()
                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }
                                exitApplication()
                            }
                        },
                        title = "SysMD Notebook",
                        state = rememberWindowState(
                            width = 1280.dp,
                            height = 768.dp,
                            position = WindowPosition(alignment = Alignment.Center),
                        ),
                        icon = painterResource(Res.drawable.logo)
                    ) {
                        MenuBar(sysMdViewModel)
                        MainView(sysMdViewModel)

                        SaveDialog(sysMdViewModel.showSaveBeforeExitDialog,
                            itemName = "Project '${sysMdViewModel.projectListViewModel.selectedProjectState.value?.name}' and/or its files",
                            onSave = {
                                sysMdViewModel.projectListViewModel.selectedProjectState.value?.saveProjectToRepository()
                                exitApplication() },
                            onDrop = {  exitApplication() },
                        )

                        SettingsDialog(showSettingsDialog)
                        if (showNoSuchFileWarning.value)
                            displayWarningNoSuchFileOrDirectory(showNoSuchFileWarning)
                        if (showDirectoryWarning.value)
                            displayWarningDirectoryAccess(showDirectoryWarning, fileURIToOpen)
                        if (showOpenFileExternalWarning.value)
                            displayWarningOpenFileWithDefaultSystemProgram(showOpenFileExternalWarning, fileURIToOpen)
                        if (menuState.deleteFileClicked.value)
                            displayWarningFileDeletion(menuState.deleteFileClicked) {}//todo - check if needed
                        if (menuState.systemCExportClicked.value) {
                            var ucbData: UcbDataPack? = null
                            val systemcExporter = Exporter()
                            try {
                                val session = SessionManager.getSession(sysMdViewModel.sessionId) ?: throw IllegalStateException("No session")
                                systemcExporter.analyzeSysMD(elementToSystemC?.value!!.toElement(session))
                                ucbData = systemcExporter.getUCBData()
                                openUCB.value = true
                            } catch (e: Exception) {
                                menuState.systemCExportClicked.value = false
                                logger.error("Problem with analyzing the SysMD tree: ${e.localizedMessage}")
                            }
                            if (openUCB.value) {
                                UserControlBoard(ucbData!!, systemcExporter, menuState.systemCExportClicked, openUCB)
                            }
                        }
                    }
                }
            }
        }
    }

    fun launchingFinished() {
        _isLaunching.value = false
    }
}