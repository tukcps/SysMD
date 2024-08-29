#ifndef _XYZ_H_
#define _XYZ_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class XYZ :  public sca_tdf::sca_module{

public:


	//	### Constructor ###
	XYZ(sc_core::sc_module_name nm);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _XYZ_H_
