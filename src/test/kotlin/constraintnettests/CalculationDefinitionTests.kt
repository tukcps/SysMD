package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CalculationDefinitionTests {
    @Test
    fun userDefFunctionSimple() = testSession("Parts") {
        loadSysMD("""
             package Test {   
                 calc def Velocity {
                    in v1 : ScalarValues::Real [km/h];
                    in v2 : ScalarValues::Real [m/s];
                    attribute a: ScalarValues::Real = v1+v2;
                    return result: ScalarValues::Real [km/h] = a; 
                 }
                 part TestModule {
                    attribute a: ScalarValues::Real [km/h] = 10.0 [km/h];
                    attribute b: ScalarValues::Real [km/h] = 26.0 [km/h];
                    attribute c: ScalarValues::Real [m/s] = Velocity(a,b);
                    attribute c2: ScalarValues::Real [km/h] = Velocity(10.0 [m/s],b);
                 }
             }    
             """)
        propagate()
        loadSysMD("""
             package Test {   
                 calc def Velocity {
                    in v1 : ScalarValues::Real [km/h];
                    in v2 : ScalarValues::Real [m/s];
                    attribute a: ScalarValues::Real = v1+v2;
                    return result: ScalarValues::Real [km/h] = a; 
                 }
                 part TestModule {
                    attribute a: ScalarValues::Real [km/h] = 10.0 [km/h];
                    attribute b: ScalarValues::Real [km/h] = 26.0 [km/h];
                    attribute c: ScalarValues::Real [m/s] = Velocity(a,b);
                    attribute c2: ScalarValues::Real [km/h] = Velocity(10.0 [m/s],b);
                 }
             }    
             """)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assert(global.resolveVar("Test::Velocity::v1")!!.feature.direction == Feature.FeatureDirectionKind.IN)
        assert(global.resolveVar("Test::Velocity::v2")!!.feature.direction == Feature.FeatureDirectionKind.IN)
        assert(global.resolveVar("Test::Velocity::a")!!.feature.direction == Feature.FeatureDirectionKind.INOUT)
        assert(global.resolveVar("Test::Velocity::result")!!.feature.direction == Feature.FeatureDirectionKind.OUT)
        assert(10.0 in global.resolve<Feature>("Test::TestModule::c")!!.variable!!.vectorQuantity.aadd().getRange())
        assert(62.0 in global.resolve<Feature>("Test::TestModule::c2")!!.variable!!.vectorQuantity.valuesIn("km/h")[0].asAadd().getRange())
    }

    @Test
    fun userDefFunctionTest() = testSession {
        loadSysMD("""
             calc def Energy {
                in v : ScalarValues::Real [km/h];
                in m : ScalarValues::Real [kg];
                return result : ScalarValues::Real [J] = 0.5 * m * sqr(v); 
             }
             feature TestModule {
                feature a: ScalarValues::Real [km/h] = 10.0 [m/s];
                feature a1: ScalarValues::Real [km/h] = 2.0 [m/s];
                feature b: ScalarValues::Real [kg] = 200.0 [kg];
                feature b1: ScalarValues::Real [kg] = 20.0 [kg];
                feature c: ScalarValues::Real [J] = Energy(a,b);
                feature c1: ScalarValues::Real [J] = Energy(a1,b1);
             }
         """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assert(10000.0 in global.resolveVar("TestModule::c")!!.vectorQuantity.aadd().getRange())
        assert(40.0 in global.resolveVar("TestModule::c1")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestVector() = testSession(loadKerML = false) {
        loadSysMD("""
             package ScalarValues { datatype ScalarValue; datatype Integer:>ScalarValue; datatype Real :> ScalarValue; }
             package Test {   
                 calc def Distance {
                    in a : ScalarValues::Real [m];
                    in b : ScalarValues::Real [m];
                    return result : ScalarValues::Real [m] = sqrt(sqr(a[0]-b[0])+sqr(a[1]-b[1])+sqr(a[2]-b[2])); 
                 }
                 feature TestModule {
                    feature a: ScalarValues::Real [m] = (10.0,5.0,20.0) [m];
                    feature b: ScalarValues::Real [m] = (0.0,-5.0,25.0) [m];
                    feature b1: ScalarValues::Real [m] = (10.0,1.0,17.0) [m];
                    feature c: ScalarValues::Real [m] = Distance(a,b);
                    feature c1: ScalarValues::Real [m] = Distance(a,b1);
                 }
             }    
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assert(15.0 in global.resolveVar("Test::TestModule::c")!!.vectorQuantity.aadd().getRange())
        assert(5.0 in global.resolveVar("Test::TestModule::c1")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestInteger() = testSession("Parts") {
        loadSysMD(
            """
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Integer;
                    in b : ScalarValues::Integer;
                    in c : ScalarValues::Integer;
                    in d : ScalarValues::Integer;
                    attribute x: ScalarValues::Integer= a+b;
                    attribute y: ScalarValues::Integer= c+d;
                    return result : ScalarValues::Integer = x+y; 
                 }
                 part TestModule {
                    attribute a: ScalarValues::Integer = 6;
                    attribute b: ScalarValues::Integer = 7;
                    attribute c: ScalarValues::Integer = 8;
                    attribute d: ScalarValues::Integer = 9;
                    attribute e: ScalarValues::Integer = SumOfFourValues(a,b,c,d);
                    attribute e1: ScalarValues::Integer = SumOfFourValues(d,c,b,a);
                 }
             }    
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assert(30 in global.resolveVar("Test::TestModule::e")!!.vectorQuantity.idd().getRange())
        assert(30 in global.resolveVar("Test::TestModule::e1")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDown() = testSession("Parts") {
        loadSysMD(
            """
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Integer;
                    in b : ScalarValues::Integer;
                    in c : ScalarValues::Integer;
                    in d : ScalarValues::Integer;
                    attribute x: ScalarValues::Integer= a+b;
                    attribute y: ScalarValues::Integer= c+d;
                    return result : ScalarValues::Integer = x+y .
                 }
                 part TestModule {
                    attribute a1: ScalarValues::Integer = 6;
                    attribute b1: ScalarValues::Integer = 7;
                    attribute c1: ScalarValues::Integer = 8;
                    attribute d1: ScalarValues::Integer;
                    attribute d2: ScalarValues::Integer;
                    attribute e1: ScalarValues::Integer(35..35) = SumOfFourValues(a1,b1,c1,d1);
                    attribute e2: ScalarValues::Integer(40..40) = SumOfFourValues(8,7,6,d2);
                 }
             }    
             """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(14, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().min)
        assertEquals(14, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().max)
        assert(14 in global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange())
        assertEquals(19, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().min)
        assertEquals(19, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().max)
        assert(19 in global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDown2() = testSession {
        loadSysMD(
            """
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Integer;
                    return result : ScalarValues::Integer = a; 
                 }
                 feature TestModule {
                    feature d1: ScalarValues::Integer; 
                    feature e1: ScalarValues::Integer(13..13) = SumOfFourValues(d1);
                    feature d2: ScalarValues::Integer;
                    feature e2: ScalarValues::Integer(17..17) = SumOfFourValues(d2);
                 }
             }    
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(13, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().min)
        assertEquals(13, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange().max)
        assert(13 in global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.idd().getRange())
        assertEquals(17, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().min)
        assertEquals(17, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange().max)
        assert(17 in global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun userDefFunctionTestRealEvalDown2() = testSession(loadKerML = false) {
        loadSysMD("""
             package ScalarValues { datatype ScalarValue; datatype Integer:>ScalarValue; datatype Real:>ScalarValue; }
             package Test {   
                 calc def SumOfFourValues {
                    in a : ScalarValues::Real;
                    return result : ScalarValues::Real = a * 2.0; 
                 }
                 feature TestModule {
                    feature d1: ScalarValues::Real; 
                    feature e1: ScalarValues::Real(28.0..28.0) = SumOfFourValues(d1); 
                    feature d2: ScalarValues::Real; 
                    feature e2: ScalarValues::Real(36.0..36.0) = SumOfFourValues(d2); 
                 }
             }    
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(14.0, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange().min)
        assertEquals(14.0, global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange().max)
        assert(14.0 in global.resolveVar("Test::TestModule::d1")!!.vectorQuantity.aadd().getRange())
        assertEquals(18.0, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange().min)
        assertEquals(18.0, global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange().max)
        assert(18.0 in global.resolveVar("Test::TestModule::d2")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestIntegerEvalDownMultipleLevel() = testSession("Parts") {
        loadSysMD("""
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
                    // feature b2: ScalarValues::Integer;
                    attribute e1: ScalarValues::Integer(13) = SumOfFourValues(x, y);
                    //feature e2: ScalarValues::Integer(14) = SumOfFourValues(a,b2); 
                 }
            }
            """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(6, global.resolveVar("Test::TestModule::x")!!.vectorQuantity.idd().getRange().min)
        assertEquals(7, global.resolveVar("Test::TestModule::y")!!.vectorQuantity.idd().getRange().max)
        // assert(7 in global.resolveVar("Test::TestModule::b1")!!.vectorQuantity.idd().getRange())
        // assertEquals(8, global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange().min)
        // assertEquals(8, global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange().max)
        // assert(8 in global.resolveVar("Test::TestModule::b2")!!.vectorQuantity.idd().getRange())
    }

    @Test
    fun calcDefTestRealEvalDown() = testSession("Parts") {
        loadSysMD("""
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
                attribute e1: ScalarValues::Real(35.0..35.0) = SumOfFourValues(a,b,c1,d1); 
                attribute e2: ScalarValues::Real(35.0..35.0) = SumOfFourValues(a,b,c2,d2); 
             } 
             """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(14.0, global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(14.0, global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(14.0 in global.resolveVar("TestModule::d1")!!.vectorQuantity.aadd().getRange())
        assertEquals(12.0, global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(12.0, global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(12.0 in global.resolveVar("TestModule::d2")!!.vectorQuantity.aadd().getRange())
    }

    @Test
    fun userDefFunctionTestRealEvalDownMultipleLevel() = testSession("Parts") {
        loadSysMD("""
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
                    attribute e: ScalarValues::Real(13.0..13.0) = SumOfFourValues(a,b);
                    attribute h: ScalarValues::Real = 6.0;
                    attribute i: ScalarValues::Real;
                    attribute j: ScalarValues::Real(14.0..14.0) = SumOfFourValues(h,i);
                 } 
             """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertEquals(7.0, global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(7.0, global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(7.0 in global.resolveVar("TestModule::b")!!.vectorQuantity.aadd().getRange())
        assertEquals(8.0, global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(8.0, global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assert(8.0 in global.resolveVar("TestModule::i")!!.vectorQuantity.aadd().getRange())
    }


    @Test
    fun userDefFunctionTestEvalDown() = testSession {
        loadSysMD("""
             package Test {   
                 calc def Energy {
                    in v : ScalarValues::Real [km/h];
                    in m : ScalarValues::Real [kg];
                    return result : ScalarValues::Real [J] = 0.5 * m * sqr(v); 
                 }
                 feature TestModule {
                    feature a: ScalarValues::Real [km/h];
                    feature b: ScalarValues::Real [kg] = 200.0 [kg];
                    feature c: ScalarValues::Real(10000..10000) [J] = Energy(a,b);
                    feature e: ScalarValues::Real [km/h];
                    feature f: ScalarValues::Real [kg] = 800.0 [kg];
                    feature g: ScalarValues::Real(10000..10000) [J] = Energy(e,f);
                 }
             } 
             """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
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
    fun userDefFunctionTestString() = testSession {
        loadSysMD("""
             calc def ConvertASILtoInt{
                in i: ScalarValues::String;            
                return result: ScalarValues::Integer = if i=="QM" ? 1 else 0.            
            }  
            
            attribute a: ScalarValues::Integer = ConvertASILtoInt("QM"); 
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals(1, global.resolveVar("a")!!.vectorQuantity.idd().getRange().min)
    }


    @Test
    fun userDefFunctionTestString2() = testSession {
        loadSysMD("""
            calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value".            
            } 
 
            attribute a: ScalarValues::String = ConvertIntToASIL(1);
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals("A", global.resolveVar("a")!!.vectorQuantity.value.asStrDD().toString())
    }

    @Test
    fun userDefFunctionTestString3() = testSession {
        loadSysMD("""
             calc def ConvertASILtoInt{
                in i : ScalarValues::String;            
                return result: ScalarValues::Integer = if i=="QM" ? 0 else if i=="A" ? 1 else if i=="B" ? 2 else if i=="C" ? 3 else if i=="D" ? 4 else 10.            
            }
            calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value".            
            }    
            
            calc def ASILDecomposition{
                in part1: ScalarValues::String;
                in part2: ScalarValues::String;
                attribute part1asInt: ScalarValues::Integer = ConvertASILtoInt(part1);
                attribute part2asInt: ScalarValues::Integer = ConvertASILtoInt(part2);
                return result: ScalarValues::String = ConvertIntToASIL(part1asInt+part2asInt).
                
            }
            attribute a: ScalarValues::String = ASILDecomposition("A","QM");                
            """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals("A", global.resolveVar("a")!!.vectorQuantity.value.asStrDD().toString())
    }

    @Test @Disabled //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown() = testSession {
        loadSysMD("""
             calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String = if i==0 ? "QM" else if i==1 ? "A" else if i==2 ? "B" else if i==3 ? "C" else if i==4 ? "D" else "Wrong Value".            
            } 
            
            attribute e: ScalarValues::Integer.
            attribute f: ScalarValues::String("QM") = ConvertIntToASIL(e).                 
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals(2, global.resolveVar("e")!!.vectorQuantity.idd().getRange().min)
    }

    @Test @Disabled //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown1() = testSession {
        loadSysMD("""
             calc def ConvertIntToASIL{
                in i : ScalarValues::Integer;            
                return result: ScalarValues::String =  if i==0 ? "A" else if i==0 ? "A" else "B".            
            } 
            
            attribute e: ScalarValues::Integer;
            attribute f: ScalarValues::String("A") = ConvertIntToASIL(e);            
             """
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals(0, global.resolveVar("e")!!.vectorQuantity.idd().getRange().min)
    }

    @Test @Disabled //TODO: evalDown for Strings and ITE
    fun userDefFunctionTestStringEvalDown4() = testSession {
        loadSysMD("""     
            attribute i: ScalarValues::Integer;
            attribute f: ScalarValues::String("A") = if i==0 ? "QM" else if i==1 ? "A" else "B";      
             """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        //sqrt could be positive or negative value
        assertEquals(1, global.resolveVar("i")!!.vectorQuantity.idd().getRange().min)
    }
}
