#ifndef _SUBCLASSONLYINHERIT_H_
#define _SUBCLASSONLYINHERIT_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "SuperClass.h"

class SubClassOnlyInherit : public SuperClass{

public:


	//	### Constructor ###
	SubClassOnlyInherit(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _SUBCLASSONLYINHERIT_H_
