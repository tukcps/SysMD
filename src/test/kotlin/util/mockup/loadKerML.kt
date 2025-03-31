package util.mockup

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.session.Session


/**
 * Loads a KerML model from an input string into the session.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 * @param catchExceptions by default, exceptions are caught, and errors are reported via status; this can
 * @param createTextualRepresentation the name of textual representation that will be created in the KerML model
 * be turned off by setting catchExceptions to false.
 */
fun Session.loadKerML(
    input: String,
    catchExceptions: Boolean = settings.catchExceptions,
    createTextualRepresentation: String? = null,
    generateAnnotations: Boolean = false
){
    settings.catchExceptions = catchExceptions
    var rep = TextualRepresentationImplementation(declaredName = createTextualRepresentation, language = "KerML", body=input)
    if (createTextualRepresentation != null) rep = create(rep, global)
    KerML(
        model = this,
        generateAnnotations = (createTextualRepresentation != null) && generateAnnotations
    ).parse(rep)

    try {
        if (settings.initialize) initialize()
    }  catch (exception: SysMDError) {
        report(exception)
        if (!settings.catchExceptions)
            throw exception
    }
}


