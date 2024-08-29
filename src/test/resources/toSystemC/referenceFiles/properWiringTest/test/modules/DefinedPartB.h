#ifndef _DEFINEDPARTB_H_
#define _DEFINEDPARTB_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class DefinedPartB :  public sca_tdf::sca_module{

public:


	//Variables with values NOT defined in SysMD ###
	double inp;

	//	### Constructor ###
	DefinedPartB(sc_core::sc_module_name nm,
		double inp_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _DEFINEDPARTB_H_
