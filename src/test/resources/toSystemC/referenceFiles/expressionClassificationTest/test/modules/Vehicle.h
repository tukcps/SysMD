#ifndef _VEHICLE_H_
#define _VEHICLE_H_

#include <systemc>
#include <systemc-ams>

#include <string>
using namespace std;
using namespace sc_core;


class Vehicle :  public sca_tdf::sca_module{

public:

	//	### Constants ###
	const double  const_Real_Unit_FakeRange = 200.0;    // Unit: m
	const double  const_Real_NoUnit_FakeRange = 200.0;   // Unit: 
	const double  const_Real_NoUnit = 5.0;              // Unit: 
	const double  const_Real_Unit = 5.0;                // Unit: s
	const int     const_Integer_Unit_FakeRange = 200;   // Unit: m
	const int     const_Integer_NoUnit_FakeRange = 200;   // Unit: 
	const int     const_Integer_NoUnit = 5;             // Unit: 
	const int     const_Integer_Unit = 5;               // Unit: m

	//Variables with values NOT defined in SysMD ###
	double var_noInit_Real;
	int var_noInit_Integer;
	string var_noInit_String;
	bool var_noInit_Boolean;

	//Variables with values defined in SysMD ###
	bool    var_Boolean;                                 // SysMD Value: false
	string  var_String;                                  // SysMD Value: "HALLO"
	double  var_Real_Unit;                               // Unit: m   ;SysMD Value: [0.0 .. 100.0]
	double  var_Real_NoUnit;                             // Unit:     ;SysMD Value: [0.0 .. 100.0]
	int     var_Integer_Unit;                            // Unit: m   ;SysMD Value: [0 .. 100]
	int     var_Integer_NoUnit;                          // Unit:     ;SysMD Value: [0 .. 5]

	//	### Constructor ###
	Vehicle(sc_core::sc_module_name nm,
		double var_noInit_Real_,
		int var_noInit_Integer_,
		string var_noInit_String_,
		bool var_noInit_Boolean_);

	//	### Functions ###
	void set_attributes();

	void change_attributes();

	void initialize();

	void reinitialize();

	void processing();

	void ac_processing();

};
#endif // _VEHICLE_H_
