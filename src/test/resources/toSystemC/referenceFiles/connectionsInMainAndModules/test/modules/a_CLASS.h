#ifndef _A_CLASS_H_
#define _A_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "b_CLASS.h"
#include "c_CLASS.h"

class a_CLASS :  public sca_tdf::sca_module{

public:


	//	### Modules ###
	b_CLASS b;
	c_CLASS c;


	//	### Constructor ###
	a_CLASS(sc_core::sc_module_name nm);

	//	### Channels  ###
	sca_tdf::sca_signal<double> wire_b_c;

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _A_CLASS_H_
