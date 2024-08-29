package com.github.tukcps.sysmd.exports.systemCElements

import com.github.tukcps.sysmd.exports.systemCElements.ModuleType.Discrete
import com.github.tukcps.sysmd.exports.systemCElements.ModuleType.TDF
import com.github.tukcps.sysmd.model.kerml.Feature
import java.io.File

enum class ModuleType{
    Discrete,
    TDF
}

/**
 * Representation of a module that holds all necessary information for writing the template
 * @param moduleName Name of the Module
 * @param fullQualifiedName String
 * @param superClassFullQualifiedName The actual ClassImplementation element of this Module;
 * can be NULL if this Module is not constructed based on a ClassImplementation element
 */
class Module(
    val moduleName: String,
    val fullQualifiedName : String,
    val superClassFullQualifiedName : String?
)   {

    //Basic information about this Module
    var moduleType: ModuleType = TDF

    //Information about the superclass
    //var superClassName : String? = null //Holds the name of the superclass
    //var superClassElement : ClassImplementation? = null //Reference to the element that defines the superclass
    var superClassModule : Module? = null
    var useSuperClass : Boolean = true

    /**Returns this or the superClass Module depending on inheritance situation**/
    val actualModule : () -> Module = {if (useSuperClass) superClassModule!!.actualModule.invoke() else this}

    //Holds all usages in which the module appears
    var moduleUsages : MutableList<Usage> = mutableListOf()

    // Holds the name, quantity and datatype of the SystemC Variable/Constant.
    // They will be sorted to inputPorts, outputPorts, constants and variables in toSystemC function
    // and after that, are more or less obsolete (they might still be needed for some checks)
    val expressions : MutableList<Feature> = mutableListOf()
    val ignoredExpressions : MutableList<Feature> = mutableListOf()

    //Holds subModules (their Usage) of this module
    val subModules : MutableList<Usage> = mutableListOf()

    //Ports
    val inputPorts : MutableList<Port> = mutableListOf()
    val outputPorts : MutableList<Port> = mutableListOf()
    val bidirectionalPorts : MutableList<Port> = mutableListOf() //TODO Not really used yet!!! Has to be considered in the SystemC Code generation.

    //Constants and variables (variables with initial values and variables that get value in constructor)
    val constants : MutableList<Constant> = mutableListOf()
    val variables : MutableList<Variable> = mutableListOf()
    val variablesNoValues : MutableList<VariableNoValues> = mutableListOf()

    val channels : MutableList<Channel> = mutableListOf()


    /**
     * Function to create header files for a module with starter code
     */
    fun writeHeaderFile(
        path: String,
        macros: BooleanArray,
        printRangeOrValue: (String?, String?, String?, String, DataType) -> String
    ) {
        val filename = "$path/${this.moduleName}.h".replace("::", "_")
        File(filename).printWriter().use { out ->
            out.println("#ifndef _${this.moduleName.uppercase()}_H_\n#define _${this.moduleName.uppercase()}_H_\n")
            out.println("#include <systemc>\n#include <systemc-ams>\n\n#include <string>\nusing namespace std;\nusing namespace sc_core;\n")
            if(this.superClassModule != null) out.println("#include \"${this.superClassModule!!.moduleName}.h\"")

            val subModIncludes : MutableList<String> = mutableListOf()


            //Generate all include strings and store them in the list
            //Via the 'any{}' function it is checked that already existing includes are skipped and no duplicates are generated
            this.subModules.forEach { subMod ->

                if(!subModIncludes.any { it == "#include \"${subMod.module.moduleName}.h\"" }) {
                    subModIncludes.add("#include \"${subMod.module.moduleName}.h\"")
                }
            }

            /**INCLUDE MODULES**/
            subModIncludes.forEach { includeName ->
                out.println(includeName)
            }

            /**INCLUDE PORTS**/
            run runBlock@{
                this.inputPorts.forEach { pt ->
                    if (pt.isTarget() == true) {
                        out.println("#include \"TLM_${pt.associatedChannels.first().channelName}_Target.h\"")
                        //Break out of loop to ensure Target include is only printed once per file
                        return@runBlock
                    }
                }
            }

            run runBlock@{
                this.outputPorts.forEach { pt ->
                    if (pt.isInitiator() == true) {
                        out.println("#include \"TLM_${pt.associatedChannels.first().channelName}_Initiator.h\"")
                        //Break out of loop to ensure Initiator include is only printed once per file
                        return@runBlock
                    }
                }
            }

            /**INCLUDE CHANNELS**/
            this.channels.forEach { ch ->
                if (ch.channelType == ChannelType.HIERARCHICAL) {
                    out.println("#include \"${ch.channelName}_class.h\"")
                }
            }
            out.println("")


            //Start the Module (not using the Module Macro to define inheritance)
            out.println("class ${this.moduleName} : ${if(this.superClassModule != null) "public ${this.superClassModule!!.moduleName}{" else " " +
                    "public ${if (this.moduleType == Discrete) "sc_module{" else "sca_tdf::sca_module{" }"}")

            out.println("\npublic:")

            /**CONSTANTS**/
            if (this.constants.size > 0) {
                out.print("\n\t//\t### Constants ###")
            }
            this.constants.forEach { constant ->
                constant.write(out)
            }

            /**UN-PARAMETRIZED VARIABLES**/
            if (this.variablesNoValues.size > 0) {
                out.print("\n\n\t//Variables with values NOT defined in SysMD ###")

                this.variablesNoValues.forEach { variableNoValue ->
                    variableNoValue.writeForHeader(out)
                }
            }


            /**PARAMETRIZED VARIABLES**/
            if (this.variables.size > 0) {
                out.print("\n\n\t//Variables with values defined in SysMD ###")

                this.variables.forEach { variable ->
                    variable.writForHeader(out, printRangeOrValue)
                }
            }


           //Print ports
            if ((this.inputPorts.size > 0) or (this.outputPorts.size > 0)) {
                out.println("\n\n\t//\t### Ports ###")

                /**INPUT PORTS**/
                this.inputPorts.forEach { port ->
                    if(!port.isInherited || port.createdFromExpression){
                        if (port.isTarget() == true) {//Handle TLM Port
                            out.println("\tTLM_${port.associatedChannels.first().channelName}_Target ${port.portName};")
                        } else {
                            if (port.isBoundToHierarchicalChannel == true) { //Binds to a hierarchical channel
                                out.println("\tsc_port<sc_signal_in_if<${port.dataType.toCPPDataType()}> > ${port.portName};")
                            } else { // Is a normal input port binding to a simple signal
                                out.println("\tsca_tdf::sca_in<${port.dataType.toCPPDataType()}> ${port.portName};")
                            }
                        }
                    }
                }

                /**OUTPUT PORTS**/
                this.outputPorts.forEach { port ->
                    if(!port.isInherited || port.createdFromExpression) {
                        if (port.isInitiator() == true) {//Handle TLM Port
                            out.println("\tTLM_${port.associatedChannels.first().channelName}_Initiator ${port.portName};")
                        } else {
                            if (port.isBoundToHierarchicalChannel == true) { //Binds to a hierarchical channel
                                out.println("\tsc_port<sc_signal_out_if<${port.dataType.toCPPDataType()}> > ${port.portName};")
                            } else { // Is a normal output port binding to a simple signal
                                out.println("\tsca_tdf::sca_out<${port.dataType.toCPPDataType()}> ${port.portName};")
                            }
                        }
                    }
                }

                /**INOUT PORTS**/
                this.bidirectionalPorts.forEach { port ->
                    if(!port.isInherited  || port.createdFromExpression) {
                        if (port.isTarget() == true) {//Handle TLM Port
                            //TODO Handle inout for TLM
                            out.println("\tTLM_${port.associatedChannels.first().channelName}_Target ${port.portName};")
                        } else {
                            if (port.isBoundToHierarchicalChannel == true) { //Binds to a hierarchical channel
                                //TODO Handle inout for Hierachical channels
                                out.println("\tsc_port<sc_signal_in_if<${port.dataType.toCPPDataType()}> > ${port.portName};")
                            } else { // Is a normal input port binding to a simple signal
                                out.println("\tsca_tdf::sca_inout<${port.dataType.toCPPDataType()}> ${port.portName};")
                            }
                        }
                    }
                }
            }


            /**SUB-MODULES**/
            if (this.subModules.size > 0) {
                out.println("\n\n\t//\t### Modules ###")

                this.subModules.forEach { subMod ->
                    out.print("\t${subMod.className} ")
                    if (subMod.amount == 1) {
                        out.print("${subMod.instanceName};\n")
                    } else {
                        for (i in 0..<subMod.amount) {
                            if (i != subMod.amount - 1) {
                                out.print("${subMod.instanceName}_${i}, ")
                            } else {
                                out.print("${subMod.instanceName}_${i};\n")
                            }
                        }
                    }
                }
            }


            /**CONSTRUCTOR**/
            out.print(
                "\n\n\t//\t### Constructor ###\n" +
                        "\t${this.moduleName}(sc_core::sc_module_name nm"
            )

            this.variablesNoValues.forEach { variableNoValues ->
                out.print(",\n\t\t${variableNoValues.dataType.toCPPDataType()} ${variableNoValues.name}_")
            }

            out.println(");")


            //Print channels
            if (this.channels.size > 0) {
                out.println("\n\t//\t### Channels  ###")

                this.channels.forEach { channel ->
                    when (channel.channelType) {
                        ChannelType.PRIMITIVE -> out.println("\tsca_tdf::sca_signal<${channel.channelDataType!!.toCPPDataType()}> ${channel.channelName};")
                        ChannelType.TLM -> {
                            out.println(
                                "\tTLM_${(channel.channelName).lowercase()}<${channel.inputPortsBindingLimit},${channel.outputPortsBindingLimit}> " +
                                        "${(channel.channelName).lowercase()}(\"${channel.channelName}\");"
                            )
                        }

                        ChannelType.HIERARCHICAL -> {
                            out.println("\t${channel.channelName}_class ${(channel.channelName).lowercase()};")
                        }

                    }

                }
            }


            //Printing the desired SystemC AMS Macros (ONLY for TDF Modules!)
            out.println("\n\t//\t### Functions ###")

            if(this.moduleType == TDF){
                if (macros[0]) {
                    out.println("\tvoid set_attributes();\n")
                }
                if (macros[1]) {
                    out.println("\tvoid change_attributes();\n")
                }
                if (macros[2]) {
                    out.println("\tvoid initialize();\n")
                }
                if (macros[3]) {
                    out.println("\tvoid reinitialize();\n")
                }
                if (macros[4]) {
                    out.println("\tvoid processing();\n")
                }
                if (macros[5]) {
                    out.println("\tvoid ac_processing();\n")
                }
            }

            //Print the end of the module
            out.println("};\n#endif // _${this.moduleName.uppercase()}_H_")
        }
    }

    fun writeSourceFile(path: String, macros: BooleanArray) {
        val filename = "$path/${this.moduleName}".replace("::", "_")
        File("$filename.cpp").printWriter().use { out ->
            out.println("#include \"${this.moduleName}.h\"\n")

            var colonIsSet = false //Necessary to know if a colon has already been set by module instantiations or has to be set by instantiation of TLM Ports or Hierarchical channels


            //Constructor Implementation
            out.println("\n//\t### Constructor Implementation ###")
            out.print("${this.moduleName}::${this.moduleName}(sc_core::sc_module_name nm")
            this.variablesNoValues.forEach { variableNoValues ->
                out.print(",\n\t\t${variableNoValues.dataType.toCPPDataType()} ${variableNoValues.name}_")
            }

            out.print(")")


            val colon = {
                if(colonIsSet) "," else {colonIsSet = true; ":"}
            }

            //Print SuperClass Constructor
            if(this.superClassModule != null) out.print( colon.invoke() + "${this.superClassModule!!.moduleName}( nm )")


            //Initializer List: Print owning modules instantiation (FeatureImplementations)
            this.subModules.forEach { subMod ->
                if (subMod.amount == 1) {
                    out.print(colon.invoke() + "${subMod.instanceName}(\"${subMod.instanceName}\")")
                } else {
                    for (i in 0..<subMod.amount) {
                        out.print(colon.invoke() + "${subMod.instanceName}_${i}(\"${subMod.instanceName}_$i\") ")
                    }
                }
            }


            //Intializer List: Print Ports
            //Inherited Ports are not initialized as this is done in the constructor of the SuperClass
            this.inputPorts.forEach { port ->
                if(!port.isInherited  || port.createdFromExpression) out.print(colon.invoke() + "${port.portName}(\"${port.portName}\")")
            }
            this.outputPorts.forEach { port ->
                if(!port.isInherited || port.createdFromExpression) out.print(colon.invoke() + "${port.portName}(\"${port.portName}\")")
            }
            this.bidirectionalPorts.forEach { port ->
                if(!port.isInherited || port.createdFromExpression) out.print(colon.invoke() + "${port.portName}(\"${port.portName}\")")
            }

            //Initializer List: Print instantiations of Hierarchical Channels
            this.channels.forEach { ch ->
                out.print(colon.invoke() + "${ch.channelName}(\"${ch.channelName}\")")
            }

            //Inside Constructor
            out.println("{") //Opening Bracket of Constructor

            //Initialize the variables that get their value from constructor call
            this.variablesNoValues.forEach { variableNoValues ->
                variableNoValues.writeForSource(out)
            }

            //Print initialization of variables whose value (in fact it's their range) is known
            if (this.variables.size > 0) {
                out.print("\n")
            }
            this.variables.forEach { variable ->
                variable.writeForSource(out)
            }

            //Connect ports of channels that belong to this module
            if (this.channels.size > 0) {
                out.println("\n\t//\t### Connect ports and channels ###")

                this.channels.forEach { ch ->
                    ch.printPortBinding(this.fullQualifiedName, out)
                }
            }

            out.println("\n}")  //Closing Bracket of Constructor

            // Printing the desired SystemC AMS Macros (ONLY for TDF Modules!)
            out.println("\n//\t### Functions ###")
            if(this.moduleType == TDF) {
                if (macros[0]) {
                    out.println("void ${this.moduleName}::set_attributes(){\n\n}\n")
                }
                if (macros[1]) {
                    out.println("void ${this.moduleName}::change_attributes(){\n\n}\n")
                }
                if (macros[2]) {
                    out.println("void ${this.moduleName}::initialize(){\n\n}\n")
                }
                if (macros[3]) {
                    out.println("void ${this.moduleName}::reinitialize(){\n\n}\n")
                }
                if (macros[4]) {
                    out.println("void ${this.moduleName}::processing(){\n\n}\n")
                }
                if (macros[5]) {
                    out.println("void ${this.moduleName}::ac_processing(){\n\n}\n")
                }
            }
        }
    }



}
