#include "Vehicle.h"


//	### Constructor Implementation ###
Vehicle::Vehicle(sc_core::sc_module_name nm,
		double var_noInit_Real_,
		int var_noInit_Integer_,
		string var_noInit_String_,
		bool var_noInit_Boolean_){

	var_noInit_Real = var_noInit_Real_;
	var_noInit_Integer = var_noInit_Integer_;
	var_noInit_String = var_noInit_String_;
	var_noInit_Boolean = var_noInit_Boolean_;

	var_Boolean = false;               
	var_String = "HALLO";              
	var_Real_Unit = 50.0;              
	var_Real_NoUnit = 50.0;            
	var_Integer_Unit = 50;             
	var_Integer_NoUnit = 3;            
}

//	### Functions ###
void Vehicle::set_attributes(){

}

void Vehicle::change_attributes(){

}

void Vehicle::initialize(){

}

void Vehicle::reinitialize(){

}

void Vehicle::processing(){

}

void Vehicle::ac_processing(){

}

