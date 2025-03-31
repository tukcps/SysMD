package services

import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.getCells
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import io.github.tukcps.sysmlv2.interchange.Meta
import util.testSession
import java.nio.file.Path
import java.nio.file.Paths
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectDataTest {

    @Test
    fun saveAndLoadProjectData() {

        // Save
        val testResourcesDir: Path = Paths.get("src/test/resources/saveAndLoadProjectData/project")
        val project = ProjectData(
            project = InterchangeProject(
                name = "name",
                description = "description"),
            directory = testResourcesDir,
            meta = Meta(
                index = hashMapOf("a" to "file1.md", "b" to "file2.kerml"), created =
                    OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
        )
        project.saveToInterchangeFiles()

        // Load
        assertTrue(testResourcesDir.toFile().exists())
        val project2 = ProjectData.fromInterchangeFiles(testResourcesDir)
        assertNotNull(project2)
        assertEquals(project2.name, "name")
        assertEquals(project2.description, "description")
    }

    @Test
    fun getCellsTest() {
        val testResourcesDir: Path = Paths.get("src/test/resources/saveAndLoadProjectData/project")
        val file = testResourcesDir.resolve("file1.md").toFile()
        val cells = file.getCells()
        assertNotNull(cells)
        assertEquals(5, cells.size)
        assertEquals("YAML", cells[0].language)
        assertEquals("KerML", cells[1].language)
        assertEquals("package test; ", cells[1].body)
    }

    @Test
    fun getCellIndexTest() = testSession(testDirectory = "saveAndLoadProjectData/project") {
        val cells = project!!.getCellIndex()
        assertNotNull(cells)
        assertEquals(5, cells["file1.md"]?.size)
        assertEquals("YAML", cells["file1.md"]?.first()?.language)
    }
}