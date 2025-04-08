package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * We organize semantic actions as classes that re-use methods and fields.
 * SemanticAction is the base class of all semantic actions.
 * @param T the type of the created element in the abstract representation (model).
 * @param context the context of the compile-run, including, e.g., a stack of owning scopes.
 * @param creator a lambda that is called when the creation process is finished.
 * The result is stored in the field created.
 */
open class SemanticAction<T: Element>(
    val context: ActionsContext,
    var creator: (SimpleName?, SimpleName?) -> T,
) {
    var created: T? = null

    /**
     * Creates a KerML element and prepares it for integration into the model.
     * Depending on the context, a UUID 4 or 5 is assigned.
     * @param identification name and short name
     */
    open fun create(identification: Identification) {
        created = creator(identification.name, identification.shortName)
        created?.input = context.compiler.input
        if (context.owners.peek().ref != null)
            context.model.addUnownedElement(created!!, startOfOwnerPath = context.owners.peek().ref!!)
        else
            context.model.addUnownedElement(created!!, context.ownerName())

        context.model.status.createdElements.add(
            context.qualifiedName(identification.name?:identification.shortName)
        )
    }

    /**
     * Sets the name, shortname, and generates a UUID5 if needed.
     * Note that the UUID5 is based on the owner that is defined in the context!
     * @param identification, Identification data structure that includes short name and name
     */
    open fun setIdentification(identification: Identification) {
        if ((context.owners.peek().ref?.isLibraryElement == true || Token.Kind.LIBRARY in context.prefixes || Token.Kind.STANDARD in context.prefixes)
                && (identification.name != null || identification.shortName != null))
            created!!.elementId =  Generators.nameBasedGenerator().generate(context.qualifiedName(identification.name?:identification.shortName))
        created!!.declaredShortName = identification.shortName
        created!!.declaredName = identification.name
    }
}