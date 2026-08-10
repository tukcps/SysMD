package compiler

import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.MetadataFeatureImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import util.testProjectSession


/**
 * The syntactical construct "Project uses NAME[:Version]" reads
 * a project with name 'NAME' into the current project.
 */
class ImportDocumentTests {


    /**
     * The package in which elements will be added is given as Language parameter of MD.
     * It shall be considered when the compiler is run.
     */
    @Test
    fun importMdWithLanguageAndNamespace() = testProjectSession {
        val input = """
# H1
## H2 
*value* or _value_
```KerML::Import
classifier Test;
```          
        """
        // Create File annotating an element ...
        val fileAnnotation = addOwnedMember(MetadataFeatureImplementation(this, declaredName = "test"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        initialize(Runlevel.NAMES_RESOLVED)
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).language == "KerML::Import")
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).getOwnerPrefix() == "Import")
    }

    /**
     * The namespace/owner prefix shall be considered.
     * And not be added a second time by second compilation.
     */
    @Test fun importMdWithLanguageAndNamespaceTwice() = testProjectSession {
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
        val fileAnnotation = addOwnedMember(MetadataFeatureImplementation(this, declaredName = "test"), global)
        // Parse it, creates Textual representations inside.
        importMD(input, fileAnnotation)
        initialize(Runlevel.NAMES_RESOLVED)
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).language == "KerML::X")
        Assertions.assertTrue((fileAnnotation.ownedElement.last() as TextualRepresentation).getOwnerPrefix() == "X")
    }
}