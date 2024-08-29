#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/A_CLASS.h"
#include "modules/B_CLASS.h"
#include "modules/C_CLASS.h"
#include "modules/D_CLASS.h"
#include "modules/X_CLASS.h"
#include "modules/Y_CLASS.h"

int sc_main(int argc, char* argv[])
{




	//	### Channels ###
	sca_tdf::sca_signal<double> if1("if1");
	sca_tdf::sca_signal<double> if2("if2");
	sca_tdf::sca_signal<double> if3("if3");
	sca_tdf::sca_signal<double> if4("if4");


	//	### Modules ###
	A_CLASS A("A");
	B_CLASS B("B");
	C_CLASS C("C");
	D_CLASS D_0("D_0"), D_1("D_1"), D_2("D_2"), D_3("D_3");
	X_CLASS X_0("X_0"), X_1("X_1"), X_2("X_2"), X_3("X_3");
	Y_CLASS Y("Y");


	//	### Port binding ###
	B.inp(if1);
	A.outp(if1);
	//B.inp(if2); //Conflict: Port is already bound to a Channel!
	//A.outp(if2); //Conflict: Port is already bound to a Channel!
	D_0.input(if3);
	D_1.input(if3);
	D_2.input(if3);
	D_3.input(if3);
	C.output(if3);
	Y.input(if4);
	X_0.output(if4);
	//X_1.output(if4); //Conflict: Channel already has one driver Port!
	//X_2.output(if4); //Conflict: Channel already has one driver Port!
	//X_3.output(if4); //Conflict: Channel already has one driver Port!

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
