package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.mof.MOFHierarchyResolver
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * Tests MOF hierarchy resolution.
 */
class MOFHierarchyResolverTests {

    /**
     * Tests transitive inheritance of attributes and operations.
     */
    @Test
    fun resolvesInheritedMembers() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.KERML_XMI, model)
        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.SYSML_XMI, model)

        val resolver = MOFHierarchyResolver(model)
        val feature = model.allClasses().first { it.name == "Feature" }

        val superClasses = resolver.allSuperClasses(feature).map { it.name }
        val attributes = resolver.inheritedAttributes(feature).map { it.name }
        val operations = resolver.inheritedOperations(feature).map { it.name }

        println("Feature superclasses")
        superClasses.forEach { println("    $it") }

        println("Feature inherited attributes")
        attributes.sorted().forEach { println("    $it") }

        println("Feature inherited operations")
        operations.sorted().forEach { println("    $it") }

        assertTrue("Type" in superClasses)
        assertTrue("Element" in superClasses)
        assertTrue("declaredName" in attributes)
        assertTrue("path" in operations)

        val hierarchy = resolver.hierarchy(feature).map { it.name }

        println("Feature hierarchy")
        hierarchy.forEach { println("    $it") }

        assertTrue(hierarchy.indexOf("Element") < hierarchy.indexOf("Namespace"))
        assertTrue(hierarchy.indexOf("Namespace") < hierarchy.indexOf("Type"))
        assertTrue(hierarchy.indexOf("Type") < hierarchy.indexOf("Feature"))
    }
}