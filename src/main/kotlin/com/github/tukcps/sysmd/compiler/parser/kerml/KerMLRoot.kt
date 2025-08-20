@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.implementation.*

/**
 * An identification, following the conventions of SysML v2 textual:
 *
 *      Identification :- ('<' NAME_LIT '>')?  (NAME_LIT)?
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
        DOT then { /* only for SysMD to end Triple */ }
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
 *          'from' )? [QualifiedName] (',' [QualifiedName] )*
 *          'to' [QualifiedName] (',' [QualifiedName] )*
 *          RelationshipBody
 */
fun KerML.Dependency() {
    val dependency = DependencyActions<Dependency>(semantics, ::DependencyImplementation)
    DEPENDENCY.consume()
    if (token.kind == NAME_LIT && nextToken.kind in setOf(FROM, LCBRACE)) {
        Identification().also { dependency.create(it) }
        FROM.consume()
    } else
        dependency.create(Identification(null, null))
    QualifiedNameList().forEach { dependency.created.source.add(unresolvedElement(it)) }
    TO.consume()
    QualifiedNameList().forEach { dependency.created.target.add(unresolvedElement(it)) }
    RelationshipBody()
}


/**
 *      AnnotatingElement = Comment
 *          | Documentation
 *          | TextualRepresentation
 *          | MetadataFeature
 */
fun KerML.OwnedAnnotation() =
    alternatives {
        COMMENT starts { Comment() }
        REGULAR_COMMENT starts { Comment() }
        DOC starts { Documentation() }
        REP starts { TextualRepresentation() }
        METADATA starts { MetadataFeature() }
    }


/**
 *      OwnedRelatedElement = NonFeatureElement | FeatureElement
 */
fun KerML.OwnedRelatedElement() {
    when(token.kind) {
        in featureElementStart    -> FeatureElement()
        in nonFeatureElementStart -> NonFeatureElement()
        else -> handleSyntaxError("At ${token.kind}: Expected a feature or non-feature element")
    }
}


/**
 *     MemberPrefix :- ( 'public' | "private" | "protected" ) "abstract"?
 */
fun KerML.MemberPrefix() {
    alternatives {
        PUBLIC    starts { PUBLIC.consume();    semantics.visibility = Import.VisibilityKind.Public }
        PRIVATE   starts { PRIVATE.consume();   semantics.visibility = Import.VisibilityKind.Private }
        PROTECTED starts { PROTECTED.consume(); semantics.visibility = Import.VisibilityKind.Protected }
        others           {  }
    }
    ABSTRACT.optional    { semantics.prefixes.add(ABSTRACT) }
    // INDIVIDUAL.optional  { semantics.prefixes.add(INDIVIDUAL) }
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
    optional(HASHTAG) { PrefixMetadataMember() }
    alternatives {
        nonFeatureElementStart starts       { NonFeatureElement() }
        featureElementStart starts          { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
        SEMICOLON then                      { /* Empty statement */ }
        // Else, we have a SysMLv2 Statement
        others                              {
            if (token.string in Token.sysMLv2Keywords.keys
                && token.string !in Token.kerMLKeywords.keys)
                handleSyntaxError("SysML v2 statement at '$token'? -- use SysML v2 compiler!")
            else
                handleSyntaxError("At ${token.kind}: Expected a valid namespace body element (kind of features, non-features, alias, import)")
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
internal fun KerML.Comment() = CommentActions(semantics, ::CommentImplementation).parse {
    create()
    COMMENT.optional {
        optional(NAME_LIT) {
            Identification().also { setIdentification(it) }
        }
        ABOUT.optional {
            QualifiedNameList().also { (this as CommentActions).addAbout(it) }
        }
    }
    REGULAR_COMMENT.consume().also { created.body = consumedToken.string.trimIndent().trim() }
}

/**
 *      Documentation =
 *          'doc' Identification
 *          ( 'locale' STRING_VALUE )?
 *          REGULAR_COMMENT
 */
internal fun KerML.Documentation() = DocumentationActions(semantics).parse {
    DOC.consume()
    Identification().also { create(it) }
    REGULAR_COMMENT.consume().also { created.body = consumedToken.string.trim() }
}

/**
 *      TextualRepresentation =
 *          ( 'rep' Identification )?
 *          'language' STRING_VALUE
 *          REGULAR_COMMENT
 */
internal fun KerML.TextualRepresentation() = AnnotatingElementActions(semantics, ::TextualRepresentationImplementation).parse {
    create()
    optional(REP) {
        REP.consume()
        Identification().also { setIdentification(it) }
    }
    LANGUAGE.consume()
    NAME_LIT.consume().also { created.language = consumedToken.string }
    REGULAR_COMMENT.consume().also { created.body = consumedToken.string.trim(' ') }
}

/**
 *      Namespace =
 *          ( PrefixMetadataMember )*
 *          NamespaceDeclaration NamespaceBody
 *
 *      NamespaceDeclaration : Namespace = 'namespace' Identification
 */
internal fun KerML.Namespace() = NamespaceActions(semantics, ::NamespaceImplementation).parse {
    NAMESPACE.consume()
    Identification().also { semantics.create(it) }
    NamespaceBody()
}


/**
 *
 * NamespaceBody : Namespace = ';' | '{' NamespaceBodyElement* '}'
 */
internal fun KerML.NamespaceBody() {
    alternatives {
        SEMICOLON starts { SEMICOLON.consume() }
        LCURBRACE starts {
            LCURBRACE.consume()
            noOrMore(end = { token.kind == RCURBRACE }) {
                NamespaceBodyElement()
            }
            RCURBRACE.consume()
        }
    }
}

/**
 *
 *      Import = ( VisibilityIndicator )?
 *              'import' 'all'? ImportDeclaration RelationshipBody
 */
internal fun KerML.Import() = ImportActions(semantics).parseImport {
    IMPORT.consume()
    ALL.optional            { isImportAll = true }
    ImportDeclaration(this)
    RelationshipBody()
}

/**
 *      ImportDeclaration = MembershipImport | NamespaceImport
 */
internal fun KerML.ImportDeclaration(actions: ImportActions) {
    QualifiedName().also { actions.importQualifiedName = it }
    alternatives {
        DPDP then TIMES  starts   { NamespaceImport(actions) }
        others                    { MembershipImport(actions) }
    }
}

/**
 *      MembershipImport = [QualifiedName] ('::' '**'?)?
 */
internal fun KerML.MembershipImport(actions: ImportActions) {
    DPDP.optional {
        STARSTAR.optional().also { actions.isRecursive = true }
    }
    actions.createMembershipImport()
}

/**
 *      NamespaceImport =  [QualifiedName] '::' '*' ('::' '**'?)?
 *                         | FilterPackage
 *
 *      FilterPackage  = ImportDeclaration ( FilterPackageMember )+
 *
 *      FilterPackageMember = '[' OwnedExpression ']'
 */
internal fun KerML.NamespaceImport(actions: ImportActions) {
    DPDP.consume()
    TIMES.optional {
        DPDP.optional {
            actions.isRecursive = true
            STARSTAR.consume()
        }
    }
    actions.createNamespaceImport()
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
    Identification().also {
        aliasMember.create(it)
        aliasMember.created.membershipOwningNamespace = semantics.element()
    }
    FOR.consume()
    QualifiedName().also { aliasMember.created.target = mutableListOf(unresolvedElement(it)) }
    RelationshipBody()
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
        EXPR    starts { ExpressionFeature() }
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
    noOrMore(start = HASHTAG) { PrefixMetadataMember() }
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