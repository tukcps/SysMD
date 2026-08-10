@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported


/**
 * 8.2.2.27 Metadata Textual Notation
 *
 *      MetadataDefinition =
 *          ('abstract')? DefinitionExtensionKeyWord*
 *          'metadata' 'def' Definition
 */
fun SysMLv2.MetadataDefinition() {
    Unsupported()
}


/**
 *      PrefixMetadataUsage = OwnedFeatureTyping
 */
fun SysMLv2.PrefixMetadataUsage() {
    Unsupported()
}


/**
 * MetadataUsage =
 * UsageExtensionKeyword* ( '@' | 'metadata' )
 * MetadataUsageDeclaration
 * ( 'about' ownedRelationship += Annotation
 * ( ',' ownedRelationship += Annotation )*
 * )?
 * MetadataBody
 * MetadataUsageDeclaration : MetadataUsage =
 * ( Identification ( ':' | 'typed' 'by' ) )?
 * ownedRelationship += OwnedFeatureTyping
 * MetadataBody : Type =
 * ';' |
 * '{' ( ownedRelationship += DefinitionMember
 * | ownedRelationship += MetadataBodyUsageMember
 * | ownedRelationship += AliasMember
 * | ownedRelationship += Import
 * )*
 * '}'
 */

/**
 * MetadataBodyUsageMember : FeatureMembership =
 * ownedMemberFeature = MetadataBodyUsage
 * MetadataBodyUsage : ReferenceUsage :
 * 'ref'? ( ':>>' | 'redefines' )? ownedRelationship += OwnedRedefinition
 * FeatureSpecializationPart? ValuePart?
 * MetadataBody
 * ExtendedDefinition : Definition =
 * BasicDefinitionPrefix? DefinitionExtensionKeyword+
 * 'def' Definition
 * ExtendedUsage : Usage =
 * UnextendedUsagePrefix UsageExtensionKeyword+
 * Usage
 */