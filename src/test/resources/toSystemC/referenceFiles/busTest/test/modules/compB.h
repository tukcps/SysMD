#ifndef _COMPB_H_
#define _COMPB_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class compB :  public sca_tdf::sca_module{

public:


	//Variables with values NOT defined in SysMD ###
	double input;

	//	### Constructor ###
	compB(sc_core::sc_module_name nm,
		double input_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _COMPB_H_
