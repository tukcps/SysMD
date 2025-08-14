@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName

/**
 * Class with functions that add a comment.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 */
open class CommentActions<T: Comment>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T
): AnnotatingElementActions<T>(context, creator) {
    /**
     * Adds an annotation (=relationship) to the about-elements.
     * @param about List of elements related to the comment
     */
    fun addAbout(about: List<QualifiedName>) {
        about.forEach {
            val annotation = AnnotationImplementation(
                owningRelatedElement = created,
                annotatingElement = created,
                annotatedElement = UnresolvedElement( it)
            )
            context.model.addOwnedRelationship(annotation,  created)
        }
    }
}

/**
 * Class with functions that add a comment.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 */
class DocumentationActions(
    context: ActionsContext,
    var body: String = "",
): CommentActions<Documentation>(
    context = context,
    creator = ::DocumentationImplementation,
) {
    override fun create(identification: Identification?) {
        super.create(identification)
        created.body = body.trim()
    }
}

/**
 * Class with functions that add a textual representation.
 * @param context holds the core data needed by all semantic actions
 */
open class AnnotatingElementActions<T: AnnotatingElement>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
): SemanticAction<T>(context, creator)

/**
 * Class with functions that add a namespace.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 */
open class NamespaceActions<T: Namespace>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
) : SemanticAction<T>(context, creator) {

    override fun init() {
        context.pushOwningNamespace(this as NamespaceActions<Namespace>)
        super.init()
    }

    override fun finish() {
        super.finish()
        context.popOwningNamespace()
    }

    override fun create(identification: Identification?) {
        super.create(identification)
        created.isStandard = STANDARD in context.prefixes
        created.isLibraryElement = LIBRARY in context.prefixes
    }
}

interface RelationshipActions<T: Relationship> {
    var created: T
    val context: ActionsContext
}

/**
 * Actions for Membership
 */
open class MembershipActions<T: Membership>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T
): RelationshipActions<T>, SemanticAction<T>(context, creator)


/**
 * Class with functions that add an import.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 */
class ImportActions(
    context: ActionsContext,
    var all: Boolean? = null,
    var isRecursive: Boolean = false,
    // production: ImportActions.() -> Unit
): SemanticAction<Import>(context, ::NamespaceImportImplementation), RelationshipActions<Import> {

    override fun create(identification: Identification?) {
        super.create(identification)
        if (context.visibilityKind == null) {
            context.model.status.info("import must be explicit public or private", context.compiler)
        }
        setImportingNamespace(context.ownerName())
        context.model.addOwnedRelationship(created, context.element())
    }

    override fun finish() {}

    fun setImportingNamespace(namespace: String) {
        if (namespace.isEmpty())
            created.source = mutableListOf(context.model.global)
        else
            created.source = mutableListOf(UnresolvedNamespace(namespace))
    }

    fun setImportedNamespace(namespace: String) {
        created.target = mutableListOf(UnresolvedNamespace(namespace))
    }
}

/**
 * Action that creates a Specialization or kind thereof
 */
class RelationshipActionsImpl<T: Relationship>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T
): SemanticAction<T>(context, creator), RelationshipActions<T>


/**
 * Adds a dependency to the model
 */
class DependencyActions<T: Dependency>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T
): SemanticAction<Dependency>(context, creator), RelationshipActions<Dependency>