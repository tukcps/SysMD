#ifndef _CLASSA_H_
#define _CLASSA_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class ClassA :  public sca_tdf::sca_module{

public:


	//Variables with values NOT defined in SysMD ###
	double outp;

	//	### Constructor ###
	ClassA(sc_core::sc_module_name nm,
		double outp_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _CLASSA_H_
