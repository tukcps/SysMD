#ifndef _PARTD_CLASS_H_
#define _PARTD_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "ClassB.h"

class PartD_CLASS : public ClassB{

public:


	//	### Ports ###
	sca_tdf::sca_in<double> inp;


	//	### Constructor ###
	PartD_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _PARTD_CLASS_H_
