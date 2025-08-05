package com.github.tukcps.sysmd.exports.systemCElements

import com.github.tukcps.sysmd.exceptions.SysMDFatalInternalError
import java.io.File
import java.io.PrintWriter

enum class PortType {
    SOURCE,
    TARGET,
    BIDIRECTIONAL,
}

/**
 * The abstract class for Ports
 */
class Port(val portName : String, val fullQualifiedName : String, val portType : PortType, val dataType: DataType, var module: Module?, val isInherited : Boolean) {

    val associatedChannels : MutableList<Channel> = mutableListOf() //The channels this port is bound to

    /**Tells if this port was originally defined as an Attribute (Expression) in the SysMD/SysMLv2 model*/
    var createdFromExpression = false

    private var isTLMInitiator : Boolean? = null                  //Tells if this is a TLM Initiator
    private var isTLMTarget : Boolean? = null                     //Tells if this is a TLM Target

    var isBoundToHierarchicalChannel : Boolean? = null      //Tells if this Port is bound to a Hierarchical channel (needed when declaring the Port)

    var alreadyBound = false

    /**Checks if the binding of this port would conflict with any of the given restrictions from SystemC Ports/Channels.
     *If a conflict is detected, the port binding is embraced in a Comment with additional info.
     *Conflicts checked:
     *  - Is Port already bound to another Channel
     *  - Does this Channel already have an input port
     *  */
    val checkConflict : (s : String, c : Channel) -> String = { s,c ->
        when {
            (alreadyBound) -> ("//$s //Conflict: Port is already bound to a Channel!")
            (this.portType == PortType.SOURCE && c.boundInputPorts > 0 && c.channelType != ChannelType.TLM) -> ("//$s //Conflict: Channel already has one driver Port!")
            else -> s
        }
    }


    /**
     * Sets this port to an initiator.
     * If this port has already been set to a target by another channel before, it throws an exception.
     */
    fun setTLMInitiator(){
        if(isTLMTarget == true){
            throw Exception("The Port has already been set to a Target! Cannot be redefined to an Initiator!")
        }else{
            isTLMInitiator = true
            isTLMTarget = false
        }
    }

    /**
     * Sets this port to a target.
     * If this port has already been set to an initiator by another channel before, it throws an exception.
     */
    fun setTLMTarget(){
        if(isTLMInitiator == true){
            throw Exception("The Port has already been set to a Initiator! Cannot be redefined to a Target!")
        }else{
            isTLMInitiator = false
            isTLMTarget = true
        }
    }

    fun isTarget() : Boolean?{
        return isTLMTarget
    }

    fun isInitiator() : Boolean?{
        return isTLMInitiator
    }

