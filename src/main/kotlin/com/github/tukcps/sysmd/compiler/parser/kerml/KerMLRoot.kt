@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.*
import com.github.tukcps.sysmd.model.datamodel.IdentificationKind
import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.datamodel.elementByName
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.util.QualifiedName

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
fun KerML.Dependency() = DependencyAction(semantics).parse {
    DEPENDENCY.consume()
    if (token.kind == NAME_LIT && nextToken.kind in setOf(FROM, LCBRACE)) {
        Identification().semantics { setIdentification(it) }
        FROM.consume()
    }
    QualifiedNameList().forEach { name -> element.source = name.map {
        elementByName(name)}.toMutableList()
    }
    TO.consume()
    QualifiedNameList().forEach { name -> element.target = name.map { elementByName(name)}.toMutableList() }
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
        setOf(METADATA, ATSIGN) starts { MetadataFeature() }
    }

/**
 *      OwnedRelatedElement = NonFeatureElement | FeatureElement
 */
fun KerML.OwnedRelatedElement() =
    when(token.kind) {
        in featureElementStart    -> FeatureElement()
        in nonFeatureElementStart -> NonFeatureElement()
        else -> handleSyntaxError("At ${token.kind}: Expected a feature or non-feature element")
    }

/**
 *     MemberPrefix :- ( 'public' | "private" | "protected" ) "abstract"?
 */
