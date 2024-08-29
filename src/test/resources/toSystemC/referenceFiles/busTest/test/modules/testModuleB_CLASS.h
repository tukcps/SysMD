#ifndef _TESTMODULEB_CLASS_H_
#define _TESTMODULEB_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "compB.h"
#include "TLM_bus_Target.h"

class testModuleB_CLASS : public compB{

public:


	//	### Ports ###
	TLM_bus_Target input;


	//	### Constructor ###
	testModuleB_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _TESTMODULEB_CLASS_H_
