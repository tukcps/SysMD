#ifndef _SUPERCLASS_H_
#define _SUPERCLASS_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class SuperClass :  public sca_tdf::sca_module{

public:


	//Variables with values NOT defined in SysMD ###
	double superAttribute;

	//	### Constructor ###
	SuperClass(sc_core::sc_module_name nm,
		double superAttribute_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _SUPERCLASS_H_
