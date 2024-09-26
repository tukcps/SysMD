package com.github.tukcps.sysmd


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.exports.UcbDataPack
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.services.Indexer
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.*
import com.github.tukcps.sysmd.ui.composables.elementToSystemC
import com.github.tukcps.sysmd.ui.composables.menuState
import com.github.tukcps.sysmd.ui.composables.selectedElement
import com.github.tukcps.sysmd.ui.diagram.showStateDiagramImagePopup
import com.github.tukcps.sysmd.ui.styles.*
import com.github.tukcps.sysmd.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.jar.JarFile
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

var settings: Settings = Settings()
val indexer = Indexer() //Indexer which is required to provide code writing support

/**
 * This function copies parts of the resources to the SysMD directory to make them
 * available for the users.
 */
fun copyResources() {
    try {
        // First, we get the JAR file; the resources are part of it.
        val path = object {}.javaClass.getClassLoader().getResource("version")!!.path.replace("%20", " ")
        val jarPathSplit = path.split("!")
        assert(jarPathSplit.isNotEmpty())
        assert(jarPathSplit.size == 2)
        val jarPath = jarPathSplit.first()
        object {}.javaClass.getResource("")!!.path.replace("%20", " ").split("!").first()

        // Then, we open it as a jar file, get all entries and filter out those that are in folder home
        val jar = JarFile(jarPath.substringAfter("file:"))
        val entries = jar.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            if (entry.name.contains("home")) {
                val file = File("${settings.dataFolder}/${entry.name.removePrefix("home/")}")
                if (entry.isDirectory) {
                    Path(file.path).createDirectories()
                } else {
                    val bytes = jar.getInputStream(entry).readAllBytes()
                    file.writeBytes(bytes)
                }
            }
        }
    } catch (exception: Exception) {
        println("  Error while copying resources: ${exception.message}")
    }
}

/**
 * Installation script ...
 * We copy some files from resources to the user-directory.
 * This is then used as the main repository where all files lie.
 */
fun init() {
    AppTheme.version = SessionManager::class.java.classLoader.getResource("version")?.readText() ?: "(unknown version)"
    copyResources()
    importSettings() 
}


/**
 * In the main program, we start the UI that permits
 * - Setting the session credentials (user, password, server) by assigning agila a new RESTHelper
 * - Reading a SysMD file after selecting the file into model with fileReader
 * - Interactively browsing and editing the model.
 * - Writing the model to the server via REST API
 * Note, that when starting from IntelliJ, the project settings must set the JVM to Java 16.
 */
fun main() {
    init()
    println("Starting SysMD version ${AppTheme.version}")
    val session = SessionManager.startSession()
    AgilaRepository.internalSessionId = session.id
    val sysMdViewModel = SysMDViewModel(session = session)

    // Initialization only needs to happen once.
    // Not on every re-rendering of the application,
    // e.g., dragging the window to another screen
    try {
        application {
            val showDialog = remember { mutableStateOf(false) }
            val showSettingsDialog = sysMdViewModel.showSettingsDialog
            val openUCB = remember { mutableStateOf(false) }
            val tabs = sysMdViewModel.tabsModel

            /**The value for the progress bar*/
            val progressBarValue = remember { mutableStateOf(0F) }

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

            //Start Coroutine to initialize the Indexes
            rememberCoroutineScope().launch {
                indexer.initializeIndexes(progressBarValue)
            }

            MaterialTheme(colorScheme = colors) {
                Window(
                    onCloseRequest = {
                        tabs.editorTabs.forEach { tab ->
                            if (tab is EditorTabModel && tab.elementEdited.value)
                                showDialog.value = true
                        }

                        if (AgilaRepository.onlineState.value)
                            try {
                                AgilaRepository.deleteSession()
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
                    icon = BitmapPainter(useResource("SysMD-Icon.png", ::loadImageBitmap)),
                ) {

                    MainView(sysMdViewModel, window, progressBarValue)
                    MenuBar(sysMdViewModel)
                    if (showDialog.value)
                        SaveDialog(openDialog = showDialog, tabs = tabs)
                    if (showSettingsDialog.value)
                        SettingsDialog(showSettingsDialog)
                    if (showNoSuchFileWarning.value)
                        displayWarningNoSuchFileOrDirectory(showNoSuchFileWarning)
                    if (showDirectoryWarning.value)
                        displayWarningDirectoryAccess(showDirectoryWarning, fileURIToOpen)
                    if (showOpenFileExternalWarning.value)
                        displayWarningOpenFileWithDefaultSystemProgramm(showOpenFileExternalWarning, fileURIToOpen)
                    if (menuState.deleteFileClicked.value)
                        displayWarningFileDeletion(menuState.deleteFileClicked) {}//Nochmal prüfen
                    if (menuState.systemCexportClicked.value) {
                        var ucbData: UcbDataPack? = null
                        val systemcExporter = Exporter()
                        try {
                            systemcExporter.analyzeSysMD(elementToSystemC?.value!!)
                            ucbData = systemcExporter.getUCBData()
                            openUCB.value = true
                        } catch (e: Exception) {
                            menuState.systemCexportClicked.value = false
                            println("Problem with analyzing the SysMD tree: ${e.localizedMessage}")
                        }
                        if (openUCB.value) {
                            UserControlBoard(ucbData!!, systemcExporter, menuState.systemCexportClicked, openUCB)
                        }
                    }

                    if (menuState.renderClicked.value){
                        val selectedElementValue = selectedElement?.value
                        if(selectedElementValue is StateUsage){
                            val diagramsPath = Path(settings.projectFolder!!, "diagrams")
                            showStateDiagramImagePopup(selectedElementValue, diagramsPath, menuState.renderClicked)
                        }
                    }

                    if (menuState.deleteFileClicked.value)
                        displayWarningFileDeletion(menuState.deleteFileClicked, sysMdViewModel::deleteFile)
                    if (menuState.deleteProjectClicked.value)
                        displayWarningFileDeletion(menuState.deleteProjectClicked, sysMdViewModel::deleteProject)
                }
            }
        }
    } catch (e: Exception) {
        println("Problem in SysMD Notebook: $e")
    }
}
