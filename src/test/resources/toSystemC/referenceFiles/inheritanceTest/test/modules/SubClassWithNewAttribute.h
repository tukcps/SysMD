#ifndef _SUBCLASSWITHNEWATTRIBUTE_H_
#define _SUBCLASSWITHNEWATTRIBUTE_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "SuperClass.h"

class SubClassWithNewAttribute : public SuperClass{

public:


	//Variables with values NOT defined in SysMD ###
	double newAttribute;

	//	### Constructor ###
	SubClassWithNewAttribute(sc_core::sc_module_name nm,
		double newAttribute_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _SUBCLASSWITHNEWATTRIBUTE_H_
