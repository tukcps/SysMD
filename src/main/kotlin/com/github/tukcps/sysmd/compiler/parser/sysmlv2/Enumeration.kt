package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token

/**
 * 8.2.2.8 Enumerations Textual Notation
 *
 *      EnumerationDefinition =
 *          DefinitionExtensionKeyword*
 *          'enum' 'def' DefinitionDeclaration EnumerationBody
 *          EnumerationBody : EnumerationDefinition =
 *      ';'
 *      | '{' ( ownedRelationship += AnnotatingMember | ownedRelationship += EnumerationUsageMember )* '}'
 *
 *      EnumerationUsageMember : VariantMembership =
 *      MemberPrefix ownedRelatedElement += EnumeratedValue
 *      EnumeratedValue : EnumerationUsage = 'enum'? Usage
 *      EnumerationUsage : EnumerationUsage = UsagePrefix 'enum' Usage
 */

fun SysMLv2.EnumerationDefinition() {
    Unsupported()
}
fun SysMLv2.enumerationDefinitionStarts() = token.kind == Token.Kind.ENUM && nextToken.kind == Token.Kind.DEF


fun SysMLv2.EnumerationUsage() {
    Unsupported()
}
fun SysMLv2.enumerationUsageStarts() = token.kind == Token.Kind.ENUM && nextToken.kind != Token.Kind.DEF
