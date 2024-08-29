#ifndef _ACTUALPARTC_CLASS_H_
#define _ACTUALPARTC_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "DefinedPartA.h"

class actualPartC_CLASS : public DefinedPartA{

public:


	//	### Ports ###
	sca_tdf::sca_out<double> outp;


	//	### Constructor ###
	actualPartC_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _ACTUALPARTC_CLASS_H_
