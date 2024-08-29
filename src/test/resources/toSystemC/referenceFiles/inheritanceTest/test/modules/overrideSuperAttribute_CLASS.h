#ifndef _OVERRIDESUPERATTRIBUTE_CLASS_H_
#define _OVERRIDESUPERATTRIBUTE_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "SuperClass.h"

class overrideSuperAttribute_CLASS : public SuperClass{

public:


	//Variables with values NOT defined in SysMD ###
	double superAttribute;

	//	### Constructor ###
	overrideSuperAttribute_CLASS(sc_core::sc_module_name nm,
		double superAttribute_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _OVERRIDESUPERATTRIBUTE_CLASS_H_
