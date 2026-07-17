package com.github.tukcps.sysmd.model.generator.mof

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory


object MOFXmiLoader {

    /**
     * Loads an XML document.
     *
     * @param file XML file.
     * @return Loaded XML document.
     */
    fun load(
        file: Path
    ): Document {

        require(file.toFile().exists()) {
            "File not found: $file"
        }

        val factory =
            DocumentBuilderFactory.newInstance()

        factory.isNamespaceAware = true

        return factory
            .newDocumentBuilder()
            .parse(file.toFile())
    }

    /**
     * Reads an XMI metamodel.
     *
     * @param file XMI file.
     * @return Loaded metamodel.
     */
    fun readMetaModel(
        file: Path
    ): MOFMetaModel {

        val model = MOFMetaModel()

        loadMetaModel(
            file,
            model
        )

        return model
    }

    /**
     * Reads an XMI metamodel and merges it into an existing metamodel.
     *
     * @param file XMI file.
     * @param model Metamodel receiving the imported packages.
     */
    fun loadMetaModel(
        file: Path,
        model: MOFMetaModel
    ) {

        val document =
            load(file)

        val root =
            document.documentElement

        val packageElement =
            root.children()
                .first {
                    it.nodeName == "uml:Package"
                }

        model.packages +=
            readPackage(packageElement)
    }

    /**
     * Reads a package recursively.
     *
     * @param element Package element.
     * @return Package.
     */
    private fun readPackage(
        element: Element
    ): MOFPackage {

        val pkg =
            MOFPackage(
                id = element.attribute("xmi:id"),
                name = element.attribute("name")
            )

        // Nested packages
        element.childElements("packagedElement")
            .filter {
                it.attribute("xmi:type") == "uml:Package"
            }
            .forEach {

                val child =
                    readPackage(it)

                child.owningPackage = pkg

                pkg.packages += child
            }

        // Classes
        element.childElements("packagedElement")
            .filter {
                it.attribute("xmi:type") == "uml:Class"
            }
            .forEach {

                val clazz =
                    readClass(it)

                clazz.owningPackage = pkg

                pkg.classes += clazz
            }

        return pkg
    }

    /**
     * Reads a metaclass.
     *
     * Only members directly owned by the metaclass are read.
     * Inherited members are represented by the superclass hierarchy.
     *
     * @param element UML class element.
     * @return Metaclass.
     */
    private fun readClass(
        element: Element
    ): MOFClass {

        val clazz =
            MOFClass(
                id = element.attribute("xmi:id"),
                name = element.attribute("name"),
                isAbstract =
                    element.attribute("isAbstract") == "true"
            )

        // Direct superclasses
        element.childElements("generalization")
            .forEach { generalization ->

                generalization
                    .childElement("general")
                    ?.referenceId()
                    ?.let(clazz.superClassIds::add)
            }

        // Owned attributes
        element.childElements("ownedAttribute")
            .forEach { attribute ->

                clazz.attributes +=
                    readAttribute(attribute)
            }

        // Owned operations
        element.childElements("ownedOperation")
            .forEach { operation ->

                clazz.operations +=
                    readOperation(operation)
            }

        return clazz
    }

    /**
     * Reads an owned attribute.
     *
     * @param element UML property element.
     * @return MOF attribute.
     */
    private fun readAttribute(
        element: Element
    ): MOFAttribute {

        val lower =
            element
                .childElement("lowerValue")
                ?.attribute("value")
                ?.takeIf { it.isNotBlank() }
                ?.toIntOrNull()
                ?: 0

        val upperValue =
            element
                .childElement("upperValue")
                ?.attribute("value")
                ?.takeIf { it.isNotBlank() }
                ?.toIntOrNull()
                ?: 1

        val attribute =
            MOFAttribute(
                id = element.attribute("xmi:id"),
                name = element.attribute("name"),
                typeId =
                    element
                        .childElement("type")
                        ?.referenceId(),
                lower = lower,
                upper =
                    upperValue.takeIf {
                        it >= 0
                    },
                isDerived =
                    element.attribute("isDerived") == "true",
                isReadOnly =
                    element.attribute("isReadOnly") == "true",
                isOrdered =
                    element.attribute("isOrdered") == "true"
            )

        element.childElements("redefinedProperty")
            .mapNotNull {
                it.referenceId()
            }
            .forEach(
                attribute.redefinedAttributeIds::add
            )

        element.childElements("subsettedProperty")
            .mapNotNull {
                it.referenceId()
            }
            .forEach(
                attribute.subsettedAttributeIds::add
            )

        return attribute
    }

    /**
     * Reads an owned operation.
     *
     * @param element UML operation element.
     * @return MOF operation.
     */
    private fun readOperation(element: Element): MOFOperation {
        val operation = MOFOperation(
            id = element.attribute("xmi:id"),
            name = element.attribute("name")
        )

        element.childElements("ownedParameter")
            .forEach { parameter ->
                operation.parameters += readParameter(parameter)
            }

        return operation
    }

    /**
     * Reads an operation parameter.
     *
     * @param element UML parameter element.
     * @return MOF parameter.
     */
    private fun readParameter(element: Element): MOFParameter {
        val name = element.attribute("name")
        val direction = element.attribute("direction")

        val lowerElement = element.childElement("lowerValue")
        val upperElement = element.childElement("upperValue")

        val lower = if (lowerElement == null) {
            1
        } else {
            lowerElement.attribute("value").toIntOrNull() ?: 0
        }

        val upperValue = if (upperElement == null) {
            1
        } else {
            upperElement.attribute("value").toIntOrNull() ?: 1
        }

        return MOFParameter(
            id = element.attribute("xmi:id"),
            name = name,
            typeId = element.childElement("type")?.referenceId(),
            direction = parameterDirection(direction, name),
            lower = lower,
            upper = upperValue.takeIf { it >= 0 },
            isOrdered = element.attribute("isOrdered") == "true"
        )
    }
}

/**
 * Returns the direction of an operation parameter.
 *
 * The OMG KerML and SysML XMI models represent unnamed parameters as
 * return parameters if no explicit direction is specified.
 *
 * @param direction Explicit XMI parameter direction.
 * @param name Parameter name.
 * @return Parameter direction.
 */
private fun parameterDirection(
    direction: String,
    name: String
): MOFParameterDirection =
    when (direction) {
        "in" -> MOFParameterDirection.IN
        "out" -> MOFParameterDirection.OUT
        "inout" -> MOFParameterDirection.INOUT
        "return" -> MOFParameterDirection.RETURN
        "" -> if (name.isEmpty()) MOFParameterDirection.RETURN else MOFParameterDirection.IN
        else -> error("Unknown parameter direction: $direction")
    }