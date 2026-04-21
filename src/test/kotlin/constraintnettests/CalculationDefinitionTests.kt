package constraintnettests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*


class CalculationDefinitionTests {

    @Test
    fun calculationTestBasics() = testSession("Calculations") {
        loadSysMLv2("""
            calc def f {
                in x: ScalarValues::Real; 
                return result: ScalarValues::Real = x*2.0; 
            }
            attribute a: ScalarValues::Real = f(2.0); 
        """)
        solver.propagate()
        assertNoIssues()
        val f = global.resolve("f")
        val a = global.resolveVar("a")
        assertNotNull(f)
        assertNotNull(a)
        assertTrue(a.vectorQuantity.aadd().getRange().contains(4.0))
    }

    @Test
    fun userDefFunctionSimple() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            private import ISQ::*; 
            calc def Velocity {
                in v1 : ISQ::SpeedValue { :>> unit = "km/h"; }
                in v2 : SpeedValue;
                attribute a: SpeedValue = v1+v2;
                return result: SpeedValue  = a {:>> unit = "km/h";}  
            }
            attribute a: SpeedValue = 10.0 [km/h];
            attribute b: SpeedValue = 26.0 [km/h];
            attribute c: SpeedValue = Velocity(a, b);
            attribute c2: SpeedValue = Velocity(10.0 [m/s], b);
        """)
        assertNoIssues()
        solver.propagate()
        assertEquals(global.resolve("Velocity::v1")!!.member<Feature>()!!.direction, Feature.FeatureDirectionKind.IN)
        assertEquals(global.resolve("Velocity::v2")!!.member<Feature>()!!.direction, Feature.FeatureDirectionKind.IN)
        assertEquals(global.resolve("Velocity::a")!!.member<Feature>()!!.direction, null)
        assertEquals(global.resolve("Velocity::result")!!.member<Feature>()!!.direction, Feature.FeatureDirectionKind.OUT)
        assertTrue(10.0 in global.resolveVar("c")!!.range<Double>())
        assertTrue(62.0 in global.resolveVar("c2")!!.vectorQuantity.valuesIn("km/h")[0].asAadd().getRange())
    }

    @Test
    fun userDefFunctionTest() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            calc def Energy {
               in v : ISQ::SpeedValue;
               in m : ISQ::MassValue;
               return result : ISQ::EnergyValue = 0.5 * m * sqr(v);
            }
            attribute a: ISQ::SpeedValue = 10.0 [m/s];
            attribute a1: ISQ::SpeedValue = 2.0 [m/s];
            attribute b: ISQ::MassValue = 200.0 [kg];
            attribute b1: ISQ::MassValue = 20.0 [kg];
            attribute c: ISQ::EnergyValue = Energy(a,b);
            attribute c1: ISQ::EnergyValue = Energy(a1,b1);
         """)
        solver.propagate()
        assertNoIssues()
        assertTrue(10000.0 in global.resolveVar("c")!!.vectorQuantity.aadd().getRange())
        assertTrue(40.0 in global.resolveVar("c1")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestVector() = testSession("Calculations","ISQ") {
        loadSysMLv2("""
            private import ISQ::*; 
            calc def Distance {
                in a : LengthValue;
                in b : LengthValue;
                return result : LengthValue = sqrt(sqr(a[0]-b[0])+sqr(a[1]-b[1])+sqr(a[2]-b[2])); 
            }
            attribute a: LengthValue = (10.0,5.0,20.0) [m];
            attribute b: LengthValue = (0.0,-5.0,25.0) [m];
            attribute b1: LengthValue = (10.0,1.0,17.0) [m];
            attribute c: LengthValue = Distance(a,b);
            attribute c1: LengthValue = Distance(a,b1);
        """)
        solver.propagate()
        assertNoIssues()
        assertTrue(15.0 in global.resolveVar("c")!!.vectorQuantity.aadd().getRange())
        assertTrue(5.0 in global.resolveVar("c1")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestInteger() = testSession("Calculations") {
        loadSysMLv2("""
            calc def SumOfFourValues {
                in a : ScalarValues::Integer;
                in b : ScalarValues::Integer;
                in c : ScalarValues::Integer;
                in d : ScalarValues::Integer;
                attribute x: ScalarValues::Integer= a+b;
                attribute y: ScalarValues::Integer= c+d;
                return result : ScalarValues::Integer = x+y; 
            }
            attribute a: ScalarValues::Integer = 6;
            attribute b: ScalarValues::Integer = 7;
            attribute c: ScalarValues::Integer = 8;
            attribute d: ScalarValues::Integer = 9;
            attribute e: ScalarValues::Integer = SumOfFourValues(a,b,c,d);
            attribute e1: ScalarValues::Integer = SumOfFourValues(d,c,b,a);
        """)
        solver.propagate()
        assertNoIssues()
        assert(30 in global.resolveVar("e")!!.vectorQuantity.idd().getRange())
        assert(30 in global.resolveVar("e1")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDown() = testSession("Calculations", "Ranges") {
        loadSysMLv2("""
            calc def SumOfFourValues {
                in a : ScalarValues::Integer;
                in b : ScalarValues::Integer;
                in c : ScalarValues::Integer;
                in d : ScalarValues::Integer;
                attribute x: ScalarValues::Integer= a+b;
                attribute y: ScalarValues::Integer= c+d;
                return result : ScalarValues::Integer = x+y; 
            }
            attribute a1: ScalarValues::Integer = 6;
            attribute b1: ScalarValues::Integer = 7;
            attribute c1: ScalarValues::Integer = 8;
            attribute d1: ScalarValues::Integer;
            attribute d2: ScalarValues::Integer;
            attribute e1: Ranges::IntegerInRange = SumOfFourValues(a1,b1,c1,d1) {:>> range = "35..35";}
            attribute e2: Ranges::IntegerInRange = SumOfFourValues(8,7,6,d2) {:>> range = "40..40";}
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(14, global.resolveVar("d1")!!.vectorQuantity.idd().getRange().min)
        assertEquals(14, global.resolveVar("d1")!!.vectorQuantity.idd().getRange().max)
        assert(14 in global.resolveVar("d1")!!.vectorQuantity.idd().getRange())
        assertEquals(19, global.resolveVar("d2")!!.vectorQuantity.idd().getRange().min)
        assertEquals(19, global.resolveVar("d2")!!.vectorQuantity.idd().getRange().max)
        assert(19 in global.resolveVar("d2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDown2() = testSession("Calculations","Ranges") {
        loadSysMLv2("""
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Integer;
                    return result : ScalarValues::Integer = a; 
                 }
                 package TestModule {
                    attribute d1: ScalarValues::Integer; 
                    attribute e1: Ranges::IntegerInRange  = SumOfFourValues(d1) {:>> range = "13..13";}
                    attribute d2: ScalarValues::Integer;
                    attribute e2: Ranges::IntegerInRange = SumOfFourValues(d2) {:>> range = "17..17";}
                 }
             }    
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(13, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().min)
        assertEquals(13, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().max)
        assert(13 in global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange())
        assertEquals(17, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().min)
        assertEquals(17, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().max)
        assert(17 in global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestRealEvalDown2() = testSession("Calculations", "Ranges") {
        loadSysMLv2("""
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Real;
                    return result : ScalarValues::Real = a * 2.0; 
                 }
                 package TestModule {
                    attribute d1: ScalarValues::Real; 
                    attribute e1: Ranges::RealInRange = SumOfFourValues(d1) {:>> range = "28.0..28.0";} 
                    attribute d2: ScalarValues::Real; 
                    attribute e2: Ranges::RealInRange = SumOfFourValues(d2) {:>> range = "36.0..36.0";}
                 }
             }    
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(14.0, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(14.0, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assert(14.0 in global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange())
        assertEquals(18.0, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(18.0, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assert(18.0 in global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDownMultipleLevel() = testSession("Parts", "Calculations") {
        loadSysMLv2("""
            package Test {
                 calc def SumOfFourValues {
                    in a : ScalarValues::Integer;
                    in b : ScalarValues::Integer;
                    attribute x: ScalarValues::Integer= b;
                    attribute y: ScalarValues::Integer= x;
                    attribute z: ScalarValues::Integer= y;
                    return result : ScalarValues::Integer = a+z; 
                 }
                 part TestModule {
                    attribute x: ScalarValues::Integer = 2+4;
                    attribute y: ScalarValues::Integer;
                    // attribute b2: ScalarValues::Integer;
                    attribute e1: ScalarValues::Integer(13) = SumOfFourValues(x, y);
                    //attribute e2: ScalarValues::Integer(14) = SumOfFourValues(a,b2); 
                 }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(6, global.resolveVar("Test::TestModule::x")!!.vectorQuantity.idd().getRange().min)
        assertEquals(7, global.resolveVar("Test::TestModule::y")!!.vectorQuantity.idd().getRange().max)
        // assert(7 in global.resolveVar("Test::TestModule::b1")!!.vectorQuantity.idd().getRange())
        // assertEquals(8, global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange().min)
        // assertEquals(8, global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange().max)
        // assert(8 in global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun calcDefTestRealEvalDown() = testSession("Parts", "Calculations", "Ranges") {
        loadSysMLv2("""
             calc def SumOfFourValues {
                in a : ScalarValues::Real;
                in b : ScalarValues::Real;
                in c : ScalarValues::Real;
                in d : ScalarValues::Real;
                attribute x: ScalarValues::Real= a+b; 
                attribute y: ScalarValues::Real= c+d; 
                return result : ScalarValues::Real = x+y; 
             }
             part TestModule {
                attribute a: ScalarValues::Real = 6.0;
                attribute b: ScalarValues::Real = 7.0;
                attribute c1: ScalarValues::Real = 8.0;
                attribute c2: ScalarValues::Real = 10.0; 
                attribute d1: ScalarValues::Real; 
                attribute d2: ScalarValues::Real; 
                attribute e1: Ranges::RealInRange = SumOfFourValues(a,b,c1,d1) {:>> range = "35.0..35.0";} 
                attribute e2: Ranges::RealInRange = SumOfFourValues(a,b,c2,d2) {:>> range = "35.0..35.0";} 
             } 
             """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        assertEquals(14.0, global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(14.0, global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(14.0 in global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange())
        assertEquals(12.0, global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(12.0, global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(12.0 in global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestRealEvalDownMultipleLevel() = testSession("Parts", "Calculations", "Ranges") {
        loadSysMLv2("""
                 calc def SumOfFourValues {
                    in a : ScalarValues::Real;
                    in b : ScalarValues::Real;
                    attribute x: ScalarValues::Real= b;
                    attribute y: ScalarValues::Real= x;
                    attribute z: ScalarValues::Real= y;
                    return result : ScalarValues::Real = a+z; 
                 }
                 part TestModule {
                    attribute a: ScalarValues::Real = 6.0;
                    attribute b: ScalarValues::Real;
                    attribute e: Ranges::RealInRange = SumOfFourValues(a,b) {:>> range = "13.0..13.0";}
                    attribute h: ScalarValues::Real = 6.0;
                    attribute i: ScalarValues::Real;
                    attribute j: Ranges::RealInRange = SumOfFourValues(h,i) {:>> range = "14.0..14.0";}
                 } 
             """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        assertEquals(7.0, global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(7.0, global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(7.0 in global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange())
        assertEquals(8.0, global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(8.0, global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(8.0 in global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange())
    }


    @Test
    fun userDefFunctionTestEvalDown() = testSession("Calculations","ISQ") {
        loadSysMLv2("""
            package Test {   
                calc def Energy {
                    in v : ISQ::SpeedValue;
                    in m : ISQ::MassValue;
                    return result : ISQ::EnergyValue = 0.5 * m * sqr(v); 
                }
                package TestModule {
                    attribute a: ISQ::SpeedValue;
                    attribute b: ISQ::MassValue = 200.0 [kg];
                    attribute c: ISQ::EnergyValue = Energy(a,b) {:>> range = "10000..10000";}
                    attribute e: ISQ::SpeedValue;
                    attribute f: ISQ::MassValue = 800.0 [kg];
                    attribute g: ISQ::EnergyValue = Energy(e,f) {:>> range = "10000..10000";}
                }
            } 
        """)
        val a = global.resolve("Test::TestModule::a")
        assertNotNull(a)
        val b = global.resolve("Test::TestModule::b")
        assertNotNull(b)
        solver.propagate()
        assertNoIssues()
        assert(10000.0 in global.resolveVar("Test::TestModule::c")!!.vectorQuantity.aadd().getRange())
        //sqrt could be positive or negative value
        assertEquals(-10.0, global.resolveVar("Test::TestModule::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(10.0, global.resolveVar("Test::TestModule::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(10.0 in global.resolveVar("Test::TestModule::a")!!.vectorQuantity.aadd().getRange())

        assertEquals(-5.0, global.resolveVar("Test::TestModule::e")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(5.0, global.resolveVar("Test::TestModule::e")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(5.0 in global.resolveVar("Test::TestModule::e")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestString() = testSession("Calculations") {
        loadSysMLv2("""
             calc def ConvertASILtoInt{
                in i: ScalarValues::String;            
                return result: ScalarValues::Integer = if i=="QM" ? 1 else 0;           
            }              
            attribute a: ScalarValues::Integer = ConvertASILtoInt("QM"); 
        """)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals(1, global.resolveVar("a")!!.vectorQuantity.idd().getRange().min)
    }


    @Test
    fun userDefFunctionTestString2() = testSession("Calculations") {
        loadSysMLv2("""
            calc def ConvertIntToASIL {
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value";        
            } 
 
            attribute a: ScalarValues::String = ConvertIntToASIL(1);
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals("A", global.resolveVar("a")!!.vectorQuantity.value.asStrDD().toString())
    }

    @Test
    fun userDefFunctionTestString3() = testSession("Calculations") {
        loadSysMLv2("""
             calc def ConvertASILtoInt{
                in i : ScalarValues::String;            
                return result: ScalarValues::Integer = if i=="QM" ? 0 else if i=="A" ? 1 else if i=="B" ? 2 else if i=="C" ? 3 else if i=="D" ? 4 else 10;         
            }
            calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value";        
            }    
            
            calc def ASILDecomposition{
                in part1: ScalarValues::String;
                in part2: ScalarValues::String;
                attribute part1asInt: ScalarValues::Integer = ConvertASILtoInt(part1);
                attribute part2asInt: ScalarValues::Integer = ConvertASILtoInt(part2);
                return result: ScalarValues::String = ConvertIntToASIL(part1asInt+part2asInt); 
            }
            attribute a: ScalarValues::String = ASILDecomposition("A","QM");                
        """)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals("A", global.resolveVar("a")!!.vectorQuantity.value.asStrDD().toString())
    }

    @Test @Ignore //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown() = testSession("Calculations") {
        loadSysMLv2("""
             calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value".            
            } 
            
            attribute e: ScalarValues::Integer.
            attribute f: ScalarValues::String("QM") = ConvertIntToASIL(e).                 
             """
        )
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals(2, global.resolveVar("e")!!.vectorQuantity.idd().getRange().min)
    }

    @Test @Ignore //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown1() = testSession("Calculations") {
        loadSysMLv2("""
             calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String =  if i==0 ? "A" else if i==0 ? "A" else "B".            
            } 
            
            attribute e: ScalarValues::Integer;
            attribute f: ScalarValues::String("A") = ConvertIntToASIL(e);            
             """
        )
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals(0, global.resolveVar("e")!!.vectorQuantity.idd().getRange().min)
    }

    @Test @Ignore //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown4() = testSession {
        loadSysMLv2("""     
            attribute i: ScalarValues::Integer;
            attribute f: ScalarValues::String("A") = if i==0 ? "QM" else if i==1 ? "A" else "B";      
             """)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals(1, global.resolveVar("i")!!.vectorQuantity.idd().getRange().min)
    }

    @Test
    fun userDefFunctionHierachical() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
           private import ISQ::*;
           calc def Deceleration {
               in brakeForce: ISQ::ForceValue;
               in mass: ISQ::MassValue;
               return decel: ISQ::AccelerationValue = brakeForce / mass;
            }
            
            calc def StoppingDistance {
               in speed: ISQ::SpeedValue;
               in brakeForce: ISQ::ForceValue;
               in mass: ISQ::MassValue;
               attribute decel: ISQ::AccelerationValue = Deceleration(brakeForce, mass);
               return distance: ISQ::LengthValue = sqr(speed) / (2.0 * decel);
            }
            
            attribute speed: ISQ::SpeedValue = 25.0 [m/s];
            attribute brakeForce: ISQ::ForceValue = 8000.0 [N];
            attribute mass: ISQ::MassValue {:>> range = "1600..2000"; :>> unit = "kg";}
            
            attribute stopDist: ISQ::LengthValue = StoppingDistance(speed, brakeForce, mass) {
                :>> range = "0..70.0"; :>> unit = "m";
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(62.5, global.resolveVar("stopDist")!!.vectorQuantity.valuesIn("m")[0].asAadd().getRange().min,0.000001)
        assertEquals(70.0, global.resolveVar("stopDist")!!.vectorQuantity.valuesIn("m")[0].asAadd().getRange().max,0.000001)
        assertEquals(1600.0, global.resolveVar("mass")!!.vectorQuantity.valuesIn("kg")[0].asAadd().getRange().min,0.000001)
        assertEquals(1792.0, global.resolveVar("mass")!!.vectorQuantity.valuesIn("kg")[0].asAadd().getRange().max,0.000001)
    }
}
