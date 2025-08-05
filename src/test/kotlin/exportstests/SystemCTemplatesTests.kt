package exportstests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exports.Exporter
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemCTemplatesTests {

    @Test @Ignore //Does not accept changes of ScalarValues
    fun generalConnectivityTest() = testSession("Parts", "Ports", "Connections") {
        settings.catchExceptions = true
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 
            
            part wirelessDevice{
                part transmitter{
                    out port ausgang1;
                    out port ausgang2;
                    attribute ausgang3_Attribute: Real;
                }
                
                part receiver{
                    in port eingang1;
                    in port eingang2;
                    attribute eingang3_Attribute: Real;
                }
            }
            
            connection def Signal;
            interface interface_wire : Signal connect wirelessDevice.transmitter.ausgang1 to wirelessDevice.receiver.eingang1;
            connection connector_wire : Signal connect  wirelessDevice.transmitter.ausgang2 to wirelessDevice.receiver.eingang2;
            connection connection_wire : Signal connect wirelessDevice.transmitter.ausgang3_Attribute to wirelessDevice.receiver.eingang3_Attribute; 
            
        }
        """)
        assertNoIssues()

        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

    }

    @Test @Ignore //Does not accept changes of ScalarValues
    fun hierarchicalChannelTest() = testSession("Parts", "Ports", "Requirements", "Connections") {
        settings.catchExceptions = true
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 
            
            part wirelessDevice{
                part transmitter{
                    out port ausgang1;
                    out port ausgang2;
                    attribute ausgang3_Attribute: Real;
                }
                
                part receiver{
                    in port eingang1;
                    in port eingang2;
                    attribute eingang3_Attribute: Real;
                }
            }
            
     
            connection def ComplexSignal;
            interface interface_wire : ComplexSignal connect test::wirelessDevice::transmitter::ausgang1 to test::wirelessDevice::receiver::eingang1;
            connection connector_wire : ComplexSignal connect  test::wirelessDevice::transmitter::ausgang2 to test::wirelessDevice::receiver::eingang2;
            connection connection_wire : ComplexSignal connect test::wirelessDevice::transmitter::ausgang3_Attribute to test::wirelessDevice::receiver::eingang3_Attribute; 
            
        }
        """)
        propagate()
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    /** Problem with connection with 2 inputs and 2 outputs */
    @Test @Ignore
    fun busTest() = testSession("Parts", "Ports", "Requirements") {
        loadSysMLv2("""
            package test {
                import ScalarValues::*; 
                import SI::*; 

                part def compA{
                    attribute output1: Real;
                    attribute output2: Real;
                    attribute inputA: Real;
                }
                
                part def compB{
                    attribute input: Real;
                }
                
                 //These will have to use their own _CLASS version as Class instead of the inherited superClass because the use the attributes as Ports
                part testModuleA: compA[2 .. 2] ;
                part testModuleB: compB[1 .. 1];
                
                //These will use the Classes as defined as they do not alter the attributes in any way
                part someA:  compA;
                part someB:  compB;
                
                connection def Bus;
                connection bus: Bus connect test::testModuleA::output1,
                                          test::testModuleA::output2
                                   to
                                          test::testModuleB::input,
                                          test::testModuleA::inputA.
            }
            """.trimIndent()
        )
        assertTrue(status.issues.isEmpty(), "Errors: ${status.issues}")

        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        
        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    @Test @Ignore //Does not accept changes of ScalarValues
    fun inheritanceTest() = testSession("Parts") {
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 
            
            part def SuperClass{
                attribute superAttribute : Real;
            }
            
            part def SubClassWithOverride :> SuperClass{
                attribute superAttribute : Real;
            }
            
            part def SubClassWithNewAttribute :> SuperClass{
                attribute newAttribute : Real;
            }
            
            part def SubClassOnlyInherit :> SuperClass;
            
            
            part subClassWithOverride : SubClassWithOverride;
            part subClassWithNewAttribute : SubClassWithNewAttribute;
            part subClassOnlyInherit : SubClassOnlyInherit;
            part justInstantiateSuperClass : SuperClass;
            part overrideSuperAttribute : SuperClass{
                attribute superAttribute : Real;
            }
            part addNewAttribute : SuperClass{
                attribute newAttribute : Real;
            }
        }
        """)
        propagate()
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    @Test
    fun connectionsInMainAndModules() = testSession("SI", "Signals", "Parts", "Ports", "Connections") {
        settings.catchExceptions = true
        loadSysMLv2("""
            package test {
                private import ScalarValues::*; 
                private import SI::*; 
                
                part def XYZ;
                
                part a{
                    part b{
                        in port b_out; 
                    }       
                    part c{
                        in port c_in;
                    }
                }
                
                part x : XYZ{
                    out port x_out;
                }
                part y : XYZ{
                    in port y_in;
                }
                
                connection wire_b_c : Signals::Signal connect a.b.b_out to a.c.c_in;
                connection wire_x_y : Signals::Signal connect x.x_out to y.y_in; 
            }
        """)
        assertTrue(status.issues.isEmpty(), "${status.issues}")

        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }


    @Test @Ignore //Does not accept changes of ScalarValues
    fun expressionClassificationTest() = testSession("Parts", "Ports", "Requirements") {
        settings.catchExceptions = true
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 

            // 1. -- CHECK IF EXPRESSIONS WORK IN MODULES
            
            part def Vehicle isA Base::Anything {
            
                //Variables NO INITS
                attribute var_noInit_Real : Real;
                attribute var_noInit_Integer : Integer;
                attribute var_noInit_String : String;
                attribute var_noInit_Boolean : Boolean;
                
                //VARIABLES
                attribute var_Boolean : Boolean = false;
                attribute var_String : String = "HALLO";
                attribute var_Real_Unit : Real [m] = [0.0 .. 100.0] m;
                attribute var_Real_NoUnit : Real (0.0 .. 100.0);
                attribute var_Integer_Unit : Integer [m] = [0 .. 100] m;
                attribute var_Integer_NoUnit : Integer (0 .. 5);
                
                //CONSTANTS
                attribute const_Real_Unit_FakeRange : Real [m] = [200.0 .. 200.0] m; 
                attribute const_Real_NoUnit_FakeRange : Real (200.0 .. 200.0);
                attribute const_Real_NoUnit : Real = 5.0;
                attribute const_Real_Unit : Real [s]  = 5.0 s;
                attribute const_Integer_Unit_FakeRange : Integer [m] = [200 .. 200] m; 
                attribute const_Integer_NoUnit_FakeRange : Integer (200 .. 200);
                attribute const_Integer_NoUnit : Integer = 5;
                attribute const_Integer_Unit : Integer [m]  = 5;
            }
            
             // 2. -- CHECK IF EXPRESSIONS WORK IN THE MAIN.CPP
             
             //Variables NO INITS
                attribute var_noInit_Real : Real;
                attribute var_noInit_Integer : Integer;
                attribute var_noInit_String : String;
                attribute var_noInit_Boolean : Boolean;
                
                //VARIABLES
                attribute var_Boolean : Boolean = false;
                attribute var_String : String = "HALLO";
                attribute var_Real_Unit : Real [m] = [0.0 .. 100.0] m;
                attribute var_Real_NoUnit : Real (0.0 .. 100.0);
                attribute var_Integer_Unit : Integer [m] = [0 .. 100] m;
                attribute var_Integer_NoUnit : Integer (0 .. 5);
                
                //CONSTANTS
                attribute const_Real_Unit_FakeRange : Real [m] = [200.0 .. 200.0] m; 
                attribute const_Real_NoUnit_FakeRange : Real (200.0 .. 200.0);
                attribute const_Real_NoUnit : Real = 5.0;
                attribute const_Real_Unit : Real [s]  = 5.0 s;
                attribute const_Integer_Unit_FakeRange : Integer [m] = [200 .. 200] m; 
                attribute const_Integer_NoUnit_FakeRange : Integer (200 .. 200);
                attribute const_Integer_NoUnit : Integer = 5;
                attribute const_Integer_Unit : Integer [m]  = 5;
        }
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()


        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    /**Ensures that:
     *   - Wiring of Classes is ignored
     *   - Wiring of Parts is realized in the templates with the following aspects
     *      - If the attributes that are connected are inherited from a class,
     *          the class will stay untouched but a new class is created from
     *          the part in which the attributes are transformed to Ports
     *  - Part Definitions (part def) should be realized as Modules in SystemC but not create instances!
     *  - Wiring of Part Definitions is ignored
     *  - Wiring of Parts that instantiate Part Definitions is realized
     */
    @Test @Ignore //Does not accept changes of ScalarValues
    fun properWiringTest() = testSession("Parts", "Ports", "Requirements", "Connections") {
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 
            
            connection def Signal;
            
            //These two classes will be featured in a wiring, the wiring should not be realized in the SystemC templates
            part def ClassA  {
                attribute outp : Real;
            }           
            part def ClassB  {
                attribute inp : Real;
            }
            
            //This wire is illegal as it connects Classes - it shall not be created in the SystemC templates
            connection illegalWire : Signal connect test::ClassA::outp to test::ClassB::inp;
             
            
            
             
            //These parts should be instantiated and use the defined Class
            part PartA : ClassA;
            part PartB : ClassB;
            
            //These parts should be instantiated but as they are used in the wire, they require the attributes to be ports and therefore use their own Class variant
            part PartC : ClassA;
            part PartD : ClassB;
            
            //This wire is legal, it connects Parts
            //The class used by the Parts has attributes which cannot be used in wiring, therefore the Parts
            //will introduce their own Class variant in which the attributes are transformed to ports which can be used in this wiring
            connection legalWire1 : Signal connect test::PartC::outp to test::PartD::inp;
            
            
            
           
            //These parts simply inherit from the Classes as they - no new Class shall be introduced
            part PartA_TrueInheritance : ClassA;
            part PartB_TrueInheritance : ClassB;
            
            
            
                  
            //Part definitions are actually just classes
            //Therefore the corresponding Module files should be created but no instantiation should take place
            part def DefinedPartA{
                attribute outp : Real;
            }
            part def DefinedPartB{
                attribute inp : Real;
            }
            
            //This Connection connects the Part Definitions - as they are in fact just classes, they cannot be used in wiring.
            //This wiring is therefore illegal and should not realized in the SystemC templates
            connection illegalPartDefWire : Signal connect test::DefinedPartA::outp to test::DefinedPartB::inp;
            
            //These are Parts that instantiate previous Definitions, these Parts should be instantiated and use the correct Class
            part actualPartA : DefinedPartA;
            part actualPartB : DefinedPartB;
            
            //These parts should be instantiated but as they are used in the wire, they require the attributes to be ports and therefore use their own Class variant
            part actualPartC : DefinedPartA;
            part actualPartD : DefinedPartB;
            
            //This wire is legal as it connects two actual parts
            connection legalWire2: Signal connect test::actualPartC::outp to test::actualPartD::inp;
        }
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()


        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    /**Checks that Channels are located at the correct location in the SystemC Project**/
    //TODO Current problem is that the channel "if2" is created two times, once in part A and a second time in part B, would be more logical to instantiate it in the main.cpp or so
    @Ignore
    @Test
    fun correctChannelLocations() = testSession("ISO26262", "Parts", "Ports", "Requirements", "Connections") {
        loadSysMLv2("""
        package test {
            import ScalarValues::*; 
            import SI::*; 
            
            connection def Signal;
            
            part A{
                out port outp;
                
                part X{
                    out port outp;
                }
            }
            
            part B{
                in port inp; 
                    
                part Y{
                    in port inp;
                }
            }
           
            interface if1 : Signal connect test::A::outp to test::B::inp;
            interface if2 : Signal connect test::A::X::outp to test::B::Y::inp;
        }
            """.trimIndent()
        )

        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()


        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    /**
     * Checks that ports are only connected once and to one Channel only.
     * Also ensures that a Channel has only one driving port (output port) but allows multiple listening ports (input ports)
     **/
    @Test
    fun restrictMultipleBindingTest() = testSession("SI", "Parts", "Ports", "Connections", "Signals") {
        loadSysMLv2("""
        package test {
            private import ScalarValues::*; 
            private import SI::*; 
                        
            part A {
                out port outp;
            }
            
            part B {
                in port inp; 
            }
                                 
            //Ports of A and B get bound twice, the second time it should be commented out to avoid over-connecting the ports
            interface if1 : Signals::Signal connect A.outp to B.inp;
            interface if2 : Signals::Signal connect A.outp to B.inp;                              
                                               
            part C {
                out port output; 
            }
                        
            part D [1 .. 4] {
                in port input;
            }
            
            //We define that C should be wired to D
            //NOTE: As D exists four times, all four instances should be wired to the Signal
            interface if3 : Signals::Signal connect C.output to D.input;
            
            part X : Base::Anything [1 .. 4] {
                out port output; 
            }
                        
            part Y{
                in port input;
            }
            
            //Here we try to wire multiple inputs to a Signal which is illegal in SystemC
            //Therefore only one input can be connected and the remaining should be commented out
            interface if4 : Signals::Signal connect test::X::output to test::Y::input;
        }
        """)
        propagate()
        val if1 = global.resolve<Element>("test::if1")
        val d = global.resolve<Element>("test::D")
        assertNoIssues()
        val testDirectory = File("src/test/resources/toSystemC")
        val exporter = Exporter()

        val pkg = global.resolve<Element>("test") as Element
        exporter.analyzeSysMD(pkg)
        exporter.toSystemC(pathIn = testDirectory.path, tbLibFolder = "")

        //Now compare all generated SystemC Files to the "ground-truth" files
        compareToFiles(Thread.currentThread().stackTrace[1].methodName, pkg.name.toString())

        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
    }

    private fun compareToFiles(folderName: String, packageName: String){
        val referenceFolder = File(Paths.get("").toAbsolutePath().toString() +
                "/src/test/resources/toSystemC/referenceFiles/$folderName/$packageName/")

        val generatedFolder = File(Paths.get("").toAbsolutePath().toString() +
        "/src/test/resources/toSystemC/$packageName/")

        if(!referenceFolder.isDirectory) throw Exception("No Directory with reference files at: ${referenceFolder.name}")

        if(!generatedFolder.isDirectory) throw Exception("No Directory with generated files at: ${generatedFolder.name}")

        if(referenceFolder.listFiles()?.isEmpty() == true) throw Exception("The reference folder contains no files: ${referenceFolder.name}")

        compareDirectory(referenceFolder, generatedFolder)

    }

    private fun compareDirectory(referenceFolder: File, generatedFolder: File) {
        referenceFolder.listFiles()?.forEach { refFile ->
            val genFilePath = generatedFolder.toPath().resolve(refFile.name)

             if(refFile.extension == "h" || refFile.extension == "cpp"){ //Only compare .cpp and .h files
                assertEquals(normalizedContentOf(refFile), normalizedContentOf(genFilePath.toFile()),
                    "Error Message: The generated SystemC file \"${refFile.name}\" does not match the ground truth file!\n" +
                            "\tPath to generated SystemC file: ${genFilePath.pathString}\n" +
                            "\tPath to ground truth file: ${refFile.path}"
                )
            } else if(refFile.isDirectory){
                 //If is a subdirectory, call the comparison inside
                 compareDirectory(refFile, genFilePath.toFile())
             }
        }

    }

    private fun normalizedContentOf(file: File): String {
        return normalizeLineEndings(file.readText())
    }

    private fun normalizeLineEndings(text: String): String {
        return text.replace("\r\n", "\n").replace("\r", "\n")
    }
}


