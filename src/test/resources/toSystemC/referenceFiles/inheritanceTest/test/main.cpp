#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/SubClassWithOverride.h"
#include "modules/SubClassWithNewAttribute.h"
#include "modules/SubClassOnlyInherit.h"
#include "modules/SuperClass.h"
#include "modules/overrideSuperAttribute_CLASS.h"
#include "modules/addNewAttribute_CLASS.h"

int sc_main(int argc, char* argv[])
{




	//	### Modules ###
	SubClassWithOverride subClassWithOverride("subClassWithOverride");
	SubClassWithNewAttribute subClassWithNewAttribute("subClassWithNewAttribute");
	SubClassOnlyInherit subClassOnlyInherit("subClassOnlyInherit");
	SuperClass justInstantiateSuperClass("justInstantiateSuperClass");
	overrideSuperAttribute_CLASS overrideSuperAttribute("overrideSuperAttribute");
	addNewAttribute_CLASS addNewAttribute("addNewAttribute");

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
