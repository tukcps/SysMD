package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

/*
 * This file provides
 * - (sets of) tokens that start a production rule and
 * - functions that check conditions that start a production rule.
 */
val refPrefixStart = setOf(IN, OUT, INOUT, ABSTRACT, VARIATION, READONLY, DERIVED, END)
val basicUsagePrefixStart = refPrefixStart + REF
val usagePrefixStart = basicUsagePrefixStart


fun SysMLv2.usageDeclarationStarts() = setOf(LCBRACE, TYPED_BY, REFERENCES, REDEFINES).starts()
        || (token.kind == NAME_LIT && nextToken.kind in setOf(TYPED_BY, LT, SEMICOLON))

fun SysMLv2.referenceUsageStarts() = usageStarts() // + refPrefixStart + REF
