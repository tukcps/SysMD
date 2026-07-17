package models.generator

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.elementdata.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElementDataIFTests {


    /**
     * Tests generation of the common element data interface.
     */
    @Test
    fun generatesElementData() {
        val model = loadMetaModel()

        val source = ElementDataIFGenerator()
            .generate(model)

        // Required base properties.
        assertTrue(source.contains("var type: ElementType"))
        assertTrue(source.contains("var elementId: UUID"))

        // Representative data properties.
        assertTrue(source.contains("var declaredName: String?"))
        assertTrue(source.contains("var declaredShortName: String?"))
        assertTrue(source.contains("var isAbstract: Boolean?"))

        // Current OMG Import attributes.
        assertTrue(source.contains("var isImportAll: Boolean?"))
        assertTrue(source.contains("var isRecursive: Boolean?"))

        // Structural graph references.
        assertTrue(
            source.contains(
                "var ownedElement: MutableList<Identified>"
            )
        )
        assertTrue(source.contains("var owner: Identified?"))
        assertTrue(
            source.contains(
                "var owningRelationship: Identified?"
            )
        )
        assertTrue(
            source.contains(
                "var source: MutableList<Identified>"
            )
        )
        assertTrue(
            source.contains(
                "var target: MutableList<Identified>"
            )
        )

        // Historic SysMD helper fields must not be generated.
        assertFalse(source.contains("importedMemberName"))
        assertFalse(source.contains("importedNamespace"))

        // Derived transition properties must not appear.
        assertFalse(source.contains("transitionUsageSource"))
        assertFalse(source.contains("transitionUsageTarget"))
    }

    /**
     * Tests the current OMG Import metaclass.
     */
    @Test
    fun importMetaclassMatchesOMG() {
        val model = loadMetaModel()

        val import = model.allClasses()
            .first { it.name == "Import" }

        assertEquals(
            listOf(
                "importOwningNamespace",
                "visibility",
                "isRecursive",
                "isImportAll",
                "importedElement"
            ),
            import.attributes.map { it.name }
        )
    }
}

class ElementDataDebugTest {
    @Test
    fun debugElementStructuralReferences() {

        val model = loadMetaModel()

        val element = model.allClasses()
            .first { it.name == "Element" }

        val classifier = ElementDataClassifier(model)

        println()
        println("=== Configured ===")
        println(
            GeneratorConfiguration.ELEMENT_DATA_REFERENCES["Element"]
        )

        println()
        println("=== Element attributes ===")

        element.attributes.forEach {
            println(it.name)
        }

        println()
        println("=== Structural references ===")

        element.attributes
            .filter {
                classifier.isStructuralReference(
                    element,
                    it,
                )
            }
            .forEach {
                println(it.name)
            }
    }

    @Test
    fun debugElementDataGeneratorPipeline() {

        val model = loadMetaModel()

        val classifier = ElementDataClassifier(model)
        val mapper = ElementDataTypeMapper(model)

        val infos = mutableListOf<ElementDataPropertyInfo>()

        model.allClasses().forEachIndexed { classOrder, clazz ->

            clazz.attributes.forEachIndexed { attributeOrder, attribute ->

                if (!classifier.isIncluded(clazz, attribute))
                    return@forEachIndexed

                val isReference =
                    classifier.isStructuralReference(
                        clazz,
                        attribute,
                    )

                infos += ElementDataPropertyInfo(
                    className = clazz.name,
                    attributeName = attribute.name,
                    kotlinType = mapper.type(
                        attribute,
                        isReference,
                    ),
                    lower = attribute.lower,
                    upper = attribute.upper,
                    isReference = isReference,
                    isDerived = attribute.isDerived,
                    isReadOnly = attribute.isReadOnly,
                    classOrder = classOrder,
                    attributeOrder = attributeOrder,
                )
            }
        }

        println()
        println("========== INFOS ==========")

        infos
            .filter { it.isReference }
            .forEach {
                println("${it.className}.${it.attributeName}")
            }

        val properties =
            infos
                .groupBy { it.attributeName }
                .flatMap { (attributeName, candidates) ->

                    val byType =
                        candidates.groupBy {
                            it.kotlinType
                        }

                    byType.map { (kotlinType, typedCandidates) ->

                        val info = typedCandidates.first()

                        val name =
                            if (byType.size > 1) {
                                info.className.replaceFirstChar {
                                    it.lowercase()
                                } +
                                        attributeName.replaceFirstChar {
                                            it.uppercase()
                                        }
                            } else {
                                attributeName
                            }

                        ElementDataProperty(
                            name = name,
                            kotlinType = kotlinType,
                            info = info,
                        )
                    }
                }

        println()
        println("======= PROPERTIES ========")

        properties
            .filter { it.info.isReference }
            .forEach {
                println("${it.name}  (${it.info.className})")
            }

        println()
        println("======= DUPLICATES ========")

        infos
            .groupBy { it.attributeName }
            .filterValues { it.size > 1 }
            .forEach { (name, infos) ->

                println(name)

                infos.forEach {
                    println(
                        "    ${it.className}  ${it.kotlinType}"
                    )
                }
            }
    }

    @Test
    fun debugElementReferences() {

        val model = loadMetaModel()

        val element = model.allClasses()
            .first { it.name == "Element" }

        val classifier = ElementDataClassifier(model)

        println()

        element.attributes.forEach { attribute ->

            val reference =
                classifier.isStructuralReference(
                    element,
                    attribute,
                )

            val included =
                classifier.isIncluded(
                    element,
                    attribute,
                )

            if (reference || included) {
                println(
                    "${attribute.name.padEnd(20)} " +
                            "reference=$reference " +
                            "included=$included " +
                            "derived=${attribute.isDerived}"
                )
            }
        }
    }
}

