#ifndef _C_CLASS_H_
#define _C_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class C_CLASS :  public sca_tdf::sca_module{

public:


	//	### Ports ###
	sca_tdf::sca_out<double> output;


	//	### Constructor ###
	C_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _C_CLASS_H_
