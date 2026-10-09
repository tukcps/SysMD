package constraintnettests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadSysMLv2
import kotlin.test.*


class CalculationDefinitionTests {

    @Test
    fun unboundedPercentAttributeStaysUnbounded() = testSession("Calculations", "ISQ", "Attributes") {
        loadSysMLv2("""
            private import ISQ::*;
            calc def calcRFP {
                in attribute DC: DimensionOneValue {:>> unit = "%";}
                return RFP: DimensionOneValue = 1.0 - DC {:>> unit = "%";}
            }
            attribute x: DimensionOneValue {:>> unit = "%";}
            attribute y: DimensionOneValue = x + 1.0;
        """, Runlevel.ALL)
        assertNoIssues()
        for (path in listOf("calcRFP::DC", "calcRFP::RFP", "x", "y")) {
            val range = solver.getVariable(path)!!.vectorQuantity.aadd().getRange()
            assertFalse(range.isEmpty(), "$path must not be empty")
            assertTrue(range.min <= -Double.MAX_VALUE && range.max >= Double.MAX_VALUE, "$path must stay unbounded")
        }
    }

    @Test
    fun calculationTestBasics() = testSession("Calculations") {
        loadSysMLv2("""
            calc def f {
                in x: ScalarValues::Real; 
                return result: ScalarValues::Real = x*2.0; 
            }
            attribute a: ScalarValues::Real = f(2.0); 
        """, Runlevel.ALL)
        assertNoIssues()
        val f = global.resolve("f")
        val a = solver.variable("a")
        assertNotNull(f)
    }

