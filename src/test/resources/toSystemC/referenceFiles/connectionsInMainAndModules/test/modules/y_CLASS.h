#ifndef _Y_CLASS_H_
#define _Y_CLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;

#include "XYZ.h"

class y_CLASS : public XYZ{

public:


	//	### Ports ###
	sca_tdf::sca_in<double> y_in;


	//	### Constructor ###
	y_CLASS(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _Y_CLASS_H_
