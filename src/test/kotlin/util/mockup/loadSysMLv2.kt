package util.mockup

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session

/**
 * Loads a SysML v2 model from an input string into the session.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 */
fun Session.loadSysMLv2(
    input: String,
    runlevel: Runlevel = settings.runlevel,
){
    SysMLv2(model = this).parse(input)
    try {
        initialize(runlevel)
    } catch(exception: SysMDError) {
        status.error(message = "During initialization: ${exception.message}", kind = Issue.Kind.ERROR_SEMANTIC, cause = exception)
    }
}
