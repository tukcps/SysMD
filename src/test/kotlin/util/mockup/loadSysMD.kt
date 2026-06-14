package util.mockup

import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session


/**
 * Loads a SysMD model from an input string into the session.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 */
fun Session.loadSysMD(
    input: String,
    runlevel: Runlevel = settings.runlevel
) {
    SysMD(this).parse(input)
    try {
        initialize(runlevel)
    } catch (exception: SysMDError) {
        status.fatal("Initialization failed", cause = exception)
    }
}
