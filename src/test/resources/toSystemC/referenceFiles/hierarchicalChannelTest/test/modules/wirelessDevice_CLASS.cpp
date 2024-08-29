#include "wirelessDevice_CLASS.h"


//	### Constructor Implementation ###
wirelessDevice_CLASS::wirelessDevice_CLASS(sc_core::sc_module_name nm):transmitter("transmitter"),receiver("receiver"),interface_wire("interface_wire"),connector_wire("connector_wire"),connection_wire("connection_wire"){

	//	### Connect ports and channels ###
	receiver.eingang1(interface_wire);
	transmitter.ausgang1(interface_wire);
	receiver.eingang2(connector_wire);
	transmitter.ausgang2(connector_wire);
	receiver.eingang3_Attribute(connection_wire);
	transmitter.ausgang3_Attribute(connection_wire);

}

//	### Functions ###
void wirelessDevice_CLASS::set_attributes(){

}

void wirelessDevice_CLASS::change_attributes(){

}

void wirelessDevice_CLASS::initialize(){

}

void wirelessDevice_CLASS::reinitialize(){

}

void wirelessDevice_CLASS::processing(){

}

void wirelessDevice_CLASS::ac_processing(){

}

