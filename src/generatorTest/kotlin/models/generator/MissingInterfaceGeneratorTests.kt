package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.migration.MissingInterfaceGenerator
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import com.github.tukcps.sysmd.model.generator.OMGDownloader
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

/**
 * Tests generation of model interfaces.
 */
class MissingInterfaceGeneratorTest {

    /**
     * Generates interface sources and verifies representative properties
     * and operations.
     */
    @Test
    fun generateInterfaces() {
        OMGDownloader.updateAll()

        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.KERML_XMI,
            model
        )

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.SYSML_XMI,
            model
        )

        MissingInterfaceGenerator().generate(model)

        val elementSource = Files.readString(
            findGenerated("Element.kt.generated")
        )

        val featureSource = Files.readString(
            findGenerated("Feature.kt.generated")
        )

        val flowSource = Files.readString(
            findGenerated("Flow.kt.generated")
        )

        val commentSource = Files.readString(
            findGenerated("Comment.kt.generated")
        )

        val expressionSource = Files.readString(
            findGenerated("Expression.kt.generated")
        )

        assertTrue(
            "val aliasIds: MutableList<String>" in elementSource,
            "Element.aliasIds was not generated correctly."
        )

        assertTrue(
            "var declaredName: String?" in elementSource,
            "Element.declaredName was not generated correctly."
        )

        assertTrue(
            "val owner: Element?" in elementSource,
            "Element.owner was not generated correctly."
        )

        assertTrue(
            "val flowEnd: MutableList<FlowEnd>" in flowSource,
            "Flow.flowEnd was not generated correctly."
        )

        assertTrue(
            "var body: String" in commentSource,
            "Comment.body was not generated correctly."
        )

        assertTrue(
            "var locale: String?" in commentSource,
            "Comment.locale was not generated correctly."
        )

        assertTrue(
            "fun effectiveName(): String?" in elementSource,
            "Element.effectiveName was not generated correctly."
        )

        assertTrue(
            "fun canAccess(feature: Feature): Boolean" in featureSource,
            "Feature.canAccess was not generated correctly."
        )

        assertTrue(
            "fun allRedefinedFeatures(): MutableSet<Feature>" in featureSource,
            "Feature.allRedefinedFeatures was not generated correctly."
        )

        assertTrue(
            "fun asCartesianProduct(): MutableList<Type>" in featureSource,
            "Feature.asCartesianProduct was not generated correctly."
        )

        assertTrue(
            "fun evaluate(target: Element, result: MutableList<Element>)" in expressionSource,
            "Expression.evaluate was not generated correctly."
        )
    }

    /**
     * Finds a generated interface source.
     *
     * @param fileName Generated file name.
     * @return Generated source path.
     */
    private fun findGenerated(fileName: String): Path =
        GeneratorConfiguration.MODEL_DIRECTORIES
            .asSequence()
            .map { it.resolve(fileName) }
            .firstOrNull(Files::exists)
            ?: error("Generated file not found: $fileName")
}