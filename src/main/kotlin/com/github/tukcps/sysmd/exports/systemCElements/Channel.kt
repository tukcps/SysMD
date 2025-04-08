package com.github.tukcps.sysmd.exports.systemCElements

import com.github.tukcps.sysmd.exceptions.SysMDFatalInternalError
import java.io.File
import java.io.PrintWriter

enum class ChannelType {
    PRIMITIVE,
    HIERARCHICAL,
    TLM,
}

/**
 * representation of a simple/hierarchical channel that holds the channel name and
 * the ports that have to be bound to it in the sc_main function
 */
class Channel(//Name of the Channel
    val channelName: String, //Determines the type of the channel (primitive, hierarchical, ..)
    val channelType: ChannelType
) {

    var trace : Boolean = false

    var channelDataType : DataType? = null  //Data Type that the channel uses


    /**
     *                      Clarification of Source and Target mapping
     * ------------------------------------------------------------------------------------------
     * The SysMD Relationship holds SOURCEs and TARGETs.
     * A Source outputs information and therefore is put into the outputPorts List of a channel.
     * A Target receives input and therefore is put into the inputPorts List of a channel.
     *
     * That's why for a TLM Channel:
     *      - every Source (Element of the outputPorts list) needs a Target_socket in the bus
     *      - every Target (Element of the inputPorts list) needs an Initiator_socket in the bus
     *
     *      SOURCE ---> Target_socket : [TLM_CHANNEL] : Initiator_socket --- > TARGET
     *     (OUTPUT)                                                            (INPUT)
     *
     */
    val inputPorts : MutableList<Port> = mutableListOf()
    val outputPorts : MutableList<Port> = mutableListOf()
    val bidirectionalPorts : MutableList<Port> = mutableListOf()


    //      TLM Specific variables

    //The limits are derived from the instantiationAmount of the ports module they belong to
    //If the module a Port belongs to is instantiated multiple times, the port will exist just as often.
    //Therefor the corresponding initiator/target sockets amount has to be adapted
    var inputPortsBindingLimit = 0
    var outputPortsBindingLimit = 0

    //Tells how many input/output ports are already bound. T
    //They must not exceed the limit set in the inputPortsBindingLimit/outputPortsBindingLimit variables!
    var boundInputPorts : Int = 0
    var boundOutputPorts : Int = 0


     /**
      * This function traverses the ports of the channel to determine the dataType that is used on the channel
      * IT gets the extracDataType function passed which is implemented in toSystemC.kt
     */
    fun determineChannelDataType(){

        //Set the Channel data according to the first port
        if (inputPorts.size > 0) channelDataType  = inputPorts.first().dataType else
            if (outputPorts.size > 0) channelDataType  = outputPorts.first().dataType

        //Traverse all ports and check for conflict between DataTypes
        //If a conflict is found, throw an Exception
        inputPorts.forEach {pt ->
            if(channelDataType != pt.dataType){
                throw SysMDFatalInternalError("Some ports of channel \"${channelName}\" use unequal data types!")
            }
        }
        outputPorts.forEach {pt ->
            if(channelDataType != pt.dataType){
                throw SysMDFatalInternalError("Some ports of channel \"${channelName}\" use unequal data types!")
            }
        }
    }



    /**
     * If this channel is a TLM channel it has to tell its Ports that they are initiators/targets
     * If this channel is a hierarchical channel it sets a variable so the ports are declared using the correct interface
     */

    fun checkTLMandHierachical(){
        //Check if Channel is TLM or Hierarchical
        when(channelType) {
            ChannelType.TLM -> {
                //Set Input ports to TLM Target and Output Ports to TLM Initiator
                inputPorts.forEach { it.setTLMTarget() }
                outputPorts.forEach { it.setTLMInitiator() }
                calculatePortLimits()
            }

            ChannelType.HIERARCHICAL -> {
                inputPorts.forEach { it.isBoundToHierarchicalChannel = true }
                outputPorts.forEach { it.isBoundToHierarchicalChannel = true }
            }

            else -> {} //No action necessary
        }
    }


    /**
     * Sets the limit for target/initiator ports of this channel.
     * This is done by looking at the modules of ports and their instantiation amount.
     * If a module is instantiated multiple times their ports will be multiplied by the same amount
     * and therefore the channel has to provide the matching amount of initiator/target sockets
     */

    private fun calculatePortLimits(){
        inputPorts.forEach {pt ->
            pt.module!!.actualModule.invoke().let {
                 it.moduleUsages.forEach { usage ->
                    inputPortsBindingLimit += usage.amount
                }
            }
        }


        outputPorts.forEach {pt ->
            pt.module!!.actualModule.invoke().let {
                it.moduleUsages.forEach { usage ->
                    outputPortsBindingLimit += usage.amount
                }
            }
        }
    }


    /**
     * Is called by the toSystemC function to create the files for this TLM channel
     * TODO create correct amount of target/initiator sockets with respect to how often the connected modules are instantiated
     */

    fun createTLMChannel(path : String){
        File("$path/TLM_${channelName}.h").printWriter().use { out ->
            out.print("#ifndef _TLM_${channelName.uppercase()}_H_\n#define _TLM_${channelName.uppercase()}_H_\n" +
                    "#include <systemc>\n#include <systemc-ams>\n#include \"tlm.h\"\n\n" +
                    "#include \"tlm_utils/simple_initiator_socket.h\"\n" +
                    "#include \"tlm_utils/simple_target_socket.h\"\n\n" +
                    "using namespace sc_core;\n" +
                    "using namespace sc_dt;\n" +
                    "using namespace std;\n\n" +

                    "template <int NR_OF_INITIATORS, int NR_OF_TARGETS>\n" +

                    "struct TLM_${channelName} : sc_module{\n\n" +


                    /**
                     * To understand relation between the instantiation of Target/Initiator Sockets and the elements
                     * of the input-/outputPorts lists read the "Clarification of Source and Target mapping" at the top of this file
                     */


                    //Checks if multiple input ports are there, if so we need tagged target sockets
                    (if (outputPortsBindingLimit > 1) "\ttlm_utils::simple_target_socket_tagged<TLM_${channelName}>*    target_socket[NR_OF_TARGETS];\n" else
                        "\ttlm_utils::simple_target_socket<TLM_${channelName}>    target_socket;\n") +

                    //Checks if multiple output ports are there, if so we need tagged initiator sockets
                    (if (inputPortsBindingLimit > 1) "\ttlm_utils::simple_initiator_socket_tagged<TLM_${channelName}>*    initiator_socket[NR_OF_INITIATORS];\n\n" else
                        "\ttlm_utils::simple_initiator_socket<TLM_${channelName}>    initiator_socket;\n\n") +


                    "\tTLM_${channelName}(sc_core::sc_module_name nm){\n\n" +

                    //Case dependent initialization of one simple initiator socket or multiple tagged initiator sockets
                    (if (inputPortsBindingLimit> 1)
                        "\t\tfor (unsigned int i = 0; i < NR_OF_INITIATORS; i++){\n" +
                                "\t\t\tchar tag[20];\n" +
                                "\t\t\tsprintf(tag, \"initiator_socket_%d\", i);\n" +
                                "\t\t\tinitiator_socket[i] = new tlm_utils::simple_initiator_socket_tagged<TLM_${channelName}>(tag);\n" +
                                "\t\t}\n\n"
                    else
                        "\t\ttlm_utils::simple_initiator_socket<TLM_${channelName}> initiator_socket(\"initiator_socket\");\n\n") +


                    //Case dependent initialization of one simple target socket or multiple tagged target sockets
                    (if (outputPortsBindingLimit > 1)
                        "\t\tfor (unsigned int i = 0; i < NR_OF_TARGETS; i++){\n" +
                                "\t\t\tchar tag[20];\n" +
                                "\t\t\tsprintf(tag, \"target_socket_%d\", i);\n" +
                                "\t\t\ttarget_socket[i] = new tlm_utils::simple_target_socket_tagged<TLM_${channelName}>(tag);\n"   +
                                "\t\t}\n\n"
                    else
                        "\t\ttlm_utils::simple_target_socket<TLM_${channelName}> target_socket(\"target_socket\");\n\n") +

                    "\t} //Constructor End"   +
                    "\n};\n\n#endif // _TLM_${channelName.uppercase()}_H_"
            )
        }
    }

    fun createHierarchicalChannel(path: String) {
        File("$path/${channelName}_class.h").printWriter().use { out ->
            //Includes
            out.print("#ifndef __${channelName.uppercase()}_CLASS_H__\n#define __${channelName.uppercase()}_CLASS_H__\n\n#include <systemc>\n#include <systemc-ams>\nusing namespace sc_core;\n\n")

            //Class definition with interface
            out.print(
                "class ${channelName}_class : public sc_channel, public sc_signal_inout_if<${
                    channelDataType!!.toCPPDataType()
                }>{\n\npublic:"
            )

            //Constructor
            out.print("\n\n\t//Constructor\n\t${channelName}_class(sc_module_name nm) : sc_channel(nm){\n")
            out.print("\n\t}")

            //Interface methods //Taken from https://learnsystemc.com/basic/hierarchical_channel
            out.print(
                "\n\n\t//Interface methods (from https://learnsystemc.com/basic/hierarchical_channel)\n" +
                        "\tvoid write(const ${channelDataType!!.toCPPDataType()}& v) {\n" +
                        "    \tif (v != m_val) {\n" +
                        "      \t\tm_val = v;\n" +
                        "      \t\te.notify();\n" +
                        "    \t}\n" +
                        "  \t}\n" +
                        "  \n\tconst ${channelDataType!!.toCPPDataType()}& read() const {\n" +
                        "    \treturn m_val;\n" +
                        "  \t}\n" +
                        "  \n\tconst sc_event& value_changed_event() const {\n" +
                        "    \treturn e;\n" +
                        "  \t}\n" +
                        "  \n\tconst sc_event& default_event() const {\n" +
                        "    \treturn value_changed_event();\n" +
                        "  \t}\n" +
                        "  \n\tconst ${channelDataType!!.toCPPDataType()}& get_data_ref() const {\n" +
                        "    \treturn m_val;\n" +
                        "  \t}\n" +
                        "  \n\tbool event() const {\n" +
                        "    \treturn true;\n" +
                        "  \t}"
            )

            //Necessary private variables for the channel methods
            out.print(
                "\n\nprivate:\n" +
                        "  \n\t${channelDataType!!.toCPPDataType()} m_val = 0;\n" +
                        "  \tsc_event e;"
            )

            out.print("\n};\n#endif //_${channelName.uppercase()}_CLASS_H__")
        }
    }


    /**
     * Returns an Integer Value that tells to which element of the Bus' Target or Initiator Array the Port should connect.
     * @param portType The Type of the Port.
     */
    fun getPortBinding(portType: PortType): Int{
        when(portType){
            PortType.TARGET -> {
                if(boundInputPorts == inputPortsBindingLimit){
                    throw Exception("Cannot bind more Ports to $channelName than there are target sockets!")
                }else{
                    boundInputPorts ++
                    return boundInputPorts-1
                }
            }

            PortType.SOURCE -> {
                if(boundOutputPorts == outputPortsBindingLimit){
                    throw Exception("Cannot bind more Ports to $channelName than there are initiator sockets!")
                }else{
                    boundOutputPorts ++
                    return boundOutputPorts-1
                }
            }

            else -> throw SysMDFatalInternalError("No Port Binding supported for INOUT Ports!")
        }
    }


    fun printPortBinding(location: String, outWriter: PrintWriter) {
        /**
         * Here we bind TLM Ports with TLM Channels
         */

        when(this.channelType){
            ChannelType.PRIMITIVE, ChannelType.HIERARCHICAL -> {
                (this.inputPorts + this.outputPorts).forEach{ port ->
                    port.printStandardBinding(location, this, outWriter)
                }
            }

            ChannelType.TLM -> {
                this.inputPorts.forEach { port ->
                    port.printTLMBinding(location, this, outWriter)
                }
                this.outputPorts.forEach { port ->
                    port.printTLMBinding(location, this, outWriter)
                }
            }
        }


    }

}
