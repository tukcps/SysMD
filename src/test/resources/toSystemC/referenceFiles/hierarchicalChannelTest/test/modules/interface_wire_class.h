#ifndef __INTERFACE_WIRE_CLASS_H__
#define __INTERFACE_WIRE_CLASS_H__

#include <systemc>
#include <systemc-ams>
using namespace sc_core;

class interface_wire_class : public sc_channel, public sc_signal_inout_if<double>{

public:

	//Constructor
	interface_wire_class(sc_module_name nm) : sc_channel(nm){

	}

	//Interface methods (from https://learnsystemc.com/basic/hierarchical_channel)
	void write(const double& v) {
    	if (v != m_val) {
      		m_val = v;
      		e.notify();
    	}
  	}
  
	const double& read() const {
    	return m_val;
  	}
  
	const sc_event& value_changed_event() const {
    	return e;
  	}
  
	const sc_event& default_event() const {
    	return value_changed_event();
  	}
  
	const double& get_data_ref() const {
    	return m_val;
  	}
  
	bool event() const {
    	return true;
  	}

private:
  
	double m_val = 0;
  	sc_event e;
};
#endif //_INTERFACE_WIRE_CLASS_H__