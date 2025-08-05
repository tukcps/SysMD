package com.github.tukcps.sysmd.model.expression.functions


import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDFatalInternalError
import com.github.tukcps.sysmd.imports.JsonImporter
import com.github.tukcps.sysmd.imports.Result
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.session.Session
import java.io.File

internal class AstCharacterizedResult (
    model: Session,
    private val namespace: Namespace,
    parameters: ArrayList<AstNode>
)
    : AstFunction("characterizedResult", model, 2, parameters)
{

    private var importedResults : MutableList<Result>? = null
    private lateinit var jsonQuantity : Quantity
    private var filePath = ""

    override fun initialize() {
        upQuantity = Quantity(model.builder.Reals, "?")

        // Set standard file path for result.json for the case user does not specify own one
         filePath = (
                this.namespace.owner?.declaredName ?: if(parameters.size == 1) {
                    throw SysMDFatalInternalError("Could not find a package name for the result folder. Please pass the Path to the result.json directly as a second Argument or put the Attribute into a proper Package.")
                } else "COULD_NOT_DERIVE_PATH"
         ) + "/testbenches/results.json"


        // Check if 2 parameters have been passed
        if(parameters.size > 2){
            throw SemanticError("Too many Parameters in Function ’characterizedResult’ - can take a maximum of two parameters!")
        }

        //Check if 2 parameters have been passed
        if(parameters.isEmpty()){
            throw SemanticError("Not enough Parameters in Function ’characterizedResult’ should contain one or two Parameter!")
        }

        //Check if parameter 0 is an AstFunction, if so initialize the json_Quantity as a [-Inf,+Inf] range using the AstFunctions unit
        if(parameters[0] is AstFunction){
            jsonQuantity = Quantity(model.builder.real(Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY), parameters[0].upQuantity.unit.toString())
        }else{
            throw SemanticError("The first parameter of 'characterizedResult' must always be a Function")
        }

        //Try to read FilePath from Second Parameter, if not possible use default File path for Result files
        if(parameters[1].isString){
            File((parameters[1] as AstLeaf).literalVal?.value.toString()).let {
                if(it.isFile) filePath = it.absolutePath
            }
        }else{
            throw SemanticError("The second parameter should be a String!")
        }

        /**
         * Try to load a result from JSON file and set min and max values in the Quantity
         * If loading results is not possible to set the min and max values of the Quantity to -Inf and +Inf
         */
        try {
            importedResults = JsonImporter.importJson(
                filePath = filePath
            )

            if(importedResults != null){
                for(result in importedResults!!) {
                    //Check if this result contains the correct Module and Attribute names that we are looking for
                    if(result.attributeQualifiedName == (this.root as AstRoot).variable.name){

                        //Check if the Units of the upQuantity and the one from the JSON are equal
                        // IF YES: Overwrite the [-INF,+INF] range of the json_Quantity with the min max values from the JSON File
                        // IF NO: Throw an Error, the remaining process will use the [-INF,+INF] range of the json_Quantity
                        try {
                            if(parameters[0].upQuantity.unit == Unit(result.resultUnit).toSI()) {
                                jsonQuantity = Quantity(model.builder.real(result.resultValue..result.resultValue), result.resultUnit)
                                break
                            }else{
                                //Units did not match, inform the User and create a Quantity that uses the ASTFunction's unit to enable the intersect function in the evalUp() call
                                throw Exception("The Unit of the imported results and the upValue of the ASTFunction do not match!\n" +
                                        "The values from the JSON file could not be used and are replaced with an [-Inf,+Inf] Interval.")
                            }
                        } catch (e : Exception) {
                            model.status.error(e.message?:"(unknown issue with characterization import)", cause = e)
                        }
                    }
                }
            } else {
                throw SysMDFatalInternalError("No Result file found for attribute: ${((this.root as AstRoot).dependency as AstCharacterizedResult).name}")
            }
        } catch (e : Exception){
            model.status.fatal(e.message.toString(), cause = e)
        }

    }

    override fun evalUp() {
        upQuantity = try {
            jsonQuantity.intersect(parameters[0].upQuantity)
        }catch (e : Exception) {
            println(e.message + "\nIntersection of the imported result and upValue from the ASTFunction could not be computed." +
                    "Therefor using the upValue of the ASTFunction instead")
            parameters[0].upQuantity
        }
    }

    override fun evalDown() {

    }

}
