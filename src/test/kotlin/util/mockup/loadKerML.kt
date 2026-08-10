package util.mockup

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session


/**
 * Loads a KerML model from an input string into the session.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 * @param catchExceptions by default, exceptions are caught, and errors are reported via status; this can
 * be turned off by setting catchExceptions to false.
 */
fun Session.loadKerML(
    input: String,
    runlevel: Runlevel = settings.runlevel,
){
    settings.runlevel = runlevel
    val elements = KerML(this).parse(input)
    import(elements)
    try {
        initialize(settings.runlevel)
    }  catch (exception: SysMDError) {
        status.fatal("Building/analyzing model failed", cause = exception)
    }
}