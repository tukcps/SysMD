#include "SubClassWithOverride.h"


//	### Constructor Implementation ###
SubClassWithOverride::SubClassWithOverride(sc_core::sc_module_name nm,
		double superAttribute_):SuperClass( nm ){

	superAttribute = superAttribute_;
}

//	### Functions ###
void SubClassWithOverride::set_attributes(){

}

void SubClassWithOverride::change_attributes(){

}

void SubClassWithOverride::initialize(){

}

void SubClassWithOverride::reinitialize(){

}

void SubClassWithOverride::processing(){

}

void SubClassWithOverride::ac_processing(){

}

