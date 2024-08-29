package parsertests

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


/**
 * Tests for the Markdown-Import into a model.
 * The MD-import shall read an input string split it into code-sections and documentation sections.
 * The sections shall be added to the Model of the AgilaService as Annotation Elements.
 */
class ImportMDTests {


    /**
     * MD titles with same text shall be allowed.
     */
    @Test fun importMD_equal_headings_allowed() = testSession(loadKerML = false) {
        val input = """
            # H1
            asdf asdf
            
            # H1 
            
            ## H2
            
            ## H2
            text
        """.trimIndent()
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(4, fileAnnotation.ownedElement.size)
        assertTrue( (fileAnnotation.ownedElement.last().ref as TextualRepresentation).body.contains("text"))
    }

    @Test fun importMD() = testSession(loadKerML = false) {
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
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        initialize()
        assertEquals(8, get().size)
    }


    @Test fun importMdWithEmptyDocumentationAfterCode() = testSession(loadKerML = false) {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Test isA Package.
            ```            
        """.trimIndent()
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(7, get().size)
    }


    /**
     * The language shall be passed including parameters.
     */
    @Test fun importMdWithLanguageAndNamespace() = testSession(loadKerML = false) {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```SysMD::A::B
            Test isA Package.
            ```            
        """.trimIndent()
        // Create File annotating element ...
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test", body="input source name"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        assertEquals(7, get().size)
        // Last one is SysMD with Language set to SysMD::A::B
        assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).language == "SysMD::A::B")
        assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).getOwnerPrefix() == "A::B")
    }

    @Test fun importMdWithNoTrailingTicks() = testSession(loadKerML = false) {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Package Test.
        """.trimIndent()
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(7, get().size)
    }


    @Test fun importMdMergesTitleAndBody() = testSession(loadKerML = false) {
        val input = """
            # H1
            asdf1
            asdf2
            
            # h1
        """.trimIndent()
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        importMD(input, fileAnnotation)
        assertEquals(6, get().size)
    }

    @Test fun importMdAndCompile() = testSession(loadKerML = false) {
        val input = """
            # H1
            ## H2 
            *asdf* or _asdf_
            ```
            Package Test.
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName = "test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertEquals(7, get().size)
        for (it in get().filterIsInstance<TextualRepresentation>()) {
            if (it.language == "SysMD") {
                it.compile()
            }
        }
        initialize()
        val test = global.resolve<Element>("Test")
        assertNotNull(test)
    }

    @Test fun importMdWithYamlHeader() = testSession(loadKerML = false) {
        val input = """
            --- 
            title: Test of Yaml Header
            maintainer: Christoph Grimm
            name: sysmdtest
            --- 
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertTrue(project.maintainer.first() == "Christoph Grimm")
        assertTrue(project.name == "sysmdtest")
    }

    @Test fun importMdWithYamlHeader2() = testSession(loadKerML = false) {
        val input = """
            --- 
            title: Test of Yaml Header
            maintainer: Christoph Grimm
            name: sysmdtest.md
            --- 
            
            [toc]
          
            ---
            
            test
            
            --- 
            
            ## blah 
            
        """.trimIndent()
        // The SysMD file name, represented as annotation.
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName="test"), global)
        // Import the resulting segments of TextualRepresentation / Documentation in MD into the model
        // They shall become owned elements of the file.
        importMD(input, fileAnnotation)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}