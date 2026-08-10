@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.datamodel.IdentificationKind
import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.util.QualifiedName

/**
 * Semantic action that builds an owned relationship and returns it.
 */
open class OwnedRelationshipAction(
    context: ActionsContext,
    type: ElementType,
): RelationshipAction(
    context = context,
    type = type
)

/**
 * Class with functions that add a namespace.
 * @param context holds the context information needed by all semantic actions
 * @param type type that is used to create the element
 */
open class NamespaceAction(
    context: ActionsContext,
    type: ElementType,
    owningMembershipType: ElementType? = ElementType.OwningMembership,
): ElementAction(context, type, owningMembershipType) {

    var visibility = Import.VisibilityKind.Public

    override fun beforeProduction() {
        super.beforeProduction()
        context.owningRelationship?.visibility = (context.visibility?:Import.VisibilityKind.Public)
    }
}

/**
 * Class with functions that add a dependency.
 * @param context holds the context information needed by all semantic actions
 */
class DependencyAction(
    context: ActionsContext
): ElementAction(
    context = context,
    type = ElementType.Dependency,
    owningMembershipType = ElementType.OwningMembership,
) {
    override fun afterProduction() {
        element.isStandard = STANDARD in context.prefixes
        if (LIBRARY in context.prefixes)
            element.type = ElementType.LibraryPackage
        super.afterProduction()
    }
}

/**
 * Class with functions that add a namespace.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 * @param context holds the core data needed by all semantic actions
 * @param type type that is used to create the element
 * @param owningMembership if null, an annotating membership will be inserted automatically.
 */
open class AnnotatingElementAction(
    context: ActionsContext,
    type: ElementType,
    owningMembership: ElementType = ElementType.OwningMembership,
): ElementAction(context, type, owningMembership)

/**
 * Class with functions that add an import.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 *
 * @param context holds the core data needed by all semantic actions
 */
class ImportAction(
    context: ActionsContext,
    type: ElementType = ElementType.NamespaceImport
): OwnedRelationshipAction(
    context = context,
    type = type             // Overwritten directly in semantic actions
) {
    var isImportAll: Boolean = false
    var isRecursive: Boolean = false
    var importQualifiedName: QualifiedName? = null

    override fun beforeProduction() {
        super.beforeProduction()
        element.visibility = context.visibility
        if (context.visibility == null) {
            context.status.info("import must be explicit 'public' or 'private'", element)
        }
    }

    override fun afterProduction() {
        element.type = type
        val ref = IdentifiedByName(importQualifiedName!!, when(element.type) {
            ElementType.NamespaceImport -> IdentificationKind.Namespace
            else -> IdentificationKind.Membership
        })

        element.target = mutableListOf(ref)
        super.afterProduction()
    }
}


// Graveyard
@Deprecated("...")
interface RelationshipActions<T: Relationship> {
    var created: T
    val context: ActionsContext
}

@Deprecated("No.")
class RelationshipActionsImpl<T: Relationship>(
    context: ActionsContext,
    creator: () -> T
): SemanticAction<T>(context, creator), RelationshipActions<T>

@Deprecated("No.")
open class NamespaceActions<T: Namespace>(
    context: ActionsContext,
    creator: () -> T,
) : SemanticAction<T>(context, creator)