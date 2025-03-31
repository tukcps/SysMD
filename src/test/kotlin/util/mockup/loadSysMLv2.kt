package util.mockup

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.session.Session

/**
 * Loads a SysML v2 model from an input string into the session.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 * @param createTextualRepresentation the name of textual representation that will be created in the KerML model
 * be turned off by setting catchExceptions to false.
 */
fun Session.loadSysMLv2(
    input: String,
    createTextualRepresentation: String? = null,
    generateAnnotations: Boolean = false
){
    var rep = TextualRepresentationImplementation(declaredName = createTextualRepresentation, language = "SysML", body=input)
    if (createTextualRepresentation != null) rep = create(rep, global)
    SysMLv2(
        model = this,
        generateAnnotations = (createTextualRepresentation != null) && generateAnnotations
    ).parse(rep)

    try {
        if (settings.initialize) initialize(5)
    }  catch (exception: SysMDError) {
        report(exception)
    }
}
