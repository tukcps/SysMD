#ifndef _TLM_BUS_INITIATOR_H_
#define _TLM_BUS_INITIATOR_H_

#include <systemc>
#include <systemc-ams>
#include "tlm.h"
#include "tlm_utils/simple_initiator_socket.h"

using namespace sc_core;
using namespace sc_dt;
using namespace std;

struct TLM_bus_Initiator : sc_module{

	tlm_utils::simple_initiator_socket<TLM_bus_Initiator> socket;

	TLM_bus_Initiator(sc_core::sc_module_name nm) : socket("socket"){

	}; 

};

#endif //TLM_BUS_INITIATOR_H_