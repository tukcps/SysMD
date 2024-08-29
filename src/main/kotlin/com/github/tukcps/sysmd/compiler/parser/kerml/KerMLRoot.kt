@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.sysmlv2.AttributeUsage
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.services.report

/**
 * An identification, following the conventions of SysML v2 textual:
 *   Identification :- ('<' NAME_LIT '>')?  (NAME_LIT)?
 */
fun KerML.Identification(): Identification {
    val identification = Identification(null, null)
    optional(start = LT) {
        LT.consume()
        NAME_LIT.consume().also { identification.shortName = consumedToken.string }
        GT.consume()
    }
    optional(start = NAME_LIT) {
        NAME_LIT.consume().also { identification.name = consumedToken.string }
    }
    return identification
}


fun KerML.RelationshipBody() {
    alternatives {
        SEMICOLON starts { SEMICOLON.consume() }
        LCURBRACE starts {
            LCURBRACE.consume()
            noOrMore(end = { token.kind == RCURBRACE }) {
                RelationshipOwnedElement()
            }
            RCURBRACE.consume()
        }
    }
}

/**
 * RelationshipOwnedElement : Relationship =
 * ownedRelatedElement += OwnedRelatedElement
 * | ownedRelationship += OwnedAnnotation
 */
fun KerML.RelationshipOwnedElement() {
    alternatives {
        COMMENT or DOC or REP starts { OwnedAnnotation() }
        others { OwnedRelatedElement() }
    }
}


/**
 * Dependency =
 * ( ownedRelationship += PrefixMetadataAnnotation )*
 * 'dependency' ( Identification? 'from' )?
 * client += [QualifiedName] ( ',' client += [QualifiedName] )* 'to'
 * supplier += [QualifiedName] ( ',' supplier += [QualifiedName] )*
 * RelationshipBody
 */
fun KerML.Dependency() {
    val dep = semantics.dependencyActions()
    DEPENDENCY.consume()

    optional ({token.kind == NAME_LIT && nextToken.kind in setOf(FROM, LCBRACE)}) {
        Identification().also { dep?.identification = it }
        FROM.consume()
    }
    QualifiedNameList().also { dep?.source = it }
    TO.consume()
    QualifiedNameList().also { dep?.target = it }
    dep?.create()
    RelationshipBody()
}


/**
 * Annotation =
 *      annotatedElement = [QualifiedName]
 * OwnedAnnotation : Annotation =
 *      annotatingElement = AnnotatingElement { ownedRelatedElement += annotatingElement }
 * AnnotatingElement =
 *      Comment
 *      | Documentation
 *      | TextualRepresentation
 *      | MetadataFeature
 */
fun KerML.OwnedAnnotation() {
    TODO()
}

fun KerML.OwnedRelatedElement() {
    NamespaceBodyElement()
}

/**
 * Comment =
 * ( 'comment' Identification
 *      ( 'about' annotation += Annotation
 *          { ownedRelationship += annotation }
 *          ( ',' annotation += Annotation
 *              { ownedRelationship += annotation } )*
 *      )?
 * )?
 * ( 'locale' locale = STRING_VALUE )?
 * body = REGULAR_COMMENT
 */
internal fun KerML.Comment() {
    val comment = semantics.commentActions()
    optional(COMMENT) {
        COMMENT.consume()
        optional(NAME_LIT) {
            Identification().also { comment?.identification = it }
        }
        optional(ABOUT) {
            ABOUT.consume()
            QualifiedNameList().also { comment?.about = it }
        }
    }
    REGULAR_COMMENT.consume().also { comment?.body = consumedToken.string.trimIndent() }
    comment?.create()
}

/**
 * Documentation =
 * 'doc' Identification
 * ( 'locale' locale = STRING_VALUE )?
 * body = REGULAR_COMMENT
 */
internal fun KerML.Documentation() {
    val doc = semantics.documentationActions()
    DOC.consume()
    Identification().also { doc?.identification = it }
    REGULAR_COMMENT.consume().also { doc?.body = consumedToken.string.trimIndent() }
    doc?.create()
}

/**
 * TextualRepresentation =
 * ( 'rep' Identification )?
 * 'language' language = STRING_VALUE
 * body = REGULAR_COMMENT
 */
