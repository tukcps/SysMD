package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.migration.ExistingSourceScanner
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests scanning existing Kotlin model sources.
 */
class ExistingSourceScannerTests {

    /**
     * Tests scanning implementation inheritance.
     */
    @Test
    fun scansImplementations() {
        val scanner = ExistingSourceScanner(
            GeneratorConfiguration.MODEL_DIRECTORIES
        )

        val implementations = scanner.implementations()

        implementations.values
            .sortedBy { it.name }
            .forEach {
                println("${it.name} -> ${it.superTypes.joinToString()}")
            }

        val feature = implementations["FeatureImplementation"]
        assertNotNull(feature)
        assertTrue("Feature" in feature.superTypes)
    }
}