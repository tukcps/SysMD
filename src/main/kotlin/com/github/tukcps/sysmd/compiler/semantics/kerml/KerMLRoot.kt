package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
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
    context: SemanticActions,
    creator: (SimpleName?, SimpleName?) -> T
): AnnotatingElementActions<T>(context, creator) {
    /**
     * Adds an annotation (=relationship) to the about-elements.
     * @param about List of elements related to the comment
     */
    fun addAbout(about: List<QualifiedName>) {
        about.forEach {
            val annotation = AnnotationImplementation(
                annotatingElement = Resolved(ref = created!!),
                annotatedElement = Resolved(str = it)
            )
            context.model.addUnownedElement(annotation, startOfOwnerPath =  created!!)
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
    context: SemanticActions,
    var body: String = "",
): CommentActions<Documentation>(
    context = context,
    creator = ::DocumentationImplementation,
) {
    override fun create(identification: Identification) {
        super.create(identification)
        created?.body = body.trim()
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
) : SemanticAction<Namespace>(context, creator) {
    override fun create(identification: Identification) {
        super.create(identification)
        created!!.isStandard = STANDARD in context.prefixes
        created!!.isLibraryElement = LIBRARY in context.prefixes
    }
}

interface RelationshipActions<T: Relationship> {
    var created: T?
    val context: ActionsContext

    fun addSource(source: List<QualifiedName>) {
        created?.source = source.toIdentityList()
    }

    fun addTarget(target: List<QualifiedName>) {
        created?.target = target.toIdentityList()
    }

    fun setSource(source: List<QualifiedName>) {
        created?.source = source.toIdentityList()
    }

    fun setTarget(target: List<QualifiedName>) {
        created?.target = target.toIdentityList()
    }
}

/**
 * Actions for Membership
 */
open class MembershipActions<T: Membership>(
    context: SemanticActions,
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
    context: SemanticActions,
    var all: Boolean? = null,
    var isRecursive: Boolean = false,
): SemanticAction<Import>(context, ::NamespaceImportImplementation), RelationshipActions<Import> {

    override fun create(identification: Identification) {
        super.create(identification)
        if (context.visibilityKind == null) {
            context.model.status.info("import must be either explicit public or private", context.compiler)
        }
        setImportingNamespace(context.ownerName())
    }

    fun setImportingNamespace(namespace: String) {
        if (namespace.isEmpty())
            setSource(mutableListOf("Global"))
        else
            setSource(mutableListOf(namespace))
    }

    fun setImportedNamespace(namespace: String) {
        setTarget(mutableListOf(namespace))
    }
}

/**
 * Action that creates a Specialization or kind thereof
 */
class RelationshipActionsImpl<T: Relationship>(
    context: SemanticActions,
    creator: (SimpleName?, SimpleName?) -> T
): SemanticAction<T>(context, creator), RelationshipActions<T>


/**
 * Adds a dependency to the model
 */
class DependencyActions<T: Dependency>(
    context: SemanticActions,
    creator: (SimpleName?, SimpleName?) -> T
): SemanticAction<Dependency>(context, creator), RelationshipActions<Dependency>