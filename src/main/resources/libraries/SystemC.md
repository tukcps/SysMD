---
name: Signals
title: Signal models in particular for export to SystemC
maintainer: RPTU Kaiserslautern, Chair of Cyber-Physical Systems
version: 2.12
usage: ScalarValues
website: https://cps.cs.uni-kl.de
---
This file defines some classes for the export to SystemC.

# 1. Data types
We rely on ScalarValues data types like Real, Integer, Boolean
that we map on SystemC types.


The mapping of data types is like the following:

| SysMLv2 | SystemC |
|---------| ------- |
| Real    | double    |
| Integer | int     |
| Boolean | bool    |
| String  | string    |



# 2. Attributes mapping
Attributes can be mapped to constants, variables, and ports.
The mapping from attributes to ports is explained later.



## 2.1 Constants
Attributes that have one precise value or have equal min/max values are mapped to
constants.\
In addition, information about its unit is given in a subsequent annotation.
<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part Clock{ 
    attribute freq: Real [MHz] = 1000.0 MHz;
}
```

</td>
<td>

```C++
const double freq = 1000.0;  // MHz 
```
</td>
</tr>

<tr>
<td> 

```
part Clock{ 
    attribute freq: Real [MHz] = [1000.0 .. 1000.0] MHz;
}
```
</td>
<td>

```C++
const double freq = 1000.0;  // MHz 
```

</td>
</tr>
</table>


## 2.2 Variables
Attributes that have different min/max values are mapped to variables.\
The variable is declared in the Header File with annotations about its unit and
the range that was defined in the SysMLv2 model.\
The variable is instantiated in the source file with the center value of the
range (in this case 1000.0).
<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part Clock{
    attribute freq: Real (900.0 .. 1100.0) [MHz];
}
```

</td>
<td>

```C++
Header File:
    double freq;   // MHz [900.0 .. 1100.0]
    
Source File:
    freq = 1000.0; // MHz
```
</td>
</tr>
</table>

Attributes for which no value is specified in the SysMLv2 model will be initialized in the modules' constructor.\
The corresponding parameter that holds the value is named after the original attribute followed by an underscore.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part Clock{
      attribute freq: Real;
}
```

</td>
<td>

```C++
Header File:
    double freq;

    //Constructor defintion
    Clock(sc_core::sc_module_name nm, double freq_)
   
Source File:
    //Constructor implementation
    Clock:Clock(sc_core::sc_module_name nm, double freq_){
        freq = freq_;
    }
```
</td>
</tr>
</table>

# 3. Parts/Modules
SysMLv2 part definitions will be mapped to SystemC modules for which Source and Header files are created.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part def Sensor;
```

</td>
<td>

```C++
SC_MODULE(Sensor){
    ...
}
```
</td>
</tr>
</table>

SysMLv2 part usages will create actual instances of modules.
The type of the module instance is inferred from the type of the SysMLv2 part.
How many instances are created is defined by the upper bound of the part usage.
If no further specification is given, the standard number of instances is 1.
In this example, a part is defined which uses the part definition of *Sensor* from above.
<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part sensor : [1 .. 2] Sensor;
```

</td>
<td>

```C++
Sensor sensor_0("sensor_0"), sensor_1("sensor_1");
```
</td>
</tr>
</table>

If a part usage does not specify any type, or it adds or changes elements so that the original type cannot be used, 
it will create an own class using the name of the part usage suffixed by "_CLASS."

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part amplifier;
```

</td>
<td>

```C++
amplifier_CLASS amplifier("amplifier");
```
</td>
</tr>
</table>

Where the modules are instantiated in the SystemC project is derived from the structure of the SysMLv2 model.
If a part usage is declared globally, the corresponding module will be instantiated in the main.cpp.
If a part usage is a subpart, it will be instantiated inside the parent module.


<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part globalPart;

part parentPart{
    part subPart;
}
```

</td>
<td>

```C++
main.cpp:
    globalPart_CLASS globalPart("globalPart");
    
parentPart_CLASS.h:
    subPart_CLASS subPart;
```
</td>
</tr>
</table>


# 4. Relations/Channels
If a relation (in this case, connections and interfaces are meant which are both based on the KerML relation) is defined, 
it will be mapped to a SystemC channel.\
The channel adopts the name of the relation. A relation can only be mapped to a SystemC channel if it uses one of the available types explained in Section 4.2.
In the following examples, the type *Signal* is used.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
connection def Signal;
connection dataline1 : Signal = source to target;

interface def Signal;
interface dataline2 : Signal = source to target;
```

</td>
<td>

```C++
sca_tdf::sca_signal<double> dataline1;
sca_tdf::sca_signal<double> dataline2;
```
</td>
</tr>
</table>

The data type used by the channel is determined by the data types assigned to the source and target ports
in the SysMLv2 model.\
For TLM channels, the data types are ignored.