    @Test
    fun userDefFunctionSimple() = testSession("Calculations", "ISQ", "Attributes") {
        loadSysMLv2("""
            private import ISQ::*; 
            calc def Velocity {
                in v1 : ISQ::SpeedValue { :>> range = (*..*) [km/h]; }
                in v2 : SpeedValue;
                attribute a: SpeedValue( *..* [km/h]) = v1+v2;
                return result: SpeedValue(* ..* [km/h]) = a;
            }
            attribute a: SpeedValue = 10.0 [km/h];
            attribute b: SpeedValue = 26.0 [km/h];
            attribute c: SpeedValue = Velocity(a, b);
            attribute c2: SpeedValue = Velocity(10.0 [m/s], b);
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertEquals(Feature.FeatureDirectionKind.IN, global.resolve("Velocity::v1")!!.member<Feature>()!!.direction)
        assertEquals(Feature.FeatureDirectionKind.IN, global.resolve("Velocity::v2")!!.member<Feature>()!!.direction)
        assertNull(global.resolve("Velocity::a")!!.member<Feature>()!!.direction)
        assertEquals(Feature.FeatureDirectionKind.OUT, global.resolve("Velocity::result")!!.member<Feature>()!!.direction)
        assertBounds(10.0, solver.variable("c"))
        assertBounds(62.0, solver.variable("c2"), unit = "km/h")
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
         """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(10000.0, solver.variable("c"))
        assertBounds(40.0, solver.variable("c1"))
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
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(15.0, solver.variable("c"))
        assertBounds(5.0, solver.variable("c1"))
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
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(30L, solver.variable("e"))
        assertBounds(30L, solver.variable("e1"))
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
            attribute e1: Ranges::IntegerInRange = SumOfFourValues(a1,b1,c1,d1) {:>> range = 35..35;}
            attribute e2: Ranges::IntegerInRange = SumOfFourValues(8,7,6,d2) {:>> range = 40..40;}
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(14L, solver.variable("d1"))
        assertBounds(19L, solver.variable("d2"))
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
                    attribute e1: Ranges::IntegerInRange  = SumOfFourValues(d1) {:>> range = 13..13;}
                    attribute d2: ScalarValues::Integer;
                    attribute e2: Ranges::IntegerInRange = SumOfFourValues(d2) {:>> range = 17..17;}
                 }
             }    
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(13L .. 13L,solver.variable("Test::TestModule::d1"))
        assertBounds(17L, solver.variable("Test::TestModule::d2"))
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
                    attribute e1: Ranges::RealInRange = SumOfFourValues(d1) {:>> range = 28.0..28.0;} 
                    attribute d2: ScalarValues::Real; 
                    attribute e2: Ranges::RealInRange = SumOfFourValues(d2) {:>> range = 36.0..36.0;}
                 }
             }    
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(14.0, solver.variable("Test::TestModule::d1"))
        assertBounds(18.0, solver.variable("Test::TestModule::d2"))
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
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(6L .. 6L, solver.variable("Test::TestModule::x"))
        assertBounds(7L .. 7L, solver.variable("Test::TestModule::y"))
        // assert(7 in solver.variable("Test::TestModule::b1").vectorQuantity.idd().getRange())
        // assertEquals(8, solver.variable("Test::TestModule::b2").vectorQuantity.idd().getRange().min)
        // assertEquals(8, solver.variable("Test::TestModule::b2").vectorQuantity.idd().getRange().max)
        // assert(8 in solver.variable("Test::TestModule::b2").vectorQuantity.idd().getRange())
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
                attribute e1: Ranges::RealInRange = SumOfFourValues(a,b,c1,d1) {:>> range = 35.0..35.0;} 
                attribute e2: Ranges::RealInRange = SumOfFourValues(a,b,c2,d2) {:>> range = 35.0..35.0;} 
             } 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(14.0, solver.variable("TestModule::d1"))
        assertBounds(12.0, solver.variable("TestModule::d2"))
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
                    attribute e: Ranges::RealInRange = SumOfFourValues(a,b) {:>> range = 13.0..13.0;}
                    attribute h: ScalarValues::Real = 6.0;
                    attribute i: ScalarValues::Real;
                    attribute j: Ranges::RealInRange = SumOfFourValues(h,i) {:>> range = 14.0..14.0;}
                 } 
             """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(7.0, solver.variable("TestModule::b"))
        assertBounds(8.0, solver.variable("TestModule::i"))
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
                    attribute c: ISQ::EnergyValue = Energy(a,b) {:>> range = 10000..10000 [J];}
                    attribute e: ISQ::SpeedValue;
                    attribute f: ISQ::MassValue = 800.0 [kg];
                    attribute g: ISQ::EnergyValue = Energy(e,f) {:>> range = 10000..10000 [J];}
                }
            } 
        """, Runlevel.ALL)
        val a = global.resolve("Test::TestModule::a")
        solver.propagate()
        assertNoIssues()
        assertNotNull(a)
        val b = global.resolve("Test::TestModule::b")
        assertNotNull(b)
        assertBounds(10000.0, solver.variable("Test::TestModule::c"))

        //sqrt could be positive or negative value
        assertBounds(-10.0 .. 10.0, solver.variable("Test::TestModule::a"))

        assertBounds(-5.0 .. 5.0, solver.variable("Test::TestModule::e"))
        assertBounds(-5.0..5.0, solver.variable("Test::TestModule::e"))
    }

    @Test
    fun userDefFunctionTestString() = testSession("Calculations") {
        loadSysMLv2("""
             calc def ConvertASILtoInt{
                in i: ScalarValues::String;            
                return result: ScalarValues::Integer = if i=="QM" ? 1 else 0;           
            }              
            attribute a: ScalarValues::Integer = ConvertASILtoInt("QM"); 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertBounds(1, solver.variable("a").vectorQuantity.idd())
    }


    @Test
    fun userDefFunctionTestString2() = testSession("Calculations") {
        loadSysMLv2("""
            calc def ConvertIntToASIL {
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value";        
            } 
 
            attribute a: ScalarValues::String = ConvertIntToASIL(1);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("A", solver.variable("a").vectorQuantity.value.asStrDD().toString())
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
        """, Runlevel.ALL)
        assertNoIssues()
        //sqrt could be positive or negative value
        assertEquals("A", solver.variable("a").vectorQuantity.value.asStrDD().toString())
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
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertBounds(2, solver.variable("e").vectorQuantity.idd())
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
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        //sqrt could be positive or negative value
        assertBounds(0, solver.variable("e").vectorQuantity.idd())
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
        assertBounds(1, solver.variable("i").vectorQuantity.idd())
    }

    @Test
    fun userDefFunctionHierarchical() = testSession("Calculations", "ISQ") {
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
            attribute mass: ISQ::MassValue {:>> range = 1600..2000 [kg];}
            
            attribute stopDist: ISQ::LengthValue = StoppingDistance(speed, brakeForce, mass) {
                :>> range = 0..70.0 [m];
            }
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(62.5 .. 70.0, solver.variable("stopDist"), unit = "m")
        assertBounds(62.5 .. 70.0, solver.variable("stopDist"), unit = "m")
        assertBounds(1600.0 .. 1792.0, solver.variable("mass"), unit = "kg")
    }
}
