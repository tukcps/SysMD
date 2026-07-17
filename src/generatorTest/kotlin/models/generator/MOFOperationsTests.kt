package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.migration.ExistingSourceScanner
import com.github.tukcps.sysmd.model.generator.migration.ImplementationResolver
import com.github.tukcps.sysmd.model.generator.mof.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests loading MOF operations and parameters.
 */
class MOFOperationTests {

    /**
     * Tests operation parameters, return values and multiplicities.
     */
    @Test
    fun loadOperations() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.KERML_XMI,
            model
        )

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.SYSML_XMI,
            model
        )

        val element = model.findClass("Element")
        val effectiveName = element.operation("effectiveName")

        assertEquals(1, effectiveName.parameters.size)

        effectiveName.parameters.single().let {
            assertEquals(MOFParameterDirection.RETURN, it.direction)
            assertEquals("String", it.typeId)
            assertEquals(0, it.lower)
            assertEquals(1, it.upper)
        }

        val feature = model.findClass("Feature")
        val canAccess = feature.operation("canAccess")

        assertEquals(2, canAccess.parameters.size)

        canAccess.parameters[0].let {
            assertEquals("feature", it.name)
            assertEquals(MOFParameterDirection.IN, it.direction)
            assertEquals("Core-Features-Feature", it.typeId)
            assertEquals(1, it.lower)
            assertEquals(1, it.upper)
        }

        canAccess.parameters[1].let {
            assertEquals("", it.name)
            assertEquals(MOFParameterDirection.RETURN, it.direction)
            assertEquals("Boolean", it.typeId)
            assertEquals(1, it.lower)
            assertEquals(1, it.upper)
        }

        val allRedefinedFeatures = feature.operation("allRedefinedFeatures")
        val returnParameter = allRedefinedFeatures.parameters.single()

        assertEquals(MOFParameterDirection.RETURN, returnParameter.direction)
        assertEquals("Core-Features-Feature", returnParameter.typeId)
        assertEquals(0, returnParameter.lower)
        assertNull(returnParameter.upper)
        assertEquals(false, returnParameter.isOrdered)

        val expression = model.findClass("Expression")
        val evaluate = expression.operation("evaluate")

        assertEquals(2, evaluate.parameters.size)

        evaluate.parameters[1].let {
            assertEquals("result", it.name)
            assertEquals(MOFParameterDirection.IN, it.direction)
            assertEquals("Root-Elements-Element", it.typeId)
            assertEquals(0, it.lower)
            assertNull(it.upper)
            assertEquals(true, it.isOrdered)
        }
    }

    /**
     * Returns a metaclass by name.
     *
     * @param name Metaclass name.
     * @return Metaclass.
     */
    private fun MOFMetaModel.findClass(name: String): MOFClass =
        allClasses().firstOrNull { it.name == name }
            ?: error("Metaclass not found: $name")

    /**
     * Returns an operation by name.
     *
     * @param name Operation name.
     * @return Operation.
     */
    private fun MOFClass.operation(name: String): MOFOperation =
        operations.firstOrNull { it.name == name }
            ?: error("Operation not found: ${this.name}.$name")


    /**
     * Reports implementations covering missing intermediate implementations.
     */
    @Test
    fun reportsImplementationGaps() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.KERML_XMI, model)
        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.SYSML_XMI, model)

        val scanner = ExistingSourceScanner(
            GeneratorConfiguration.MODEL_DIRECTORIES
        )

        val resolver = ImplementationResolver(model, scanner)

        model.allClasses()
            .filter(resolver::hasImplementation)
            .map { it to resolver.requiredClasses(it) }
            .filter { (_, required) -> required.size > 1 }
            .sortedBy { (clazz, _) -> clazz.name }
            .forEach { (clazz, required) ->
                println(
                    "${clazz.name}Implementation -> " +
                            required.joinToString(" -> ") { it.name }
                )
            }
    }

    /**
     * Tests resolution across missing intermediate implementations.
     */
    @Test
    fun resolvesImplementationGap() {
        val model = loadMetaModel()

        val resolver = ImplementationResolver(
            model,
            ExistingSourceScanner(
                GeneratorConfiguration.MODEL_DIRECTORIES
            )
        )

        val partUsage = model.allClasses()
            .first { it.name == "PartUsage" }

        assertTrue(
            resolver.requiredClasses(partUsage).isEmpty()
        )
    }

    @Test
    fun reportsImplementedMembers() {
        val scanner = ExistingSourceScanner(
            GeneratorConfiguration.MODEL_DIRECTORIES
        )

        val feature = scanner.implementations()["FeatureImplementation"]
        assertNotNull(feature)

        println("FeatureImplementation properties")
        feature.properties.sorted().forEach { println("    $it") }

        println("FeatureImplementation operations")
        feature.operations
            .sortedWith(
                compareBy(
                    ExistingSourceScanner.OperationSignature::name,
                    { it.parameterTypes.joinToString() }
                )
            )
            .forEach {
                println("    ${it.name}(${it.parameterTypes.joinToString()})")
            }

        assertTrue(feature.properties.isNotEmpty())
        assertTrue(feature.operations.isNotEmpty())
    }

    @Test
    fun reportsMissingMembers() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.KERML_XMI, model)
        MOFXmiLoader.loadMetaModel(GeneratorConfiguration.SYSML_XMI, model)

        val scanner = ExistingSourceScanner(
            GeneratorConfiguration.MODEL_DIRECTORIES
        )

        val resolver = ImplementationResolver(model, scanner)
        val feature = model.allClasses().first { it.name == "Feature" }

        println("FeatureImplementation missing attributes")
        resolver.missingAttributes(feature)
            .sortedBy { it.name }
            .forEach { println("    ${it.name}") }

        println("FeatureImplementation missing operations")
        resolver.missingOperations(feature)
            .sortedBy { it.name }
            .forEach { println("    ${it.name}") }
    }
}
