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
	sc_port<sc_signal_out_if<double> > ausgang1;
	sc_port<sc_signal_out_if<double> > ausgang2;
	sc_port<sc_signal_out_if<double> > ausgang3_Attribute;


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
