package services

import com.github.tukcps.sysmd.rest.entities.interchange.InterchangeProject
import com.github.tukcps.sysmd.rest.entities.interchange.Meta
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.getCells
import kotlinx.datetime.Instant
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import util.testProjectSession
import kotlin.io.path.toPath
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectDataTest {

    @Test
    fun saveAndLoadProjectData() {

        // Save
        val testResourcesDir = Path("src/test/resources/saveAndLoadProjectData/project")
        val project = ProjectData(
            project = InterchangeProject(name = "name", description = "description"),
            directory = testResourcesDir
        )
        project.meta = Meta(
            index = linkedMapOf("a" to "file1.md", "b" to "file2.kerml"),
            created = Instant.parse("2026-06-08T15:30:00Z")
        )
        project.saveToInterchangeFiles()

        // Load
        assertTrue( SystemFileSystem.exists(testResourcesDir))
        val project2 = ProjectData.fromInterchangeFiles(testResourcesDir)
        assertNotNull(project2)
        assertEquals(project2.name, "name")
        assertEquals(project2.description, "description")
    }

    @Test
    fun getCellsTest() {
        val testResourcesDir = Path("src/test/resources/saveAndLoadProjectData/project")
        val file = Path(testResourcesDir, "file1.md")
        val cells = file.getCells()
        assertNotNull(cells)
        assertEquals(5, cells.size)
        assertEquals("YAML", cells[0].language)
        assertEquals("KerML", cells[1].language)
        assertEquals("package test; ", cells[1].body)
    }

    @Test
    fun getCellIndexTest() {
        val dir = (javaClass.getResource( "saveAndLoadProjectData/project")?.toURI()?.toPath())
            ?: javaClass.classLoader.getResource( "saveAndLoadProjectData/project")?.toURI()?.toPath()
            ?: throw Exception("Could not load project from saveAndLoadProjectData")

        dir.resolve(".project.json").writeText("""
            {
                "name" : "name",
                "version" : "*",
                "description" : "description",
                "license" : null,
                "maintainer" : null,
                "website" : null,
                "topic" : null,
                "usage" : [ ],
                "id" : "7733965d-d7c8-44ae-ae27-6df87aa3e875"
            }
            """.trimIndent()
        )

        dir.resolve(".meta.json").writeText("""
            {
              "index" : {
                "a" : "file1.md",
                "b" : "file2.kerml"
              },
              "created" : "2020-01-01T00:00:00Z",
              "metamodel" : null,
              "includesDerived" : null,
              "includesImplied" : null,
              "checkSum" : null
            }
            """.trimIndent()
        )

        testProjectSession(testDirectory = "saveAndLoadProjectData/project") {
            val cells = project.getCells()
            assertNotNull(cells)
            assertEquals(5, cells["file1.md"]?.size)
            assertEquals("YAML", cells["file1.md"]?.first()?.language)
        }
    }

    @Test
    fun loadLegacyObjectUuidTest() {
        val dir = (javaClass.getResource("saveAndLoadProjectData/project")?.toURI()?.toPath())
            ?: javaClass.classLoader.getResource("saveAndLoadProjectData/project")?.toURI()?.toPath()
            ?: throw Exception("Could not load project from saveAndLoadProjectData")

        dir.resolve(".project.json").writeText("""
            {
                "name" : "name",
                "version" : "*",
                "description" : "description",
                "license" : null,
                "maintainer" : null,
                "website" : null,
                "topic" : null,
                "usage" : [ ],
                "id" : {
                    "mostSignificantBits" : 1180091352349625126,
                    "leastSignificantBits" : -6991388618554527646
                }
            }
            """.trimIndent()
        )

        val projectData = ProjectData.fromInterchangeFiles(Path(dir.toString()))
        assertNotNull(projectData)
        assertEquals("name", projectData.name)
        assertEquals("106086d5-234f-4b26-9ef9-9917e8593062", projectData.project.id.toString())
    }
}