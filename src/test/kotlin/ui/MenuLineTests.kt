package ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.performClick
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.v2.runComposeUiTest
import com.github.tukcps.sysmd.services.repositories.local.ProjectData.Companion.fromInterchangeFiles
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.CompileProgressIndicator
import com.github.tukcps.sysmd.ui.MenuLine
import com.github.tukcps.sysmd.ui.viewmodel.CompileProgress
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import kotlinx.io.files.Path
import util.mockup.MockupSysMDProjectService
import java.io.File
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@EnabledIf("isSkikoAvailable")
class MenuLineTests {

    companion object {
        @JvmStatic
        fun isSkikoAvailable(): Boolean {
            return try {
                org.jetbrains.skiko.Library.load()
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    @Test
    fun progressIndicatorShowsMessageAndFraction() = runComposeUiTest {
        setContent { MaterialTheme { CompileProgressIndicator(CompileProgress("Creating variables", 0.45f)) } }
        onNodeWithText("Creating variables").assertIsDisplayed()
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.45f, 0f..1f))).assertIsDisplayed()
        System.getenv("SCREENSHOT_DIR")?.let { dir ->
            javax.imageio.ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(dir, "progress-indicator.png"))
        }
    }

    @Test
    fun solveButtonSolvesInTheBackgroundAndShowsTheResults() {
        val directory = createTempDirectory("sysmd-menuline-test").toFile()
        try {
            File(directory, "Demo.md").writeText("```SysML\npackage Demo {\n    private import ScalarValues::*;\n    part def Vehicle { attribute mass: Real = 1500.0; attribute total: Real = mass + 500.0; }\n}\n```\n")
            File(directory, ".project.json").writeText("""{ "name" : "Demo", "version" : "*", "description" : "", "usage" : [ ], "id" : "5f0b8a7e-3c1d-4e2f-8a9b-0c1d2e3f4a5b" }""")
            File(directory, ".meta.json").writeText("""{ "index" : { "Demo.md" : "Demo.md" }, "created" : "2020-01-01T00:00:00Z" }""")
            val service = MockupSysMDProjectService()
            SessionManager.projectService = service
            service.setProjects(listOf(assertNotNull(fromInterchangeFiles(Path(directory.absolutePath)))))
            val sysMDViewModel = SysMDViewModel()
            sysMDViewModel.projectListViewModel.projectViewModels.value.single().createProjectSession()
            val sessionId = sysMDViewModel.sessionId

            runComposeUiTest {
                setContent { MaterialTheme { MenuLine(sysMDViewModel) } }
                onNodeWithText("Solve").performClick()
                waitUntil(timeoutMillis = 30_000) { sysMDViewModel.sessionId != sessionId && sysMDViewModel.compileProgress.value == null }
                System.getenv("SCREENSHOT_DIR")?.let { dir ->
                    javax.imageio.ImageIO.write(onRoot().captureToImage().toAwtImage(), "png", File(dir, "menu-line-idle.png"))
                }
            }

            assertNotEquals(sessionId, sysMDViewModel.sessionId)
            val results = sysMDViewModel.editorTabsViewModel.editorTabs.flatMap { tab -> tab.cells.flatMap { cell -> cell.displayItems.map { it.text } } }
            assertTrue(results.any { "total = 2000" in it }, "the solved value is not shown in $results")
            SessionManager.kill(sysMDViewModel.sessionId)
        } finally {
            directory.deleteRecursively()
        }
    }
}
