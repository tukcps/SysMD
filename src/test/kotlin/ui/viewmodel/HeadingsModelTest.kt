package ui.viewmodel

import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import org.commonmark.parser.Parser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HeadingsModelTest {
    lateinit var parser : Parser
    lateinit var headingsModel : InternalRefReference

    @BeforeEach
    fun setUp() {
        parser = Parser.builder().build()
        headingsModel = InternalRefReference(null) {  }
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun basicTableOfContentTest(){

        val documentString = "# Test 1\r\n## Test 2\r\n### Test 3\r\n#### Test 4\r\n##### Test 5"
        val document = parser.parse(documentString)
            headingsModel.generateRefReferenceOfElements(null,document)
        headingsModel.generateHeadingNumbering()

        assertEquals(5,headingsModel.Headings.size)
        assertEquals(5,headingsModel.HeadingsWithNumbering.size)

        assertEquals("Test 1",headingsModel.Headings[0].second)
        assertEquals(1,headingsModel.Headings[0].first)

        assertEquals("Test 2",headingsModel.Headings[1].second)
        assertEquals(2,headingsModel.Headings[1].first)

        assertEquals("Test 3",headingsModel.Headings[2].second)
        assertEquals(3,headingsModel.Headings[2].first)

        assertEquals("Test 4",headingsModel.Headings[3].second)
        assertEquals(4,headingsModel.Headings[3].first)

        assertEquals("Test 5",headingsModel.Headings[4].second)
        assertEquals(5,headingsModel.Headings[4].first)
    }

    @Test
    fun checkNumberingOfTheIndividualElement(){
        val documentString = "# Test 1\r\n# Test 2\r\n## Test 2.1\r\n## Test 2.2\r\n# Test 3\r\n### Test 3.0.1"
        val document = parser.parse(documentString)
        headingsModel.generateRefReferenceOfElements(null, document)
        headingsModel.generateHeadingNumbering()

        assertEquals(6,headingsModel.Headings.size)
        assertEquals(6,headingsModel.HeadingsWithNumbering.size)

        assertEquals("Test 1",headingsModel.Headings[0].second)
        assertEquals(1,headingsModel.Headings[0].first)
        assertEquals(headingsModel.Headings[0].second,headingsModel.HeadingsWithNumbering[0].second)
        assertEquals("1 ",headingsModel.HeadingsWithNumbering[0].first)

        assertEquals("Test 2",headingsModel.Headings[1].second)
        assertEquals(1,headingsModel.Headings[1].first)
        assertEquals(headingsModel.Headings[1].second,headingsModel.HeadingsWithNumbering[1].second)
        assertEquals("2 ",headingsModel.HeadingsWithNumbering[1].first)

        assertEquals("Test 2.1",headingsModel.Headings[2].second)
        assertEquals(2,headingsModel.Headings[2].first)
        assertEquals(headingsModel.Headings[2].second,headingsModel.HeadingsWithNumbering[2].second)
        assertEquals("2.1 ",headingsModel.HeadingsWithNumbering[2].first)

        assertEquals("Test 2.2",headingsModel.Headings[3].second)
        assertEquals(2,headingsModel.Headings[3].first)
        assertEquals(headingsModel.Headings[3].second,headingsModel.HeadingsWithNumbering[3].second)
        assertEquals("2.2 ",headingsModel.HeadingsWithNumbering[3].first)

        assertEquals("Test 3",headingsModel.Headings[4].second)
        assertEquals(1,headingsModel.Headings[4].first)
        assertEquals(headingsModel.Headings[4].second,headingsModel.HeadingsWithNumbering[4].second)
        assertEquals("3 ",headingsModel.HeadingsWithNumbering[4].first)

        assertEquals("Test 3.0.1",headingsModel.Headings[5].second)
        assertEquals(3,headingsModel.Headings[5].first)
        assertEquals(headingsModel.Headings[5].second,headingsModel.HeadingsWithNumbering[5].second)
        assertEquals("3.0.1 ",headingsModel.HeadingsWithNumbering[5].first)
    }

    @Test
    fun checkNoExceptionIsThrownIfNoTocCanBeCreated(){
        val documentString = ""
        val document = parser.parse(documentString)
        headingsModel.generateRefReferenceOfElements(null,document)
        assertDoesNotThrow(headingsModel::generateHeadingNumbering)
    }
}