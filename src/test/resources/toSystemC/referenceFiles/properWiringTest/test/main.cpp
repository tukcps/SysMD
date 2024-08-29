#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;

#include "modules/ClassA.h"
#include "modules/ClassB.h"
#include "modules/PartC_CLASS.h"
#include "modules/PartD_CLASS.h"
#include "modules/ClassA.h"
#include "modules/ClassB.h"
#include "modules/DefinedPartA.h"
#include "modules/DefinedPartB.h"
#include "modules/actualPartC_CLASS.h"
#include "modules/actualPartD_CLASS.h"

int sc_main(int argc, char* argv[])
{




	//	### Channels ###
	sca_tdf::sca_signal<double> legalWire1("legalWire1");
	sca_tdf::sca_signal<double> legalWire2("legalWire2");


	//	### Modules ###
	ClassA PartA("PartA");
	ClassB PartB("PartB");
	PartC_CLASS PartC("PartC");
	PartD_CLASS PartD("PartD");
	ClassA PartA_TrueInheritance("PartA_TrueInheritance");
	ClassB PartB_TrueInheritance("PartB_TrueInheritance");
	DefinedPartA actualPartA("actualPartA");
	DefinedPartB actualPartB("actualPartB");
	actualPartC_CLASS actualPartC("actualPartC");
	actualPartD_CLASS actualPartD("actualPartD");


	//	### Port binding ###
	PartD.inp(legalWire1);
	PartC.outp(legalWire1);
	actualPartD.inp(legalWire2);
	actualPartC.outp(legalWire2);

	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