internal fun KerML.TextualRepresentation() {
    val rep = semantics.textualRepresentationActions()
    REP.consume()
    optional(NAME_LIT) {
        Identification().also { rep?.identification = it }
    }
    optional(LANGUAGE) {
        LANGUAGE.consume()
        NAME_LIT.consume().also { rep?.language = consumedToken.string }
    }
    REGULAR_COMMENT.consume().also { rep?.body = consumedToken.string.trimIndent() }
    rep?.create()
}

/**
 * Namespace :- "namespace" Identification Body
 */
internal fun KerML.Namespace() {
    val namespace = semantics.namespaceActions()
    NAMESPACE.consume()
    Identification().also { namespace?.identification = it }
    namespace?.create()
    Body(Resolved(null, namespace?.created, null))
}

/**
 * Namespace =
 * ( ownedRelationship += PrefixMetadataMember )*
 * NamespaceDeclaration NamespaceBody
 *
 * NamespaceDeclaration : Namespace = 'namespace' Identification
 *
 * NamespaceBody : Namespace = ';' | '{' NamespaceBodyElement* '}'
 *
 * NamespaceBodyElement : Namespace =
 *      ownedRelationship += NamespaceMember
 *      | ownedRelationship += AliasMember
 *      | ownedRelationship += Import
 *
 * MemberPrefix : Membership =
 *      ( visibility = VisibilityIndicator )?
 * VisibilityIndicator : VisibilityKind = 'public' | 'private' | 'protected'
 * NamespaceMember : OwningMembership = NonFeatureMember | NamespaceFeatureMember
 * NonFeatureMember : OwningMembership =
 * MemberPrefix
 * ownedRelatedElement += MemberElement
 * NamespaceFeatureMember : OwningMembership =
 * MemberPrefix
 * ownedRelatedElement += FeatureElement
 */


/**
 * Body :-
 *            "{" ElementList "}"
 *          | ";"
 *          | "."  // Only for SysMD to end Triple
 */
internal fun KerML.Body(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            ElementList()
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
        DOT then { } // Iff Triple
    }
}


/**
 * Import =
 * ( visibility = VisibilityIndicator )?
 * 'import' ( isImportAll ?= 'all' )?
 * ImportDeclaration RelationshipBody
 * ImportDeclaration : Import
 * MembershipImport | NamespaceImport
 * MembershipImport =
 * importedMembership = [QualifiedName]
 * ( '::' isRecursive ?= '**' )?
 * NamespaceImport =
 * importedNamespace = [QualifiedName] '::' '*'
 * ( '::' isRecursive ?= '**' )?
 * | importedNamespace = FilterPackage
 * { ownedRelatedElement += importedNamespace }
 * FilterPackage : Package =
 * ownedRelationship += ImportDeclaration
 * ( ownedRelationship += FilterPackageMember )+
 * FilterPackageMember : ElementFilterMembership =
 * '[' ownedRelatedElement += OwnedExpression ']'
 * { visibility = 'private' }
 */

/**
 * Import :-
 *  "import" ["all"] [Identification ":"] QualifiedName "::" ("*"| "**") Body
 */
internal fun KerML.Import() {
    val import = semantics.importActions()
    IMPORT.consume()
    optional(ALL, consume = true).also { import?.all = true }
    if (token.kind == NAME_LIT && nextToken.kind == DP || token.kind == LT)
        Identification().also { import?.identification = it }

    QualifiedName().also { import?.namespace = it }
    optional(DPDP, consume = true) {
        alternatives {
            STARSTAR starts { import?.isRecursive = true; STARSTAR.consume() }
            TIMES starts { TIMES.consume() }
        }
    }
    import?.create()
    Body(Resolved(import?.created!!))
}

/**
 * AliasMember : Membership =
 * MemberPrefix
 * 'alias' ( '<' memberShortName = NAME '>' )?
 * ( memberName = NAME )?
 * 'for' memberElement = [QualifiedName]
 * RelationshipBody
 */
internal fun KerML.AliasMember() {
    model.report("Not yet supported: alias membership")
    ALIAS.consume()
    Identification()
    FOR.consume()
    optional(NAME_LIT) {
        QualifiedName()
    }
    RelationshipBody()
}


/**
 *   ElementList :- Element+
 *   NOTE: Also an Element ending with a dot shall stop the list
 */
