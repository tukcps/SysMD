#ifndef _RECEIVER_CLASS_H_
#define _RECEIVER_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class receiver_CLASS :  public sca_tdf::sca_module{

public:


	//	### Ports ###
	sc_port<sc_signal_in_if<double> > eingang1;
	sc_port<sc_signal_in_if<double> > eingang2;
	sc_port<sc_signal_in_if<double> > eingang3_Attribute;


	//	### Constructor ###
	receiver_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _RECEIVER_CLASS_H_
