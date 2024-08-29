#ifndef _B_CLASS_H_
#define _B_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class B_CLASS :  public sca_tdf::sca_module{

public:


	//	### Ports ###
	sca_tdf::sca_in<double> inp;


	//	### Constructor ###
	B_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _B_CLASS_H_
