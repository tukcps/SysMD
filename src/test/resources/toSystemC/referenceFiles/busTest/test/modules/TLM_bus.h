#ifndef _TLM_BUS_H_
#define _TLM_BUS_H_
#include <systemc>
#include <systemc-ams>
#include "tlm.h"

#include "tlm_utils/simple_initiator_socket.h"
#include "tlm_utils/simple_target_socket.h"

using namespace sc_core;
using namespace sc_dt;
using namespace std;

template <int NR_OF_INITIATORS, int NR_OF_TARGETS>
struct TLM_bus : sc_module{

	tlm_utils::simple_target_socket_tagged<TLM_bus>*    target_socket[NR_OF_TARGETS];
	tlm_utils::simple_initiator_socket_tagged<TLM_bus>*    initiator_socket[NR_OF_INITIATORS];

	TLM_bus(sc_core::sc_module_name nm){

		for (unsigned int i = 0; i < NR_OF_INITIATORS; i++){
			char tag[20];
			sprintf(tag, "initiator_socket_%d", i);
			initiator_socket[i] = new tlm_utils::simple_initiator_socket_tagged<TLM_bus>(tag);
		}

		for (unsigned int i = 0; i < NR_OF_TARGETS; i++){
			char tag[20];
			sprintf(tag, "target_socket_%d", i);
			target_socket[i] = new tlm_utils::simple_target_socket_tagged<TLM_bus>(tag);
		}

	} //Constructor End
};

#endif // _TLM_BUS_H_