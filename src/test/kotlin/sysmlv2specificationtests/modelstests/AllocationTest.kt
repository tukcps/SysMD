package sysmlv2specificationtests.modelstests

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue


class AllocationTest {

    @Test
    fun testAllocation() = testSession( "Allocations", "Parts") {
        loadSysMLv2("""
                package AllocationTest {
                    part def Logical {
                        part component;
                    }
                    
                    part def Physical {
                        part assembly {
                            part element;
                        }
                    }
                    
                    part l : Logical {
                        part :>> component;
                    }
                    part p : Physical {
                        part :>> assembly {
                            part :>> element;
                        }
                    }
                    
                    allocation def A;
                    
                    allocation def Logical_to_Physical :> A {
                        end logical : Logical;
                        end physical : Physical;
                    }
                    
                    allocation allocation1 : Logical_to_Physical allocate l to p;	
                    allocation allocation2 : Logical_to_Physical allocate (
                        logical ::> l,
                        physical ::> p
                    );
                
                    allocate l.component to p.assembly.element;
                }    
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}