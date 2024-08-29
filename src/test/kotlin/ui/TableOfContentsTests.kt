package test.uitests

import com.github.tukcps.sysmd.ui.rendering.TableOfContentsRenderer
import com.github.tukcps.sysmd.ui.rendering.isElementTOCElement
import com.github.tukcps.sysmd.ui.rendering.isShortTOCElement
import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Link
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TableOfContentsParserTest {
    lateinit var renderer : TableOfContentsRenderer
    lateinit var parser : Parser
    lateinit var headingsModel : InternalRefReference
    @BeforeEach
    fun setUp() {
        parser = Parser.builder().build()
        headingsModel = InternalRefReference(null) {}
        renderer = TableOfContentsRenderer(headingsModel)
    }

    fun setUpBasicTableOfContent() {
        val documentString = "# Test 1\r\n## Test 2\r\n### Test 3\r\n#### Test 4\r\n##### Test 5"
        val document = parser.parse(documentString)
        headingsModel.generateRefReferenceOfElements(null,document)
        headingsModel.generateHeadingNumbering()
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun checkSuccessfulRecognitionSingleTOC() {
        val document = parser.parse("[TOC]\n")
        val returnValue = isElementTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkSuccessfulRecognitionSingleTOCLowercase() {
        val document = parser.parse("[toc]\n")
        val returnValue = isElementTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkSuccessfulRecognitionSingleUnderscoreTOC() {
        val document = parser.parse("[[_TOC_]]\n")
        val returnValue = isElementTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkSuccessfulRecognitionSingleUnderscoreTOCLowercase() {
        val document = parser.parse("[[_toc_]]\n")
        val returnValue = isElementTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkSuccessfulRecognitionSingleColonTOC() {
        val document = parser.parse("{:toc}\n")
        val returnValue = isElementTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkSuccessfulRecognitionSingleTOCWithAddedContent() {
        val document = parser.parse("[toc] (...)\n")
        val returnValue = isShortTOCElement(document.firstChild)
        assertTrue(returnValue)
    }

    @Test
    fun checkRenderingOfSimpleTableOfContent()
    {
        setUpBasicTableOfContent()
        val element = renderer.generateTOCAsParagraphElement()
        assertNotNull(element)

        var lineOfToc = element.firstChild
        do {
            assertTrue(lineOfToc is Link)
            val splittedTitle = (lineOfToc as Link).title.split(" ")
            val expectedString = "#" + lineOfToc.title.lowercase().removeRange(0,splittedTitle[0].length+1).replace(" ","-")
            assertEquals(expectedString, lineOfToc.destination)
            assertTrue(lineOfToc.firstChild is Text)
            assertEquals(lineOfToc.title,(lineOfToc.firstChild as Text).literal)
            lineOfToc = lineOfToc.next
            assertTrue(lineOfToc is HardLineBreak)
            lineOfToc = lineOfToc.next
        } while (lineOfToc!=null)
    }

    @Test
    fun checkRenderingOfSimpleShortTableOfContent()
    {
        setUpBasicTableOfContent()
        val element = renderer.generateSmallTOC()
        assertNotNull(element)

        var lineOfToc = element.firstChild
        var numberOfElement = 0
        do {
            assertTrue(lineOfToc is Link)
            val splittedTitle = (lineOfToc as Link).title.split(" ")
            val expectedString = "#" + lineOfToc.title.lowercase().removeRange(0,splittedTitle[0].length+1).replace(" ","-")
            assertEquals(expectedString, lineOfToc.destination)
            assertTrue(lineOfToc.firstChild is Text)
            assertEquals(lineOfToc.title,(lineOfToc.firstChild as Text).literal)
            lineOfToc = lineOfToc.next
            assertTrue(lineOfToc is Text)
            assertEquals("(...)",(lineOfToc as Text).literal)
            lineOfToc = lineOfToc.next
            assertTrue(lineOfToc is HardLineBreak)
            lineOfToc = lineOfToc.next
            numberOfElement++
        } while (lineOfToc!=null)
        assertEquals(1,numberOfElement)
    }

    @Test
    fun checkElementNotNullIfNoTocCanBeCreated()
    {
        val documentString = ""
        val document = parser.parse(documentString)
        headingsModel.generateRefReferenceOfElements(null,document)
        assertDoesNotThrow(headingsModel::generateHeadingNumbering)

        val normalToc = renderer.generateTOCAsParagraphElement()
        assertNotNull(normalToc)

        val smallToc = renderer.generateSmallTOC()
        assertNotNull(smallToc)
    }
}
