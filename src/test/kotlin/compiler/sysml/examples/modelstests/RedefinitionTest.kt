package compiler.sysml.examples.modelstests

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test


class RedefinitionTest {


    @Test
    fun testRedefinition() = testSession("SysMLLibraries") {
        loadSysMLv2(
            """
                package 'Redefinition Example' {
                
                    part def Vehicle {
                        part eng : Engine;
                    }
                    part def SmallVehicle :> Vehicle {
                        part smallEng : SmallEngine redefines eng;
                    }
                    part def BigVehicle :> Vehicle {
                        part bigEng : BigEngine :>> eng;
                    }
                
                    part def Engine {
                        part cyl : Cylinder[4..6];
                    }
                    part def SmallEngine :> Engine {
                        part redefines cyl[4];
                    }
                    part def BigEngine :> Engine {
                        part redefines cyl[6];
                    }
                
                    part def Cylinder;
                }
        """)
        assertNoIssues()
    }
}