To map a relation to a SystemC channel, 
it is important to use supported endpoints which are SysMLv2 ports and attributes.
Any other type of element that is stated in the source or target list of a relation is not supported.
Additionally, the structure of the SysMLv2 relation must match the supported structure of the SystemC channel it will be mapped to.
This is further explained in section 4.2.
In the generated SystemC files, the ports will already be bound to the respective channel.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
connection dataline : Signal = 
    transmitter::output to receiver::input;
```

</td>
<td>

```C++
transmitter.output(dataline);
receiver.input(dataline);
```
</td>
</tr>
</table>




##  4.1 Ports
While a SysMLv2 port will always be mapped to a SystemC port, a SysMLv2 attribute
will only map to a port if it is used as endpoint in a relation. This difference is shown in the example below.
The attribute *output* is used in a relation and therefore mapped to a port while the attribute *frequency* is mapped to a variable.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
part transmitter{
    attribute output : Real;
    attribute frequency : Real;
}

part receiver{
    in port input;
}
 
connection dataline: Signal = from transmitter::output
                                to receiver::input;
```

</td>
<td>

```C++
SC_MODULE(transmitter_CLASS){
    sca_tdf::sca_out<double> output;
    double frequency;
}

SC_MODULE(receiver_CLASS){
    sca_tdf::sca_in<double> input;
}
```
</td>
</tr>
</table>

The data type used by the ports is adopted from the SysMLv2 model.

ATTENTION: Source and Target Properties that use different data types create a conflict and therefore
no channel can be created for them! This does not count for TLM channels, as there data types are ignored.

##  4.2 Channel Types
A relation will only be mapped to a SystemC channel if it uses one of the following types:
- Signal (Simple SystemC AMS signals are used)
- ComplexSignal (A hierarchical channel using the "sc_signal_inout_if" interface is created)
- Bus (A simple TLM bus model is created)

\
These types are explained in detail in the following subsections.


### 4.2.1 Relationship: Signal
If the user specifies a relation as a "Signal" a simple SystemC AMS signal using the correct data type will be created.\
See chapter 4. for an example.


### 4.2.3 Relationship: ComplexSignal
If the user specifies a relation as a "ComplexSignal" a hierarchical channel which
implements the "sc_signal_inout_if" interface is created.\
For the hierarchical channel, a header file is created.
A hierarchical channel always adopts the name of the original SysMLv2 relationship followed
a "_class" suffix.
The header file contains a constructor and the functions required by the implemented interface.
```   
SystemC
------------------------------------------------------------------------------------------   
    Excerpt from simple_hierarchical_channel_class.h:
    
        class simple_hierarchical_channel_class : public sc_channel, public sc_signal_inout_if<double>{

            //Constructor
            simple_hierarchical_channel_class(sc_module_name nm) : sc_channel(nm){}
        
            //Interface methods 
            void write(const double& v) {...}
          
            const double& read() const {...}
          
            const sc_event& value_changed_event() const {...}
          
            const sc_event& default_event() const {...}
          
            const double& get_data_ref() const {...}
          
            bool event() const {...}
	
};
```
The data type used by the hierarchical channel is determined by the data types assigned to the source and target elements
in the SysMLv2 model. The data type is passed to the generic of the interface and is also assigned
to the functions. The channel already contains functional code and is ready to use.
The code can be modified to achieve the desired behavior.

The ports that connect to a hierarchical channel are sc_ports using the
"sc_signal_out_if" and "sc_signal_in_if" respectively.
```
SystemC
------------------------------------------------------------------------------------------   
	sc_port<sc_signal_out_if<double> > output;
	sc_port<sc_signal_in_if<double> > input;
```
The data type used by the ports is adopted from the SysMLv2 model.


### 4.2.3 Relationship: Bus
If the user specifies a relation as "Bus" a simple model of a TLM bus is created.
The model for a bus always consists of following files:
- A header file for the bus
- A header file for the initiator ports that connect to the bus
- A header file for the target ports that connect to the bus

For better explanation, we imagine the definition of a following relation:
```
SysMLv2                                   
------------------------------------------------------------------------------------------                        
    connection example_bus : Bus = from ModuleA:output,
                                        ModuleC:output_1,
                                        ModuleC:output_2
                                              to
                                        ModuleA:input,
                                        ModuleB:input_1,
                                        ModuleB:input_2;

    
```
The structure of the bus can be visualized like this:
```
                                                ######################
                                                #    example_bus     #
                                                # ------------------ #
                ModuleA:                        #                    #
    output(TLM_example_bus_initiator) ------->  #target_socket       #
    input (TLM_example_bus_target)    <-------  #initiator_socket    #
                                                #                    #
                                                #                    #                   ModuleB:
                                                #    initiator_socket# ------> input_1 (TLM_example_bus_target)
                                                #    initiator_socket# ------> input_2 (TLM_example_bus_target)
                                                #                    #
                ModuleC:                        #                    #
    output_1(TLM_example_bus_initiator) ------> #target_socket       #
    output_2(TLM_example_bus_initiator) ------> #target_socket       #
                                                #                    #
                                                ######################

    
```
The sources of a relation are mapped to initiators that send a request to the bus while
targets of a relation are targets that only receive requests from the bus.


