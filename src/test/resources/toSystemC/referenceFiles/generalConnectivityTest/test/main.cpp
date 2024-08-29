#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/wirelessDevice_CLASS.h"

int sc_main(int argc, char* argv[])
{




	//	### Modules ###
	wirelessDevice_CLASS wirelessDevice("wirelessDevice");

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
