#ifndef _A_CLASS_H_
#define _A_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class A_CLASS :  public sca_tdf::sca_module{

public:


	//	### Ports ###
	sca_tdf::sca_out<double> outp;


	//	### Constructor ###
	A_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _A_CLASS_H_
