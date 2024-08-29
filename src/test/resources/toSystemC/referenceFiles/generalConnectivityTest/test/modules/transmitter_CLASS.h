#ifndef _TRANSMITTER_CLASS_H_
#define _TRANSMITTER_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class transmitter_CLASS :  public sca_tdf::sca_module{

public:


	//	### Ports ###
	sca_tdf::sca_out<double> ausgang1;
	sca_tdf::sca_out<double> ausgang2;
	sca_tdf::sca_out<double> ausgang3_Attribute;


	//	### Constructor ###
	transmitter_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _TRANSMITTER_CLASS_H_
