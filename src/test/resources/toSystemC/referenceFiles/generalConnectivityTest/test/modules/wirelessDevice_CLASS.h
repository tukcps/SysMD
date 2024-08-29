#ifndef _WIRELESSDEVICE_CLASS_H_
#define _WIRELESSDEVICE_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "transmitter_CLASS.h"
#include "receiver_CLASS.h"

class wirelessDevice_CLASS :  public sca_tdf::sca_module{

public:


	//	### Modules ###
	transmitter_CLASS transmitter;
	receiver_CLASS receiver;


	//	### Constructor ###
	wirelessDevice_CLASS(sc_core::sc_module_name nm);

	//	### Channels  ###
	sca_tdf::sca_signal<double> interface_wire;
	sca_tdf::sca_signal<double> connector_wire;
	sca_tdf::sca_signal<double> connection_wire;

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _WIRELESSDEVICE_CLASS_H_
