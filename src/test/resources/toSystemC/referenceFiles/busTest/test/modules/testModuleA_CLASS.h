#ifndef _TESTMODULEA_CLASS_H_
#define _TESTMODULEA_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "compA.h"
#include "TLM_bus_Target.h"
#include "TLM_bus_Initiator.h"

class testModuleA_CLASS : public compA{

public:


	//	### Ports ###
	TLM_bus_Target inputA;
	TLM_bus_Initiator output1;
	TLM_bus_Initiator output2;


	//	### Constructor ###
	testModuleA_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _TESTMODULEA_CLASS_H_
