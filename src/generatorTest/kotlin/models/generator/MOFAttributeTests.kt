package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.OMGDownloader
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.mof.*
import org.w3c.dom.Element
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests loading attributes from the OMG metamodel.
 */
class MOFAttributeTests {

    /**
     * Loads and inspects owned metaclass attributes.
     */
    @Test
    fun loadAttributes() {

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

        val attributes =
            model.allClasses()
                .flatMap { clazz ->
                    clazz.attributes.map { attribute ->
                        clazz to attribute
                    }
                }

        assertTrue(
            attributes.isNotEmpty(),
            "No metamodel attributes loaded."
        )

        println()
        println("============================================================")
        println("MOF Attribute Report")
        println("============================================================")
        println()

        model.allClasses()
            .filter { it.attributes.isNotEmpty() }
            .sortedBy { it.name }
            .forEach { clazz ->

                println(clazz.name)

                clazz.attributes
                    .sortedBy { it.name }
                    .forEach { attribute ->

                        val upper =
                            attribute.upper?.toString() ?: "*"

                        println(
                            "    ${attribute.name}" +
                                    " [${attribute.lower}..$upper]" +
                                    " type=${attribute.typeId}" +
                                    " derived=${attribute.isDerived}" +
                                    " readOnly=${attribute.isReadOnly}" +
                                    " ordered=${attribute.isOrdered}" +
                                    " redefines=${attribute.redefinedAttributeIds}" +
                                    " subsets=${attribute.subsettedAttributeIds}"
                        )
                    }

                println()
            }

        println("Classes      : ${model.allClasses().size}")
        println("Attributes   : ${attributes.size}")
    }

    /**
     * Checks representative attribute metadata.
     */
    @Test
    fun checkRepresentativeAttributes() {

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

        val usage =
            model.allClasses()
                .firstOrNull { it.name == "Usage" }

        assertNotNull(usage, "Usage metaclass not found.")

        val definition =
            usage.attributes
                .firstOrNull { it.name == "definition" }

        assertNotNull(definition, "Usage.definition attribute not found.")

        println()
        println("Usage.definition")
        println("    type       : ${definition.typeId}")
        println("    multiplicity: ${definition.lower}..${definition.upper ?: "*"}")
        println("    derived    : ${definition.isDerived}")
        println("    readOnly   : ${definition.isReadOnly}")
        println("    ordered    : ${definition.isOrdered}")
        println("    redefines  : ${definition.redefinedAttributeIds}")
        println("    subsets    : ${definition.subsettedAttributeIds}")

        assertEquals("definition", definition.name)
        assertTrue(definition.typeId != null, "Usage.definition has no type.")
    }

    /**
     * Tests Kotlin type mapping.
     */
    @Test
    fun mapAttributeTypes() {

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

        model.allClasses()
            .sortedBy { it.name }
            .forEach { clazz ->

                clazz.attributes
                    .sortedBy { it.name }
                    .forEach { attribute ->

                        println(
                            "${clazz.name}.${attribute.name}" +
                                    " -> " +
                                    KotlinTypeMapper.type(
                                        model,
                                        attribute
                                    ) +
                                    " [" +
                                    KotlinTypeMapper.declarationKeyword(
                                        attribute
                                    ) +
                                    "]"
                        )
                    }
            }
    }
}

/**
 * Inspects raw XMI operation parameters.
 */
class MOFOperationXmiTest {

    /**
     * Dumps raw XMI parameters of representative operations.
     */
    @Test
    fun dumpOperationParameters() {
        val document = MOFXmiLoader.load(
            GeneratorConfiguration.KERML_XMI
        )

        val root = document.documentElement

        dumpOperation(root, "Element", "effectiveName")
        dumpOperation(root, "Feature", "canAccess")
        dumpOperation(root, "MultiplicityRange", "hasBounds")
        dumpOperation(root, "Expression", "evaluate")
    }

    /**
     * Dumps the raw parameters of an operation.
     *
     * @param root Root XMI element.
     * @param className Metaclass name.
     * @param operationName Operation name.
     */
    private fun dumpOperation(
        root: Element,
        className: String,
        operationName: String
    ) {
        val clazz = findClass(root, className)
            ?: error("Class not found: $className")

        val operation = clazz.childElements("ownedOperation")
            .firstOrNull { it.attribute("name") == operationName }
            ?: error(
                "Operation not found: $className.$operationName"
            )

        println()
        println("$className.$operationName")

        operation.childElements("ownedParameter")
            .forEach { parameter ->
                println(
                    "    name='${parameter.attribute("name")}'" +
                            " direction='${parameter.attribute("direction")}'" +
                            " xmi:type='${parameter.attribute("xmi:type")}'"
                )

                println(
                    "        type=" +
                            parameter.childElement("type")?.referenceId()
                )

                println(
                    "        lowerValue=" +
                            parameter.childElement("lowerValue")
                                ?.attribute("value")
                )

                println(
                    "        upperValue=" +
                            parameter.childElement("upperValue")
                                ?.attribute("value")
                )
            }
    }

    /**
     * Finds a metaclass recursively.
     *
     * @param element Current XML element.
     * @param className Metaclass name.
     * @return Metaclass element, or null if not found.
     */
    private fun findClass(
        element: Element,
        className: String
    ): Element? {
        element.childElements("packagedElement")
            .forEach { child ->
                if (
                    child.attribute("xmi:type") == "uml:Class" &&
                    child.attribute("name") == className
                ) {
                    return child
                }

                if (child.attribute("xmi:type") == "uml:Package") {
                    findClass(child, className)?.let {
                        return it
                    }
                }
            }

        element.childElements("uml:Package")
            .forEach { pkg ->
                findClass(pkg, className)?.let {
                    return it
                }
            }

        return null
    }
}