For the bus, a header file is created.\
The name of a bus always starts with "TLM_" followed by the name of the relationship defined in the SysMLv2 model.
```   
SystemC
------------------------------------------------------------------------------------------   
    Excerpt from TLM_example_bus.h:  
    
        struct TLM_example_bus : sc_module{
        
            tlm_utils::simple_target_socket_tagged<TLM_example_bus>*    target_socket[NR_OF_TARGETS];
            tlm_utils::simple_initiator_socket_tagged<TLM_example_bus>*    initiator_socket[NR_OF_INITIATORS];	       	     
	        
	    ...  initialiazation of the sockets ...
	       
        }
```

The header file for the bus starts with the declaration of the initiator and target sockets and
code for their initialization and has to be complemented with code for the routing of requests.

In addition, a header file for the initiator and target ports that are used inside the connecting modules
are created.\
The naming of these files always starts with "TLM_" followed by the name of
the channel and ends with the according type of the port.
```   
SystemC
------------------------------------------------------------------------------------------  
    TLM_example_bus_Initiator.h:
     
        struct TLM_example_bus_Initiator : sc_module{
        
            tlm_utils::simple_initiator_socket<TLM_example_bus_Initiator> socket;
        
            TLM_example_bus_Initiator(sc_core::sc_module_name nm) : socket("socket"){}; 
        };
```    
``` 
SystemC
------------------------------------------------------------------------------------------         
    TLM_example_bus_Target.h:
    
        struct TLM_example_bus_Target : sc_module{
    
            tlm_utils::simple_target_socket<TLM_example_bus_Target> socket;
    
            TLM_example_bus_Target(sc_core::sc_module_name nm) : socket("socket"){}; 
        };
``` 
The ports of the modules are automatically connected to the respective
initiators/target sockets of the bus.\
The ports have to be complemented with code for the logic of
initiating or receiving requests.

### 5. Requirements/Testbenches
To aid in the validation process of requirements, the template generation process also includes the setup of a
test structure based on basic test-bench templates. The aim of these templates is not to provide any functional code 
but propagate important information from the SysMLv2 requirements down to SystemC and support back-propagation of
simulation results back to the SysMLv2 model.

For every SysMLv2 requirement, a corresponding test-bench file is created that uses the name of the requirement
suffixed by "_TB" (abbreviation for test-bench) and the .cpp file ending.
The test-bench contains the sc_main() function and an outline, starting with information about invariants and requirements
down to the actual simulation part and post-processing.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
requirement example_requirement {
    ...
}
```

</td>
<td>

```C++
example_requirement_TB.cpp:

    int sc_main(int argc, char* argv[]){
    
        // --- INVARIANTS ---
    
        // --- REQUIREMENTS ---
        //DUT: Amplifier
    
        // --- STIMULI ---
        //Here you define Signals and Inputs that are used as stimuli for the DUT
    
        // --- DUT ---
        //This is the device that is under test
    
        Amplifier dut("dut");
    
        // --- MONITORING and POST-PROCESSING ---
        //Monitor relevant signals and calculate results
    
        // --- RESULT WRITING ---
        //Feed your results to the ResultWriter so that they can be imported in the SysMD Notebook.
    
        ResultWriter rw;
        rw.writeResultFile();
    
    
    return 0;
    }
```
</td>
</tr>
</table>

In the following, the mapping of individual SysMLv2 elements that are relevant
for the test-bench templates are explained.

An asserted constraint will be used in the INVARIANTS section of the test-bench.
Here, the constraint is given as a comment, providing the user with invariants that must be considered when
designing the simulation.

<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
assert temperatureRange { (temp >= -30.0 [°C]) & (temp <= 50.0 [°C]) }
```

</td>
<td>

```C++
// --- INVARIANTS ---
//Invariant 1: temperatureRange -> (temp >= -30.0 [°C]) & (temp <= 50.0 [°C])
```
</td>
</tr>
</table>

A required constraint is listed under the REQUIREMENTS section in the template.
<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
require minGain {
    amplifier::gain >= 20.0 [dB]
    }
```

</td>
<td>

```C++
// --- REQUIREMENTS ---
//Requirement minGain requires that amplifier::gain >= 20.0 [dB]
```
</td>
</tr>
</table>

A subject reference will be listed in the REQUIREMENTS and the DUT section.
In the first section, a comment is given that informs about the module that is under test.
In the DUT section, the dut is actually instantiated with the respective module type.
<table>
<tr>
<td> SysMLv2 </td>
<td> SystemC </td>
</tr>
<tr>

<td>

```
subject amp references amplifier;
```

</td>
<td>

```C++
// --- REQUIREMENTS ---
//DUT: Amplifier

...

// --- DUT ---
//This is the device that is under test
Amplifier dut("dut");
```
</td>
</tr>
</table>
