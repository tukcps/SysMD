#ifndef _ACTUALPARTD_CLASS_H_
#define _ACTUALPARTD_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "DefinedPartB.h"

class actualPartD_CLASS : public DefinedPartB{

public:


	//	### Ports ###
	sca_tdf::sca_in<double> inp;


	//	### Constructor ###
	actualPartD_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _ACTUALPARTD_CLASS_H_
