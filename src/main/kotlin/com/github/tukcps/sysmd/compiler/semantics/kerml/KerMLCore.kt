@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.END
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.SimpleName

/**
 * Semantic action for the declaration of a Type.
 * @param context object with the overall semantic action's context of the parser
 * @param isImplicit The qualified name of the Type's superclass
 */
open class TypeActions<T: Type>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    var isImplicit: String? = "Base::Anything",
): NamespaceActions<T>(context, creator) {

    override fun finish() {
        if (created.specialization.isEmpty() && isImplicit != null) {
            context.addSpecialization(isImplicit!!)
        }
        if (Token.Kind.ABSTRACT in context.prefixes) created.isAbstract = true
        super.finish()
    }
}

open class ClassifierActions<T: Classifier>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    isImplicit: String = "Base::Anything",
): TypeActions<T>(context, creator, isImplicit)

/**
 * Semantic action for the declaration of a Class.
 * @param context object with the semantic actions of the parser
 * @param creator function that creates a Class element
 * @param isImplicit class that is general for non-abstract classes; also default for missing general class
 */
open class ClassActions<T: Class>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    isImplicit: String = "Occurrences::Occurrence",
): ClassifierActions<Class>(context, creator, isImplicit) {
    override fun finish() {
        if (created.specialization.isEmpty() && isImplicit != null) {
            context.addSubclassification(isImplicit!!)
        }
        super.finish()
    }
}

/**
 * Semantic action for the declaration of a DataType.
 * @param context object with the semantic actions of the parser
 * @param creator Function to build a DataType
 * @param specializes Default specialization if user gives none
 */
open class DataTypeActions<T: DataType>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: String = "Base::DataValue",
): ClassifierActions<T>( context, creator, specializes)


/**
 * Semantic action for the declaration of a Structure.
 * @param context object with the semantic actions of the parser
 */
open class StructureActions<T: Structure>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: String = "Objects::Object",
): ClassifierActions<T>(
    context, creator, specializes
)


/**
 * Semantic actions of a Feature
 * @param context object with the semantic context of the parse run
 * @param defaultType supertypes to be added as default
 * @param valuePart the AST as parsed
 */
open class FeatureActions<T: Feature>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    var valuePart: AstRoot? = null,
): TypeActions<T>(context, creator, defaultType) {

    override fun create(identification: Identification?) {
        created.direction = context.directionFromPrefixes()
        super.create(identification)
        created.isEnd = END in context.prefixes
        created.isComposite = Token.Kind.COMPOSITE in context.prefixes
        created.isPortion = Token.Kind.PORTION in context.prefixes
        created.isUnique = Token.Kind.UNIQUE in context.prefixes
        created.isOrdered = Token.Kind.ORDERED in context.prefixes
        created.isReadOnly = Token.Kind.READONLY in context.prefixes
        created.isAbstract = Token.Kind.ABSTRACT in context.prefixes
        created.isDerived = Token.Kind.DERIVED in context.prefixes
        context.namespace = created
    }

    override fun finish() {
        if (created.redefining == null) {
            if (created.specialization.isEmpty() && isImplicit != null) {
                context.addTyping(isImplicit!!)
            }
        } else isImplicit = null
        super.finish()
    }
}
