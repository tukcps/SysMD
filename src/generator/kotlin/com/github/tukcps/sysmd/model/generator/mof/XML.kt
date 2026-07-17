package com.github.tukcps.sysmd.model.generator.mof

import org.w3c.dom.Element

/**
 * Returns all direct child XML elements.
 * Text nodes, comments and other non-element nodes are ignored.
 *
 * @return List of direct child XML elements.
 */
fun Element.children(): List<Element> =
    (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filterIsInstance<Element>()

/**
 * Returns the value of the specified XML attribute.
 *
 * @param name Name of the XML attribute.
 * @return Attribute value, or an empty string if the attribute does not exist.
 */
fun Element.attribute(name: String): String =
    getAttribute(name)

/**
 * Returns the value of the specified XML attribute.
 *
 * @param name Name of the XML attribute.
 * @return Attribute value, or null if the attribute does not exist.
 */
fun Element.attributeOrNull(name: String): String? =
    getAttribute(name).ifBlank { null }

/**
 * Returns all direct child XML elements with the specified node name.
 *
 * @param name XML node name.
 * @return List of matching child XML elements.
 */
fun Element.childElements(name: String): List<Element> =
    children().filter { it.nodeName == name }

/**
 * Returns the first direct child XML element with the specified node name.
 *
 * @param name XML node name.
 * @return Matching child XML element, or null if none exists.
 */
fun Element.childElement(name: String): Element? =
    children().firstOrNull { it.nodeName == name }


fun collectClasses(
    pkg: MOFPackage,
    result: MutableList<MOFClass>
) {
    result += pkg.classes
    pkg.packages.forEach {
        collectClasses(it, result)
    }
}

fun Element.referenceId(): String? =
    attributeOrNull("xmi:idref")
        ?: attributeOrNull("href")?.substringAfter('#')