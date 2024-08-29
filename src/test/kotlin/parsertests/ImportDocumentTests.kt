package parsertests

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test


/**
 * The syntactical construct "Project uses NAME[:Version]" reads
 * a project with name 'NAME' into the current project.
 */
class ImportDocumentTests {


    /**
     * The namespace/owner prefix shall be considered.
     */
    @Test fun importMdWithLanguageAndNamespace() = testSession {
        val input = """
            # H1
            ```SysMD
            Package X.
            ```
            ## H2 
            *value* or _value_
            ```SysMD::X
            Package Y.
            ```            
        """.trimIndent()
        // Create File annotating element ...
        val fileAnnotation = create(AnnotatingElementImplementation(declaredName = "test", body = "input source name"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        (fileAnnotation.ownedElement[1].ref as TextualRepresentation).compile()
        (fileAnnotation.ownedElement[3].ref as TextualRepresentation).compile()
        initialize()
        Assertions.assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).language == "SysMD::X")
        Assertions.assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).getOwnerPrefix() == "X")
        val test = global.resolve<Package>("X::Y")
        assertNotNull(test)
    }

    /**
     * The namespace shall be considered.
     */
    @Test
    fun importMdWithLanguageAndNamespace2() = testSession("ScalarValues") {
        val input = """
            # H1
            ## H2 
            *value* or _value_
            ```SysMD::ScalarValues
            class Test isA Any.
            ```          
        """.trimIndent()
        // Create File annotating element ...
        val fileAnnotation =
            create(AnnotatingElementImplementation(declaredName = "test", body = "input source name"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        (fileAnnotation.ownedElement.last().ref as TextualRepresentation).compile()
        initialize()
        Assertions.assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).language == "SysMD::ScalarValues")
        Assertions.assertTrue((fileAnnotation.ownedElement.last().ref as TextualRepresentation).getOwnerPrefix() == "ScalarValues")
        val test = global.resolve<Classifier>("ScalarValues::Test")
        assertNotNull(test)
    }
}