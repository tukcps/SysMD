#ifndef _X_CLASS_H_
#define _X_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "XYZ.h"

class x_CLASS : public XYZ{

public:


	//	### Ports ###
	sca_tdf::sca_out<double> x_out;


	//	### Constructor ###
	x_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _X_CLASS_H_
