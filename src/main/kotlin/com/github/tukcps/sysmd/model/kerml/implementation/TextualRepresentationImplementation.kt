package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


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
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    override var language: String="SysML",
    body: String="",
): TextualRepresentation, AnnotatingElementImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    body=body,
) {
    override fun clone(): TextualRepresentation = TextualRepresentationImplementation(model)
        .also { it.updateFrom(this) }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is TextualRepresentation) {
            language = template.language
            body = template.body
        }
    }

    override fun toString(): String {
        return "[TextualRepresentation]" +"body=$body"
    }
}
