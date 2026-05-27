@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedMultiplicity
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LCBRACE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.THEN

/**
 * 8.2.2.9.3 Occurrence Successions
 */

/**
 *      SourceSuccessionMember = 'then' SourceSuccession
 */
fun SysMLv2.SourceSuccessionMember() {
    THEN.consume()
    SourceSuccession()
}
fun SysMLv2.souurceSuccessionMemberStarts() = THEN.starts()

/**
 *      SourceSuccession = SourceEndMember
 *      SourceEndMember = SourceEnd
 *      SourceEnd = ( OwnedMultiplicity )?
 */
fun SysMLv2.SourceSuccession() {
    optional(LCBRACE) {
        OwnedMultiplicity()
    }
}