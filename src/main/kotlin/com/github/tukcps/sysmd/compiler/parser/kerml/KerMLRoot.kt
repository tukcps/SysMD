@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.*

/**
 * An identification, following the conventions of SysML v2 textual:
 *
 *      Identification :- ( '<' NAME_LIT '>' )?  (NAME_LIT)?
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

/**
 * KerML, 8.3.2.1
 *
 *      RelationshipBody = ';' | '{' RelationshipOwnedElement* '}'
 */
fun KerML.RelationshipBody(owner: Resolved<Relationship>) {
    alternatives {
        SEMICOLON starts { SEMICOLON.consume() }
        LCURBRACE starts {
            semantics.owners.push(owner)
            LCURBRACE.consume()
            noOrMore(end = { token.kind == RCURBRACE }) {
                RelationshipOwnedElement()
            }
            RCURBRACE.consume()
            semantics.owners.pop()
        }
    }
}

/**
 * KerML, 8.3.2.1
 *
 *      RelationshipOwnedElement = OwnedRelatedElement | OwnedAnnotation
 */
fun KerML.RelationshipOwnedElement() {
    alternatives {
        COMMENT or DOC or REP or LANGUAGE starts { OwnedAnnotation() }
        others { OwnedRelatedElement() }
    }
}


/**
 *      Dependency = ( PrefixMetadataAnnotation )*
 *          'dependency' ( Identification?
 *          'from' )? [QualifiedName] ( ',' [QualifiedName] )*
 *          'to' [QualifiedName] ( ',' [QualifiedName] )*
 *          RelationshipBody
 */
fun KerML.Dependency() {
    val dependency = DependencyActions<Dependency>(semantics, ::DependencyImplementation)
    DEPENDENCY.consume()
    optional({ token.kind == NAME_LIT && nextToken.kind in setOf(FROM, LCBRACE) }) {
        Identification().also { dependency.create(it) }
        FROM.consume()
    }
    if (dependency.created == null) dependency.create(Identification(null, null))
    QualifiedNameList().also { dependency.addSource(it) }
    TO.consume()
    QualifiedNameList().also { dependency.addTarget(it) }
    RelationshipBody(Resolved(dependency.created!!))
}


/**
 *      AnnotatingElement = Comment
 *          | Documentation
 *          | TextualRepresentation
 *          | MetadataFeature
 */
fun KerML.OwnedAnnotation() {
    alternatives {
        COMMENT starts { Comment() }
        REGULAR_COMMENT starts { Comment() }
        DOC starts { Documentation() }
        REP starts { TextualRepresentation() }
        METADATA starts { MetadataFeature() }
    }
}

/**
 *      OwnedRelatedElement = NonFeatureElement | FeatureElement
 */
fun KerML.OwnedRelatedElement() {
    when(token.kind) {
        in featureElementStart    -> FeatureElement()
        in nonFeatureElementStart -> NonFeatureElement()
        else -> throwSyntaxError("At ${token.kind}: Expected a valid feature or non-feature element")
    }
}


/**
 *     MemberPrefix :- ( 'public' | "private" | "protected" ) "abstract"?
 */
fun KerML.MemberPrefix() {
    alternatives {
        PUBLIC    starts { PUBLIC.consume();    semantics.visibilityKind = PUBLIC }
        PRIVATE   starts { PRIVATE.consume();   semantics.visibilityKind = PRIVATE }
        PROTECTED starts { PROTECTED.consume(); semantics.visibilityKind = PROTECTED }
        others           {  }
    }
    ABSTRACT.optional    { semantics.prefixes.add(ABSTRACT) }
}

/**
 *    NamespaceBodyElement =
 *          NamespaceMember
 *          | AliasMember
 *          | Import
 *    MemberPrefix =  VisibilityIndicator?
 *    VisibilityIndicator : VisibilityKind = 'public' | 'private' | 'protected'
 *    NamespaceMember = NonFeatureMember | NamespaceFeatureMember
 */
