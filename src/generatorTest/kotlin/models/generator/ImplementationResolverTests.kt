package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.migration.ExistingSourceScanner
import com.github.tukcps.sysmd.model.generator.migration.ImplementationResolver
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests resolving existing Kotlin implementations.
 */
class ImplementationResolverTests {

    @Test
    fun resolvesImplementationSuperClass() {
        val model = MOFMetaModel()

        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.KERML_XMI,
            model
        )
        MOFXmiLoader.loadMetaModel(
            GeneratorConfiguration.SYSML_XMI,
            model
        )

        val resolver = ImplementationResolver(
            model,
            ExistingSourceScanner(
                GeneratorConfiguration.MODEL_DIRECTORIES
            )
        )

        val expression = model.allClasses()
            .first { it.name == "Expression" }
        val actorMembership = model.allClasses()
            .first { it.name == "ActorMembership" }

        assertEquals(
            "Step",
            resolver.implementationSuperClass(expression)?.name
        )
        assertEquals(
            "ParameterMembership",
            resolver.implementationSuperClass(actorMembership)?.name
        )
    }

    @Test
    fun resolvesRequiredMembers() {
        val model = loadMetaModel()

        val resolver = ImplementationResolver(
            model,
            ExistingSourceScanner(
                GeneratorConfiguration.MODEL_DIRECTORIES
            )
        )

        val feature = model.allClasses()
            .first { it.name == "Feature" }

        assertTrue(resolver.requiredAttributes(feature).isEmpty())
        assertTrue(resolver.requiredOperations(feature).isEmpty())
    }

    /**
     * Loads the KerML and SysML metamodels.
     *
     * @return Loaded MOF metamodel.
     */
    private fun loadMetaModel(): MOFMetaModel =
        MOFMetaModel().also { model ->
            MOFXmiLoader.loadMetaModel(
                GeneratorConfiguration.KERML_XMI,
                model
            )
            MOFXmiLoader.loadMetaModel(
                GeneratorConfiguration.SYSML_XMI,
                model
            )
        }
}