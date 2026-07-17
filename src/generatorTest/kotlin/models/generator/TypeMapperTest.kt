package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.mof.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Tests mapping MOF operation parameter types to Kotlin types.
 */
class KotlinTypeMapperTests {

    /**
     * Tests scalar, nullable and collection parameter types.
     */
    @Test
    fun mapsOperationParameterTypes() {
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
        assertEquals(
            "String?",
            KotlinTypeMapper.type(model, effectiveName.returnParameter())
        )

        val feature = model.findClass("Feature")
        val canAccess = feature.operation("canAccess")

        assertEquals(
            "Feature",
            KotlinTypeMapper.type(model, canAccess.parameter("feature"))
        )

        assertEquals(
            "Boolean",
            KotlinTypeMapper.type(model, canAccess.returnParameter())
        )

        val allRedefinedFeatures = feature.operation("allRedefinedFeatures")
        assertEquals(
            "MutableSet<Feature>",
            KotlinTypeMapper.type(model, allRedefinedFeatures.returnParameter())
        )

        val asCartesianProduct = feature.operation("asCartesianProduct")
        assertEquals(
            "MutableList<Type>",
            KotlinTypeMapper.type(model, asCartesianProduct.returnParameter())
        )

        val expression = model.findClass("Expression")
        val evaluate = expression.operation("evaluate")

        assertEquals(
            "Element",
            KotlinTypeMapper.type(model, evaluate.parameter("target"))
        )

        assertEquals(
            "MutableList<Element>",
            KotlinTypeMapper.type(model, evaluate.parameter("result"))
        )
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
     * Returns a parameter by name.
     *
     * @param name Parameter name.
     * @return Parameter.
     */
    private fun MOFOperation.parameter(name: String): MOFParameter =
        parameters.firstOrNull { it.name == name }
            ?: error("Parameter not found: ${this.name}.$name")

    /**
     * Returns the operation return parameter.
     *
     * @return Return parameter.
     */
    private fun MOFOperation.returnParameter(): MOFParameter =
        parameters.firstOrNull {
            it.direction == MOFParameterDirection.RETURN
        } ?: error("Return parameter not found: $name")
}