fun KerML.NamespaceBodyElement() {
    MemberPrefix()      // Consume and remember them in context
    FeaturePrefix()     // Optional anyhow, we just consume and remember them in context
    alternatives {
        nonFeatureElementStart starts       { NonFeatureElement() }
        featureElementStart starts          { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
        SEMICOLON then                      { /* Empty statement */ }
        // Else, we have a SysMLv2 Statement
        others                              {
            throw SyntaxError(this@NamespaceBodyElement, "SysML v2 statement '$token'? -- use SysML v2 compiler!")
        }
    }
    semantics.prefixes.clear()
}

/**
 *      Comment =
 *          ( 'comment' Identification
 *              ( 'about' Annotation ( ',' Annotation )* )?
 *          )?
 *          ( 'locale' STRING_VALUE )?
 *          REGULAR_COMMENT
 */
internal fun KerML.Comment() {
    val comment = CommentActions(semantics, ::CommentImplementation)
    comment.create(Identification(null, null))
    COMMENT.optional {
        optional(NAME_LIT) {
            Identification().also { comment.setIdentification(it) }
        }
        ABOUT.optional {
            QualifiedNameList().also { comment.addAbout(it) }
        }
    }
    REGULAR_COMMENT.consume().also { comment.created!!.body = consumedToken.string.trimIndent().trim() }
}

/**
 *      Documentation =
 *          'doc' Identification
 *          ( 'locale' STRING_VALUE )?
 *          REGULAR_COMMENT
 */
internal fun KerML.Documentation() {
    val doc = DocumentationActions(semantics)
    DOC.consume()
    Identification().also { doc.create(it) }
    REGULAR_COMMENT.consume().also { doc.created?.body = consumedToken.string.trim() }
}

/**
 *      TextualRepresentation =
 *          ( 'rep' Identification )?
 *          'language' STRING_VALUE
 *          REGULAR_COMMENT
 */
internal fun KerML.TextualRepresentation() {
    val rep = AnnotatingElementActions(semantics, ::TextualRepresentationImplementation)
    rep.create(Identification(null, null))
    optional(REP) {
        REP.consume()
        Identification().also { rep.setIdentification(it) }
    }
    LANGUAGE.consume()
    NAME_LIT.consume().also { rep.created!!.language = consumedToken.string }
    REGULAR_COMMENT.consume().also { rep.created!!.body = consumedToken.string.trim(' ') }
}

/**
 *      Namespace :- "namespace" Identification Body
 */
internal fun KerML.Namespace() {
    val namespace = NamespaceActions(semantics, ::NamespaceImplementation)
    NAMESPACE.consume()
    Identification().also { namespace.create(it) }
    Body(Resolved(null, namespace.created, null))
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
 * ImportDeclaration RelationshipBody
 * ImportDeclaration : Import
 * MembershipImport | NamespaceImport
 *
 * MembershipImport = importedMembership = QualifiedName ( '::' isRecursive ?= '**' )?
 * NamespaceImport = importedNamespace = QualifiedName '::' '*' ( '::' isRecursive ?= '**' )? | importedNamespace = FilterPackage
 *
 * FilterPackage : Package =
 * ownedRelationship += ImportDeclaration
 * ( ownedRelationship += FilterPackageMember )+
 * FilterPackageMember : ElementFilterMembership =
 * '[' ownedRelatedElement += OwnedExpression ']'
 * { visibility = 'private' }
 */

/**
 *
 *      Import = ( VisibilityIndicator )?
 *              'import' 'all'? ImportDeclaration RelationshipBody
 *
 *      ImportDeclaration = MembershipImport | NamespaceImport
 *
 *      MembershipImport = [QualifiedName] ( '::' '**'? )?
 *
 *      NamespaceImport =  [QualifiedName] '::' '*' ( '::' '**'? )? | FilterPackage
 *
 *      Import = "import" ["all"] [Identification ":"] QualifiedName "::" ("*"| "**") Body
 */
internal fun KerML.Import() {
    val import = ImportActions(semantics)
    IMPORT.consume().also   { import.create(Identification(null, null)) }
    ALL.optional            { import.all = true }

    QualifiedName().also    { import.setImportedNamespace(it) }
    optional(DPDP, consume = true) {
        alternatives {
            STARSTAR starts { import.isRecursive = true; STARSTAR.consume() }
            TIMES starts { TIMES.consume() }
        }
    }
    Body(Resolved(import.created!!))
}

/**
 *      AliasMember  = MemberPrefix
 *          'alias' ( '<' memberShortName = NAME '>' )?
 *          ( memberName = NAME )?
 *          'for' memberElement = [QualifiedName]
 *          RelationshipBody
 */
internal fun KerML.AliasMember() {
    val aliasMember = MembershipActions(semantics, ::MembershipImplementation)
    ALIAS.consume()
    Identification().also { aliasMember.create(it) }
    FOR.consume()
    optional(NAME_LIT) {
        QualifiedName().also { aliasMember.addTarget(listOf(it)) }
    }
    RelationshipBody(Resolved(aliasMember.created!!))
}

/**
 *      ElementList :- Element+
 *       Deprecated (SysMD legacy): Also an Element ending with a dot shall stop the list
 */
fun KerML.ElementList() {
    oneOrMore(stop = { consumedToken.kind == DOT || token.kind == RCURBRACE || token.kind == EOF }) {
        NamespaceBodyElement()
    }
}

/**
 * Parses a qualified name.
 *
 *      QualifiedName :- "NAME_LIT ("::" NAME_LIT)*
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
 *      FeatureElement:
 *          Feature | Step | Expression | BooleanExpression | Invariant
 *          | Connector | BindingConnector | Succession | ItemFlow
 *          | SuccessionItemFlow
 */
val featureElementStart = setOf(
    FEATURE, STEP, EXPR, INV, CONNECTOR, SUCCESSION, SUCCESSION, REDEFINES  // Prefixes that start a feature kind production rule
) + FEATURE_PREFIX_START

fun KerML.FeatureElement() {
    FeaturePrefix()
    alternatives {
        REDEFINES starts { Feature() }
        FEATURE starts { Feature() }
        STEP    starts { Step() }
        EXPR    starts { Feature() } // Dirty, needs work!
        // Boolean expression is handled as an Expression
        INV     starts { Invariant() }
        CONNECTOR starts { Connector() }
        // BindingConnector is handled as Connector
        SUCCESSION starts { Succession() }
        others { Feature() }
    }
}

/**
 *      AnnotatingElement = Comment | Documentation | TextualRepresentation | MetadataFeature
 */
val annotatingElementStart = setOf(COMMENT, REGULAR_COMMENT, DOC, REP, METADATA, LANGUAGE)
fun KerML.AnnotatingElement() {
    alternatives {
        COMMENT or REGULAR_COMMENT  starts { Comment() }
        DOC                         starts { Documentation() }
        LANGUAGE or REP             starts { TextualRepresentation()}
        METADATA                    starts { MetadataFeature() }
    }
}

/**
 *      NonFeatureMember = MemberPrefix MemberElement
 *      MemberElement = AnnotatingElement | NonFeatureElement
 *      NonFeatureElement = Dependency | Namespace
 *          | Type | Classifier | DataType | Class
 *          | Structure | Metaclass | Association | AssociationStructure
 *          | Interaction | Behavior | Function | Predicate | Multiplicity
 *          | Package | LibraryPackage | Specialization | Conjugation
 *          | Subclassification | Disjoining | FeatureInverting | FeatureTyping
 *          | Subsetting | Redefinition | TypeFeaturing
 */

fun KerML.NonFeatureElement() {
    alternatives {
        annotatingElementStart starts { AnnotatingElement() }
        DEPENDENCY  starts { Dependency() }
        NAMESPACE   starts { Namespace() }
        TYPE        starts { Type() }
        CLASSIFIER  starts { Classifier() }
        DATATYPE    starts { Datatype() }
        CLASS       starts { Class() }
        STRUCT      starts { Structure() }
        METACLASS   starts { Metaclass() }
        ASSOC       starts { Association() }
        ASSOC then STRUCT starts { AssociationStructure() }
        INTERACTION starts { Interaction() }
        BEHAVIOR    starts { Behavior() }
        FUNCTION    starts { Function() }
        PREDICATE   starts { Predicate() }

        // Multiplicity???
        PACKAGE     starts { Package() }
        LIBRARY     starts { LibraryPackage() }
        STANDARD    starts { LibraryPackage() }
        specializationStart starts { Specialization() }
        CONJUGATION_START starts { Conjugation()}
        DISJOINING_START starts { Disjoining() }

        //
        // Following is more syntactic sugar as there are more straightforward ways to
        // model different kind of specializations. Functionality is in Feature/Type anyhow.
        //
        // | Subclassification
        // | FeatureInverting
        // | FeatureTyping
        // | Subsetting
        // | Redefinition
        // | TypeFeaturing
    }
}