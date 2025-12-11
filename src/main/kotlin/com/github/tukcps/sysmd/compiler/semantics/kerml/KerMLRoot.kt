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
    var isImportAll: Boolean = false,
    var isRecursive: Boolean = false,
    var importQualifiedName: QualifiedName? = null,
): SemanticAction<Import>(context, ::NamespaceImportImplementation), RelationshipActions<Import> {

    fun createNamespaceImport() {
        if (context.visibility == null) {
            context.model.status.info("import must be explicit public or private", context.compiler)
        }
        created = NamespaceImportImplementation()
        created.isRecursive = isRecursive
        created.isImportAll = isImportAll
        setImportedNamespace()
        setImportingNamespace(context.element())
        super.create(null)
    }

    fun createMembershipImport() {
        if (context.visibility == null) {
            context.model.status.info("import must be explicit public or private", context.compiler)
        }
        created = MembershipImportImplementation()
        created.isRecursive = isRecursive
        created.isImportAll = isImportAll
        setImportedMember()
        setImportingNamespace(context.element())
        super.create(null)
    }

    fun parseImport(production: ImportActions.() -> Unit){
        production()
    }

    override fun finish() {}

    fun setImportingNamespace(owningNamespace: Namespace) {
       created.source = mutableListOf(owningNamespace)
    }

    fun setImportedNamespace() {
        created.target = mutableListOf(UnresolvedNamespace(importQualifiedName))
    }

    fun setImportedMember() {
        created.target = mutableListOf(UnresolvedMembership(importQualifiedName))
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