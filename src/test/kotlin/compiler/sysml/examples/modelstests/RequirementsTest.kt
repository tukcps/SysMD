package compiler.sysml.examples.modelstests

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull


class RequirementsTest {

    //  @Ignore // satisfy is not yet implemented
    @Test
    fun testRequirements() = testSession("Requirements", "Parts") {
        loadSysMLv2("""
            package RequirementTest {
                constraint def C;
                constraint c : C;
                private import q::**;
                requirement def R {
                    assume constraint c1 : C;
                    require c;
                    doc /* */
                    requirement;
                    requirement def <'1'> A {
                        doc /* Text */
                        subject s;
                    }
                }
                requirement def R1 :> R { // maybe typo in SysML doc? --> added :> R 
                    require constraint c1 :>> c;
                }
                part p;
                part q {
                    requirement r : R;
                    satisfy r by p;
                    assert satisfy r by q;
                }
                
                requirement r1 : R1;
                not satisfy r1 by p;
                assert not satisfy r1 by q;
            }
        """)
        val r = global.resolve<Element>("RequirementTest::R")
        assertNotNull(r)
        assertNoIssues()
    }
}