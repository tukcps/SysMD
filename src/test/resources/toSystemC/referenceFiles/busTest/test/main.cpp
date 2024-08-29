#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/testModuleA_CLASS.h"
#include "modules/testModuleB_CLASS.h"
#include "modules/compA.h"
#include "modules/compB.h"
#include modules/"TLM_bus.h"

int sc_main(int argc, char* argv[])
{




	//	### Channels ###
	TLM_bus<3,4> bus("bus");


	//	### Modules ###
	testModuleA_CLASS testModuleA_0("testModuleA_0"), testModuleA_1("testModuleA_1");
	testModuleB_CLASS testModuleB("testModuleB");
	compA someA("someA");
	compB someB("someB");


	//	### Port binding ###
	testModuleB.input.socket.bind( *bus.initiator_socket[0]);
	testModuleA_0.inputA.socket.bind( *bus.initiator_socket[1] );
	testModuleA_1.inputA.socket.bind( *bus.initiator_socket[2] );
	testModuleA_0.output1.socket.bind( *bus.target_socket[0] );
	testModuleA_1.output1.socket.bind( *bus.target_socket[1] );
	testModuleA_0.output2.socket.bind( *bus.target_socket[2] );
	testModuleA_1.output2.socket.bind( *bus.target_socket[3] );

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
