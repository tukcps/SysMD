package compiler

import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.services.initialize
import util.testSession
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
    @Test fun importMD_equal_headings_allowed() = testSession {
        val input = """
            # H1
            asdf asdf
            
            # H1 
            
            ## H2
            
            ## H2
            text
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(4, fileAnnotation.ownedElement.size)
        assertTrue( (fileAnnotation.ownedElement.last() as TextualRepresentation).body.contains("text"))
    }

    @Test fun importMarkdown() = testSession {
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
        val fileAnnotation = addOwnedMember(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, global)
        initialize()
        assertEquals(15, get().size)
    }


    @Test fun importMdWithEmptyDocumentationAfterCode() = testSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Test isA Package.
            ```            
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(13, get().size)
    }


    /**
     * The language shall be passed including parameters.
     */
    @Test fun importMdWithLanguageAndNamespace() = testSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```SysMD::A::B
            Test isA Package.
            ```            
        """.trimIndent()
        // Create File annotating the element ...
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        assertEquals(13, get().size)
        // The last one is SysMD with the Language set to SysMD::A::B
        assertEquals((fileAnnotation.ownedElement.last() as TextualRepresentation).language, "SysMD::A::B")
        assertEquals((fileAnnotation.ownedElement.last() as TextualRepresentation).getOwnerPrefix(), "A::B")
    }

    @Test fun importMdWithNoTrailingTicks() = testSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Package Test.
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(3, global.resolve("test")?.member<Namespace>()!!.ownedElement.size)
    }


    @Test fun importMdMergesTitleAndBody() = testSession {
        val input = """
            # H1
            asdf1
            asdf2
            
            # h1
        """.trimIndent()
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(11, get().size)
    }

    @Test fun importMdAndCompile() = testSession {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```KerML
            package Test;
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName = "test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertEquals(13, get().size)
        for (it in get().filterIsInstance<TextualRepresentation>()) {
            if (it.language == "KerML") {
                it.compile()
            }
        }
        initialize()
        val test = global.resolve("Test")?.memberElement
        assertNotNull(test)
    }

    @Test fun importMdWithYamlHeader2() = testSession {
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
        val fileAnnotation = addOwnedMember(NamespaceImplementation(declaredName="test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}