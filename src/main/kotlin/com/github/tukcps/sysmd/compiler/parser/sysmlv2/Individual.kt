package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.INDIVIDUAL
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported

fun SysMLv2.IndividualUsage() {
    INDIVIDUAL.consume()
    Unsupported()
}