fun KerML.ElementList() {
    oneOrMore(stop = { consumedToken.kind == DOT || token.kind == RCURBRACE || token.kind == EOF }) {
        token = token.considerMetaKeywords()
        NamespaceBodyElement()
    }
}


/**
 * Parses a qualified name.
 * QualifiedName :- "NAME_LIT ("::" NAME_LIT)*
 */
fun KerML.QualifiedName(): String {
    var qualifiedName: String
    NAME_LIT.consume().also { qualifiedName = consumedToken.string }
    noOrMore({ token.kind == DPDP && nextToken.kind == NAME_LIT }) {
        DPDP.consume().also { qualifiedName += "::" }
        NAME_LIT.consume().also { qualifiedName += consumedToken.string }
    }
    return qualifiedName
}


/**
 * FeatureElement:
 *       Feature
 *     | Step
 *     | Expression
 *     | BooleanExpression
 *     | Invariant
 *     | Connector
 *     | BindingConnector
 *     | Succession
 *     | ItemFlow
 *     | SuccessionItemFlow
 */
val FEATURE_ELEMENT_TOKENS = setOf(
    FEATURE, STEP, EXPR, INV, CONNECTOR, SUCCESSION, SUCCESSION,
    END, IN, OUT, INOUT, RETURN, PORTION, COMPOSITE // Prefixes that start a feature kind production rule
)

fun KerML.FeatureElement(): Boolean {
    var noKerML = false
    // Handle Feature prefixes
    FeaturePrefixes()
    alternatives {
        FEATURE starts { Feature() }
        STEP starts { TODO() }
        EXPR starts { Feature() } // Dirty, needs work!
        // Boolean expression is handled as an Expression
        INV starts { Invariant() }
        CONNECTOR starts { Connector() }
        // BindingConnector is handled as Connector
        SUCCESSION starts { TODO() }
        others { noKerML = true }
    }
    return noKerML
}

fun KerML.Classifier() {
    TODO("Not yet implemented")
}

/**
 * NonFeatureElement : Element =
 *       Dependency
 *     | Namespace
 *     | Type
 *     | Classifier
 *     | DataType
 *     | Class
 *     | Structure
 *     | Metaclass
 *     | Association
 *     | AssociationStructure
 *     | Interaction
 *     | Behavior
 *     | Function
 *     | Predicate
 *     | Multiplicity
 *     | Package
 *     | LibraryPackage
 *     | Specialization
 *     | Conjugation
 *     | Subclassification
 *     | Disjoining
 *     | FeatureInverting
 *     | FeatureTyping
 *     | Subsetting
 *     | Redefinition
 *     | TypeFeaturing
 *
 */
val NON_FEATURE_ELEMENT_TOKENS = setOf(
    DEPENDENCY, NAMESPACE, TYPE, CLASSIFIER, DATATYPE, CLASS, STRUCTURE, METACLASS, ASSOC,
    INTERACTION, BEHAVIOR, FUNCTION, PREDICATE, PACKAGE, LIBRARY, STANDARD, SPECIALIZATION
)

fun KerML.NonFeatureElement() {
    alternatives {
        DEPENDENCY  starts { Dependency() }
        NAMESPACE   starts { Namespace() }
        TYPE        starts { Type() }
        CLASSIFIER  starts { Classifier() }
        DATATYPE    starts { Datatype() }
        CLASS       starts { Class() }
        STRUCTURE   starts { TODO() }
        METACLASS   starts { TODO() }
        ASSOC       starts { Association() }
        ASSOC then STRUCT starts { TODO() }
        INTERACTION starts { TODO() }
        BEHAVIOR    starts { TODO() }
        FUNCTION    starts { Function() }
        PREDICATE   starts { TODO() }

        // Multiplicity???
        PACKAGE     starts { Package() }
        LIBRARY     starts { LibraryPackage() }
        STANDARD    starts { LibraryPackage() }
        SPECIALIZATION starts { TODO() }

        //
        // Following are more syntactic sugar as there are more straightforward ways to
        // model different kind of specializations.
        //
        // | Conjugation
        // | Subclassification
        // | Disjoining
        // | FeatureInverting
        // | FeatureTyping
        // | Subsetting
        // | Redefinition
        // | TypeFeaturing
    }
}