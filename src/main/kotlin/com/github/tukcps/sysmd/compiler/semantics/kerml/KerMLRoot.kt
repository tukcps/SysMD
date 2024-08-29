package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions


/**
 * Class with functions that add a comment.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 * @param owner allows overriding the default owner in the current parse run
 * @param identification holds the declared name and shortName if any
 * @param created holds, after creation, a reference to the created namespace
 */
class CommentActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var body: String = "",
    var created: Comment? = null,
    var about: List<QualifiedName> = mutableListOf()
) {
    fun create(): Comment {
        created = CommentImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            body = body.trim(),
            owner = context.owners.peek(),
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        about.forEach {
            val annotation = AnnotationImplementation(
                owner = Resolved(ref = created!!),
                annotatingElement = Resolved(ref = created!!),
                annotatedElement = Resolved(str = it)
            )
            context.model.addUnownedElement(annotation, startOfOwnerPath =  created!!)
        }
        return created!!
    }
}


/**
 * Class with functions that add a comment.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 * @param owner allows overriding the default owner in the current parse run
 * @param identification holds the declared name and shortName if any
 * @param created holds, after creation, a reference to the created namespace
 */
class DocumentationActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var body: String = "",
    var created: Documentation? = null,
) {
    fun create(): Documentation {
        created = DocumentationImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            body = body.trim(),
            owner = context.owners.peek(),
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        return created!!
    }
}



/**
 * Class with functions that add a textual representation.
 *
 * @param context holds the core data needed by all semantic actions
 * @param owner allows overriding the default owner in the current parse run
 * @param identification holds the declared name and shortName if any
 * @param created holds, after creation, a reference to the created namespace
 */
class TextualRepresentationActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var language: String? = null,
    var body: String = "",
    var created: TextualRepresentation? = null,
) {
    fun create(): TextualRepresentation {
        created = TextualRepresentationImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            body = body.trim(),
            language = language?:"SysML",
            owner = context.owners.peek(),
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        return created!!
    }
}



/**
 * Class with functions that add a namespace.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 * @param owner allows overriding the default owner in the current parse run
 * @param identification holds the declared name and shortName if any
 * @param created holds, after creation, a reference to the created namespace
 */
class NamespaceActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var created: Namespace? = null
) {
    fun create(): Namespace {
        created = NamespaceImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            owner = context.owners.peek(),
            isStandard = STANDARD in context.prefixes,
            isLibraryElement = LIBRARY in context.prefixes,
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
        return created!!
    }
}



/**
 * Class with functions that add an import.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 * @param owner allows overriding the default owner in the current parse run
 * @param identification holds the declared name and shortName if any
 * @param created holds, after creation, a reference to the created namespace
 */
class ImportActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var namespace: String? = null,
    var all: Boolean? = null,
    var isRecursive: Boolean = false,
    var created: Import? = null
) {
    fun create() {
        if (namespace != null) {
            created = NamespaceImportImplementation(
                owner = Resolved(owner),
                declaredName = identification?.name,
                declaredShortName = identification?.shortName,
                importedNamespace = Resolved(namespace!!)
            )
            created?.textualRepresentation = mutableListOf(context.textualRepresentation)
            context.model.addUnownedElement(created!!, context.ownerName())
        }
    }
}



/**
 * Adds a relationship/association types by Link to the KerML instances.
 * @param owner Qualified name of the owner
 * @param identification name, short name
 * @param source list of all sources
 * @param target list of all targets
 */
class DependencyActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var source: List<QualifiedName> = mutableListOf(),
    var target: List<QualifiedName> = mutableListOf(),
    var created: Dependency? = null
) {
    fun create() = with(context) {
        created = DependencyImplementation(
            owner = owners.peek(),
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            client = source.toIdentityList(),
            supplier = target.toIdentityList(),
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        model.addUnownedElement(created!!, owner)

        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}