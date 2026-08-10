package compiler

import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import util.assertNoIssues
import util.testProjectSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for the Markdown-Import into a model.
 * The MD-import shall read an input string split it into code-sections and documentation sections.
 * The sections shall be added to the Model of the AgilaService as Annotation Elements.
 */
class ImportMDTests {


    /**
     * MD titles with the same text shall be allowed.
     */
    @Test fun importMD_equal_headings_allowed() = testProjectSession {
        val input = """
            # H1
            asdf asdf
            
            # H1 
            
            ## H2
            
            ## H2
            text
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName="test"), global)
        importMD(input, fileAnnotation)
        initialize(Runlevel.NAMES_RESOLVED)
        assertNoIssues()
        assertEquals(4, fileAnnotation.ownedElement.size)
        assertTrue( (fileAnnotation.ownedElement.last() as TextualRepresentation).body.contains("text"))
    }

    @Test fun importMarkdown() = testProjectSession {
        val input = """
            # H1
            
            ## H2 
            
            *asdf* or _asdf_
            ```
            Test isA Package.
            ```
            
            # H1_2
            Some text 
            
        """.trimIndent()
        importMD(input, global)
        assertEquals(5, global.ownedElement.size)
    }


    @Test fun importMdWithEmptyDocumentationAfterCode() = testProjectSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Test isA Package.
            ```            
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName="test"), global)
        importMD(input, fileAnnotation)
        val member = global.resolve("test")?.member<Element>()
        assertEquals(3, member?.ownedElement?.size)
    }


    @Test fun importMdWithNoTrailingTicks() = testProjectSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Package Test.
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(3, global.resolve("test")?.member<Namespace>()!!.ownedElement.size)
    }


    @Test fun importMdMergesTitleAndBody() = testProjectSession {
        val input = """
            # H1
            asdf1
            asdf2
            
            # h1
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName="test"), global)
        importMD(input, fileAnnotation)
        val member = global.resolve("test")?.member<Element>()
        assertEquals(2, member?.ownedElement?.size)
    }

    @Test fun importMdWithLanguage() = testProjectSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```KerML
            package Test;
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName = "test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        val kerml = global.resolve("test")?.member<Element>()?.ownedElement
            ?.find { it is TextualRepresentation && it.language == "KerML" }
        assertNotNull(kerml)
    }

    @Test fun importMdWithYamlHeader2() = testProjectSession {
        val input = """
            --- 
            title: Test of Yaml Header
            author: Christoph Grimm
            --- 
            
            [toc]
          
            ---
            
            test
            
            --- 
            
            ## blah 
            
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = addOwnedMember(NamespaceImplementation(this, declaredName="test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertNoIssues()
    }
}