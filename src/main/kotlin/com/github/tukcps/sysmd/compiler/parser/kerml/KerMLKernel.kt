@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.reportInfo


/**
 * Association :-
 *      "assoc" Identification
 *          ["specializes" QualifiedName]
 *          ["from" Multiplicity QualifiedName]
 *          ["to" Multiplicity QualifiedName]
 *          Body
 */
fun KerML.Association() {
    val association = semantics.associationActions()
    ASSOC.consume()
    Identification().also { association?.identification = it }
    optional(SPECIALIZES, consume = true) {
        QualifiedName().also { association?.superclass = it }
    }
    // From, to !!!
    association?.create()
    Body(Resolved(association?.created!!))
}



/**
 * Connector :- "connector" Identification ":" QualifiedName
 *                  "from" QualifiedName "to" QualifiedName
 */
fun KerML.Connector() {
    val connector = semantics.connectorActions()

    CONNECTOR.consume()
    Identification().also { connector?.identification = it  }

    alternatives {
        EQ then { // SysMD Triple, deprecated.
            model.reportInfo(this@Connector.textualRepresentation,"Deprecated syntax of connextor")
            QualifiedNameList().also { connector?.source = it }
            QualifiedName().also { connector?.association = it }
            QualifiedNameList().also { connector?.target = it }
        }
        DP then {
            QualifiedName().also { connector?.association = it }
            optional(EQ, consume = true)
            optional(FROM, true) {
                QualifiedNameList().also { connector?.source = it }
            }
            optional(TO, true) {
                QualifiedNameList().also { connector?.target = it }
            }
        }
        FROM starts {
            FROM.consume()
            QualifiedNameList().also { connector?.source = it }
            optional(TO, true) {
                QualifiedNameList().also { connector?.target = it }
            }
        }
        TO starts {
            TO.consume()
            QualifiedNameList().also { connector?.target = it }
        }
        LCURBRACE starts {}
    }
    connector?.create()
    Body(Resolved(connector?.created!!))
}


/**
 * ValuePart : Feature =
 * ownedRelationship += FeatureValue
 * FeatureValue =
 * ( '='
 * | isInitial ?= ':='
 * | isDefault ?= 'default' ( '=' | isInitial ?= ':=' )?
 * )
 * ownedRelatedElement += OwnedExpression
 *
 */


/**
 * 8.2.5.12 Metadata Concrete Syntax
 * Metaclass = TypePrefix 'metaclass' ClassifierDeclaration TypeBody
 *
 * PrefixMetadataAnnotation : Annotation = '#' ownedRelatedElement += PrefixMetadataFeature
 * PrefixMetadataMember : OwningMembership = '#' ownedRelatedElement += PrefixMetadataFeature
 * PrefixMetadataFeature : MetadataFeature : ownedRelationship += OwnedFeatureTyping
 * MetadataFeature =
 *      ( ownedRelationship += PrefixMetadataMember )*
 *      ( '@' | 'metadata' )
 *      MetadataFeatureDeclaration
 *      ( 'about' annotation += Annotation
 *          { ownedRelationship += annotation }
 *          ( ',' annotation += Annotation
 *          { ownedRelationship += annotation } )*
 *      )?
 *      MetadataBody
 *
 * MetadataFeatureDeclaration : MetadataFeature = ( Identification ( ':' | 'typed' 'by' ) )?
 *  ownedRelationship += OwnedFeatureTyping
 *
 * MetadataBody : Feature = ';' | '{' ( ownedRelationship += MetadataBodyElement )* '}'
 * MetadataBodyElement : Membership =
 *      NonFeatureMember
 *      | MetadataBodyFeatureMember
 *      | AliasMember
 *      | Import
 *
 * MetadataBodyFeatureMember : FeatureMembership =
 *      ownedMemberFeature = MetadataBodyFeature
 *
 * MetadataBodyFeature : Feature =
 *      'feature'? ( ':>>' | 'redefines')? ownedRelationship += OwnedRedefinition
 *      FeatureSpecializationPart? ValuePart?
 *      MetadataBody
 */


/**
 * Invariant :- "inv" Identification "{" Expression "}" // Expression is of type Boolean and must be satisfied
 */
fun KerML.Invariant() {
    val invariant = semantics.invariantActions()
    INV.consume()
    Identification().also { invariant?.identification = it; invariant?.create() }

    LCURBRACE.consume() // TODO: Body
    val kerml = invariant?.created!!
    val iBeforeExpression = token.indices.first
    Expression().also {
        kerml.featureWithValue = AstRoot(model, kerml, it)
        kerml.indices = iBeforeExpression .. consumedToken.indices.last
        kerml.expression = input.subSequence(kerml.indices!!).toString().trim()
    }
    RCURBRACE.consume()
}



/**
 * 8.2.5.13 Packages Concrete Syntax
 * Package = ( ownedRelationship += PrefixMetadataMember )*
 *           PackageDeclaration PackageBody
 *
 * PackageDeclaration : Package = 'package' Identification
 * PackageBody : Package =
 *      ';'
 *      | '{' ( NamespaceBodyElement | ownedRelationship += ElementFilterMember)* '}'
 *
 * ElementFilterMember : ElementFilterMembership =
 *      MemberPrefix 'filter' condition = OwnedExpression ';'
 */
/**
 * Package :- "package" Identification Body
 */
fun KerML.Package() {
    val pkg = semantics.packageActions()
    PACKAGE.consume()
    Identification().also { pkg?.identification = it }
    pkg?.create()
    Body(Resolved(null, pkg?.created, null))
}


/**
 * LibraryPackage = ( isStandard ?= 'standard' ) 'library'
 *          ( ownedRelationship += PrefixMetadataMember )*
 *          PackageDeclaration PackageBody
 */
fun KerML.LibraryPackage() {
    val pkg = semantics.packageActions().also { it?.isLibrary = true }
    STANDARD.optional { pkg?.isStandard = true }
    LIBRARY.consume()
    PACKAGE.consume()
    Identification().also { pkg?.identification = it }
    pkg?.create()
    Body(Resolved(null, pkg?.created, null))
}



/**
 * Function :- "function" Identification [ :> QualifiedName] FunctionBody
 */
fun KerML.Function() {
    val semantics = semantics.functionActions()
    FUNCTION.consume()
    Identification().also { semantics?.identification = it }
    optional(SPECIALIZES, consume = true) {
        QualifiedName().also { semantics?.superclass = it }
    }
    semantics?.create()
    FunctionBody(Resolved(semantics?.created!!))
}


/**
 * FunctionBody :-
 *            "{" FeatureList "}"
 *          | ";"
 *          | "."  // Only for SysMD to end Triple
 */
internal fun KerML.FunctionBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            oneOrMoreUntil(RCURBRACE) {
                MemberPrefix()
                alternatives {
                    RETURN then  { semantics.prefixes.add(OUT); Feature() }
                    others       { TypeBodyElement() }
                }
            }
            semantics.popOwner()
            RCURBRACE.consume()
        }
        SEMICOLON then { }
    }
}
