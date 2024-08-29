#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/a_CLASS.h"
#include "modules/x_CLASS.h"
#include "modules/y_CLASS.h"

int sc_main(int argc, char* argv[])
{




	//	### Channels ###
	sca_tdf::sca_signal<double> wire_x_y("wire_x_y");


	//	### Modules ###
	a_CLASS a("a");
	x_CLASS x("x");
	y_CLASS y("y");


	//	### Port binding ###
	y.y_in(wire_x_y);
	x.x_out(wire_x_y);

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