fun KerML.MemberPrefix() {
    when(token.kind) {
        PUBLIC    -> { PUBLIC.consume   { semantics.visibility = Import.VisibilityKind.Public} }
        PRIVATE   -> { PRIVATE.consume  { semantics.visibility = Import.VisibilityKind.Private} }
        PROTECTED -> { PROTECTED.consume {semantics.visibility = Import.VisibilityKind.Protected} }
        else      -> { semantics.visibility = Import.VisibilityKind.Public }
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
    optional(HASHTAG) { PrefixMetadataMember() }
    alternatives {
        nonFeatureElementStart starts       { NonFeatureElement() }
        featureElementStart starts          { FeatureElement() }
        ALIAS starts                        { AliasMember() }
        IMPORT starts                       { Import() }
        SEMICOLON then                      { /* Empty statement */ }
        // Else, we maybe have a SysML v2 Statement
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
internal fun KerML.Comment() = AnnotatingElementAction(semantics, ElementType.Comment).parse {
    COMMENT.optional {
        optional(NAME_LIT) {
            Identification().semantics  { setIdentification(it) }
        }
        ABOUT.optional {
            Annotation()
            noOrMore(COMMA){
                COMMA.consume()
                Annotation()
            }
        }
    }
    REGULAR_COMMENT.consume             { element.body = consumedToken.string.trimIndent().trim() }
}

/**
 *      Annotation := QualifiedName
 */
internal fun KerML.Annotation() = OwnedRelationshipAction(semantics, ElementType.Annotation).parse {
    QualifiedName().semantics { addTarget(elementByName(it)) }
}

/**
 *      Documentation =
 *          'doc' Identification
 *          ( 'locale' STRING_VALUE )?
 *          REGULAR_COMMENT
 */
internal fun KerML.Documentation() = AnnotatingElementAction(semantics, ElementType.Documentation).parse {
    DOC.consume()
    Identification().also { setIdentification(it) }
    REGULAR_COMMENT.consume().also { element.body = consumedToken.string.trim() }
}

/**
 *      TextualRepresentation =
 *          ( 'rep' Identification )?
 *          'language' STRING_VALUE
 *          REGULAR_COMMENT
 */
internal fun KerML.TextualRepresentation() = AnnotatingElementAction(semantics, ElementType.TextualRepresentation).parse {
    optional(REP) {
        REP.consume()
        Identification().also { setIdentification(it) }
    }
    LANGUAGE.consume()
    NAME_LIT.consume().also { element.language = consumedToken.string }
    REGULAR_COMMENT.consume().also { element.body = consumedToken.string.trim(' ') }
}

/**
 *      Namespace =
 *          ( PrefixMetadataMember )*
 *          NamespaceDeclaration NamespaceBody
 *
 *      NamespaceDeclaration : Namespace = 'namespace' Identification
 */
internal fun KerML.Namespace() = NamespaceAction(semantics, ElementType.Namespace).parse {
    NAMESPACE.consume()
    Identification().also { setIdentification(it) }
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
 *      Import = ( VisibilityIndicator )?
 *              'import' 'all'? ImportDeclaration RelationshipBody
 */
internal fun KerML.Import() = ImportAction(semantics).parse {
    IMPORT.consume()
    ALL.optional            { element.isImportAll = true }
    ImportDeclaration()
    RelationshipBody()
}

/**
 *      ImportDeclaration = MembershipImport | NamespaceImport
 */
internal fun KerML.ImportDeclaration() {
    QualifiedName().also { (semantics.action as ImportAction).importQualifiedName = it }
    alternatives {
        DPDP then TIMES  starts { NamespaceImport() }
        others                  { MembershipImport() }
    }
}

/**
 *      MembershipImport = [QualifiedName] ('::' '**'?)?
 */
internal fun KerML.MembershipImport() {
    semantics.element.type = ElementType.MembershipImport
    semantics.setTarget(IdentifiedByName((semantics.action as ImportAction).importQualifiedName?:"", IdentificationKind.Membership))
    DPDP.optional {
        STARSTAR.optional().also { semantics.element.isRecursive = true }
    }
}

/**
 *      NamespaceImport =  [QualifiedName] '::' '*' ('::' '**'?)?
 *                         | FilterPackage
 *
 *      FilterPackage  = ImportDeclaration ( FilterPackageMember )+
 *
 *      FilterPackageMember = '[' OwnedExpression ']'
 */
internal fun KerML.NamespaceImport() {
    semantics.element.type = ElementType.NamespaceImport
    semantics.setTarget(IdentifiedByName((semantics.action as ImportAction).importQualifiedName?:"", IdentificationKind.Namespace))
    DPDP.consume()
    TIMES.optional {
        DPDP.optional {
            semantics.action.element.isRecursive = true
            STARSTAR.consume()
        }
    }
}

/**
 *      AliasMember  = MemberPrefix
 *          'alias' ( '<' memberShortName = NAME '>' )?
 *          ( memberName = NAME )?
 *          'for' memberElement = [QualifiedName]
 *          RelationshipBody
 */
internal fun KerML.AliasMember() = OwnedRelationshipAction(semantics, ElementType.Membership).parse {
    ALIAS.consume()
    Identification()        .semantics { setIdentification(it) } // fixme: this is wrong, we need to set memberName, not declaredName
    FOR.consume()
    QualifiedName()         .semantics { setTarget(IdentifiedByName(it, IdentificationKind.Element)) }
    RelationshipBody()
}

/**
 * Parses a qualified name.
 *
 *      QualifiedName :- "NAME_LIT ("::" NAME_LIT)*
 */
fun KerML.QualifiedName(): QualifiedName {
    var qualifiedName = ""
    NAME_LIT.consume        { qualifiedName += consumedToken.string }
    noOrMore({ token.kind == DPDP && nextToken.kind == NAME_LIT }) {
        DPDP.consume()      { qualifiedName += "::" }
        NAME_LIT.consume    { qualifiedName += consumedToken.string }
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
    FEATURE, STEP, EXPR, BOOL, INV, CONNECTOR, BINDING, SUCCESSION, SUCCESSION, REDEFINES  // Prefixes that start a feature kind production rule
) + FeaturePrefixStart

fun KerML.FeatureElement() {
    FeaturePrefix()
    alternatives {
        REDEFINES starts { Feature() }
        FEATURE   starts { Feature() }
        STEP      starts { Step() }
        EXPR      starts { ExpressionFeature() }
        BOOL      starts { BooleanExpression() }
        INV       starts { Invariant() }
        CONNECTOR starts { Connector() }
        BINDING   starts { Unsupported("Not implemented yet") }
        SUCCESSION starts { Succession() }
        others           { Feature() }
    }
}

/**
 *      AnnotatingElement = Comment | Documentation | TextualRepresentation | MetadataFeature
 */
val annotatingElementStart = setOf(COMMENT, REGULAR_COMMENT, DOC, REP, METADATA, ATSIGN, LANGUAGE)
fun KerML.AnnotatingElement() {
    alternatives {
        COMMENT or REGULAR_COMMENT  starts { Comment() }
        DOC                         starts { Documentation() }
        LANGUAGE or REP             starts { TextualRepresentation()}
        setOf(METADATA, ATSIGN)     starts { MetadataFeature() }
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