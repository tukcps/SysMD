#include "a_CLASS.h"


//	### Constructor Implementation ###
a_CLASS::a_CLASS(sc_core::sc_module_name nm):b("b"),c("c"),wire_b_c("wire_b_c"){

	//	### Connect ports and channels ###
	c.c_in(wire_b_c);
	b.b_out(wire_b_c);

}

//	### Functions ###
void a_CLASS::set_attributes(){

}

void a_CLASS::change_attributes(){

}

void a_CLASS::initialize(){

}

void a_CLASS::reinitialize(){

}

void a_CLASS::processing(){

}

void a_CLASS::ac_processing(){

}

