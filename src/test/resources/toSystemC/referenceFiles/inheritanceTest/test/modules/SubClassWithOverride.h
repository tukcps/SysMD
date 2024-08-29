#ifndef _SUBCLASSWITHOVERRIDE_H_
#define _SUBCLASSWITHOVERRIDE_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "SuperClass.h"

class SubClassWithOverride : public SuperClass{

public:


	//Variables with values NOT defined in SysMD ###
	double superAttribute;

	//	### Constructor ###
	SubClassWithOverride(sc_core::sc_module_name nm,
		double superAttribute_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _SUBCLASSWITHOVERRIDE_H_