    /**
     * Creates the files for this Port.
     */
    fun createTLMPort(path : String){

        //check if Port is associated with one TLM Channel only!
        if(associatedChannels.size == 1){
            if(associatedChannels.first().channelType != ChannelType.TLM){
                throw Exception("Cannot create a TLM Port for $portName when its channel (${associatedChannels.first().channelName}) is not of Type TLM ")
            }
        }else{
            println(associatedChannels.size)
            throw Exception("TLM Port $portName is associated with no or multiple channels! A TLM Port must be bound to one channel only!")
        }

        File(if (isTLMTarget!!) "$path/TLM_${associatedChannels.first().channelName}_Target.h" else
            "$path/TLM_${associatedChannels.first().channelName}_Initiator.h").printWriter().use { out ->

                    out.print("#ifndef ${if (isTLMTarget!!) "_TLM_${associatedChannels.first().channelName.uppercase()}_TARGET_H_\n#define _TLM_${associatedChannels.first().channelName.uppercase()}_TARGET_H_" else
                        "_TLM_${associatedChannels.first().channelName.uppercase()}_INITIATOR_H_\n#define _TLM_${associatedChannels.first().channelName.uppercase()}_INITIATOR_H_"}" +

                        "\n\n#include <systemc>\n#include <systemc-ams>\n" +

                        "#include \"tlm.h\"\n${(if (isTLMTarget!!) "#include \"tlm_utils/simple_target_socket.h\"" else "#include \"tlm_utils/simple_initiator_socket.h\"")}\n\n" +
                        "using namespace sc_core;\nusing namespace sc_dt;\nusing namespace std;\n\n" +

                        (if (isTLMTarget!!) "struct TLM_${associatedChannels.first().channelName}_Target : sc_module{\n\n" else "struct TLM_${associatedChannels.first().channelName}_Initiator : sc_module{\n\n") +
                        (if (isTLMTarget!!) "\ttlm_utils::simple_target_socket<TLM_${associatedChannels.first().channelName}_Target> socket;\n\n" else "\ttlm_utils::simple_initiator_socket<TLM_${associatedChannels.first().channelName}_Initiator> socket;\n\n") +
                        "${if (isTLMTarget!!) "\tTLM_${associatedChannels.first().channelName}_Target(sc_core::sc_module_name nm) : socket(\"socket\"){\n\n\t};" else "\tTLM_${associatedChannels.first().channelName}_Initiator(sc_core::sc_module_name nm) : socket(\"socket\"){\n\n\t};"} " +
                        "\n\n};\n\n#endif //TLM_${associatedChannels.first().channelName.uppercase()}_${if (isTLMTarget!!) "TARGET_H_" else "INITIATOR_H_"}"
                    )
        }
    }


    /**Writes the standard Port binding for Ports connecting to signals or hierarchical channels**/
    fun printStandardBinding(location: String, channel: Channel, outWriter: PrintWriter) {
        this.module!!.actualModule.invoke().moduleUsages.forEach { usage ->
            if(usage.instanceLocation == location){
                if (usage.amount == 1) {
                    outWriter.println( "\t" + checkConflict("${(usage.instanceName)}.${this.portName}(${(channel.channelName)});", channel) )
                    if(portType == PortType.SOURCE) channel.boundInputPorts++
                    alreadyBound = true
                } else {
                    for (i in 0 until usage.amount) {
                        outWriter.println( "\t" + checkConflict("${(usage.instanceName)}_$i.${this.portName}(${(channel.channelName)});", channel) )
                        if(portType == PortType.SOURCE) channel.boundInputPorts++
                    }
                    alreadyBound = true
                }
            }
        }

    }

    /**Writes the TLM Port binding for Ports connecting to a TLM Bus**/
    fun printTLMBinding(location: String, channel: Channel, outWriter: PrintWriter) {
        this.module!!.actualModule.invoke().moduleUsages.forEach { usg ->
            if(usg.instanceLocation == location){
                if (usg.amount == 1) {
                    outWriter.println(
                        "\t${(usg.instanceName)}.${this.portName}.socket.bind(${
                            if (channel.inputPorts.size > 1) " *" else ""
                        }${(channel.channelName)}.${
                            when(portType) {
                               PortType.SOURCE -> "target_socket"
                               PortType.TARGET -> "initiator_socket"
                               PortType.BIDIRECTIONAL -> throw SysMDFatalInternalError("INOUT Ports cannot be used with TLM Channels!")
                            }
                        }${
                            if (channel.inputPorts.size > 1) "[${channel.getPortBinding(portType)}]" else ""
                        });"
                    )
                } else {
                    for (i in 0 until usg.amount) {
                        outWriter.println(
                            "\t${(usg.instanceName)}_$i.${this.portName}.socket.bind(${
                                if (channel.inputPorts.size > 1) " *" else ""
                            }${(channel.channelName)}.${
                                when(portType) {
                                    PortType.SOURCE -> "target_socket"
                                    PortType.TARGET -> "initiator_socket"
                                    PortType.BIDIRECTIONAL -> throw SysMDFatalInternalError("INOUT Ports cannot be used with TLM Channels!")
                                }
                            }${
                                if (channel.inputPorts.size > 1) "[${channel.getPortBinding(portType)}] " else ""
                            });"
                        )
                    }
                }
            }
        }
    }


}
