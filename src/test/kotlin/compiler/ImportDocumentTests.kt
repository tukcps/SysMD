package compiler

import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.MetadataFeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import util.testSession


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
            ```KerML
            package X;
            ```
            ## H2 
            *value* or _value_
            ```KerML::X
            package Y;
            ``` 
        """.trimIndent()
        // Create File annotating an element ...
        val fileAnnotation = addOwnedMember(MetadataFeatureImplementation(declaredName = "test"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        (fileAnnotation.ownedElement[1] as TextualRepresentation).compile()
        (fileAnnotation.ownedElement[3] as TextualRepresentation).compile()
        initialize()
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).language == "KerML::X")
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).getOwnerPrefix() == "X")
        val test = global.resolve<Package>("X::Y")
        assertNotNull(test)
    }

    /**
     * The package in which elements will be added is given as Language parameter of MD.
     * It shall be considered when the compiler is run.
     */
    @Test
    fun importMdWithLanguageAndNamespace2() = testSession("ScalarValues") {
        val input = """
# H1
## H2 
*value* or _value_
```KerML::ScalarValues
class Test;
```          
        """
        // Create File annotating an element ...
        val fileAnnotation = addOwnedMember(MetadataFeatureImplementation(declaredName = "test"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        (fileAnnotation.ownedElement.last() as TextualRepresentation).compile()
        initialize()
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).language == "KerML::ScalarValues")
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).getOwnerPrefix() == "ScalarValues")
        val test = global.resolve<Classifier>("ScalarValues::Test")
        assertNotNull(test)
    }
}