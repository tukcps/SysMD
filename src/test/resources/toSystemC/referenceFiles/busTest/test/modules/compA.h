#ifndef _COMPA_H_
#define _COMPA_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class compA :  public sca_tdf::sca_module{

public:


	//Variables with values NOT defined in SysMD ###
	double output1;
	double output2;
	double inputA;

	//	### Constructor ###
	compA(sc_core::sc_module_name nm,
		double output1_,
		double output2_,
		double inputA_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _COMPA_H_
