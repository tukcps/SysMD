package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.firstName
import java.util.*


/**
 * A textual model of something in a modeling language.
 * In extension of the SysMLv2 metamodel, we also save here results of the compilation:
 * - errorsByLine in a hashmap lineno -> error message
 * - infoByLine in a hashmap lineno -> infotext
 * @param declaredShortName a short abbreviation for the name
 * @param declaredName an optional name
 * @param language the language in which the model is given, e.g., SysML v2 textual or SysMD or Markdown
 * @param body the model itself
 */
class TextualRepresentationImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    owner: Resolved<Element> = Resolved(),
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    override var language: String="SysML",
    body: String="",
    elementType: String = "TextualRepresentation"
): TextualRepresentation, AnnotatingElementImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owner = owner,
    ownedElement = ownedElement,
    body=body,
    elementType = elementType
) {
    /**
     * Runs the parser depending on the language field.
     */
    override fun compile(generateAnnotations: Boolean) {
        when (language.firstName()) {
            "SysML", "KerML", "SysMD" -> KerML(model!!,
                textualRepresentation = this,
                indices = null,
                generateAnnotations = generateAnnotations).parseSysMD()
        }
    }

    override fun clone(): TextualRepresentation {
        return TextualRepresentationImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            owner = Resolved(owner),
            ownedElement = Resolved.copyOfIdentityList(ownedElement),
            language = language,
            body = body).also { klon ->
            klon.model = model
            klon.updated = updated
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is TextualRepresentation) {
            // owner = template.owner
            // ownedElements = Identity.copyOfIdentityList(ownedElements)
            language = template.language
            body = template.body
        }
    }

    override fun toString(): String {
        return "TextualRepresentation {" +
                "body=$body, +" +
                "id='${elementId}' }"
    }
}
