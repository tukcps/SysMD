#include <systemc>
#include <systemc-ams>
#include <string>

using namespace std;


int sc_main(int argc, char* argv[])
{




	//	### Constants ###
	const double  const_Real_Unit_FakeRange = 200.0;    // Unit: m
	const double  const_Real_NoUnit_FakeRange = 200.0;   // Unit: 
	const double  const_Real_NoUnit = 5.0;              // Unit: 
	const double  const_Real_Unit = 5.0;                // Unit: s
	const int     const_Integer_Unit_FakeRange = 200;   // Unit: m
	const int     const_Integer_NoUnit_FakeRange = 200;   // Unit: 
	const int     const_Integer_NoUnit = 5;             // Unit: 
	const int     const_Integer_Unit = 5;               // Unit: m

	//	### Variables with values defined in SysMD ###
	bool   var_Boolean = false;                    // SysMD Value: false
	string var_String = "HALLO";                   // SysMD Value: "HALLO"
	double var_Real_Unit = 50.0;                   // Unit: m   ;SysMD Value: [0.0 .. 100.0]
	double var_Real_NoUnit = 50.0;                 // Unit:     ;SysMD Value: [0.0 .. 100.0]
	int    var_Integer_Unit = 50;                  // Unit: m   ;SysMD Value: [0 .. 100]
	int    var_Integer_NoUnit = 3;                 // Unit:     ;SysMD Value: [0 .. 5]

	//	### Variables with values NOT defined in SysMD ###
	double var_noInit_Real = SPECIFY_VALUE    
	int    var_noInit_Integer = SPECIFY_VALUE 
	string var_noInit_String = SPECIFY_VALUE  
	bool   var_noInit_Boolean = SPECIFY_VALUE 
	sca_util::sca_trace_file* tr = sca_util::sca_create_vcd_trace_file("output");


	sca_close_vcd_trace_file(tr);

	sc_core::sc_start();


	return 0;
}
