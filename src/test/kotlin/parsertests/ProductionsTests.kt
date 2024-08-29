package parsertests

import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.parser.kerml.ElementList
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Unit
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.COMMA
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.services.session.SessionImplementation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue


/**
 * Tests of the SysMD2 language's production rules.
 * To access the protected rules, the test class must be inherited from the Parser class.
 */
class ProductionsTests {

    private fun testParser(): KerML {
        val model = SessionImplementation(loadKerML = false)
        val textualRepresentation = model.create(TextualRepresentationImplementation(language = "SysMD", body = ""), model.global)
        return KerML(model = model, textualRepresentation = textualRepresentation)
    }

    /**
     * The identification statement following SysMLv2 textual is tested here.
     */
    @Test fun identificationTest()  = testParser().run {
        input = """
                name
                < idname > 'name in quotes' 
                < 'id in quotes' > 
                """.trimIndent()
        var identification = Identification()
        assertEquals("name", identification.name)
        identification=Identification()
        assertEquals("name in quotes", identification.name)
        assertEquals("idname", identification.shortName)
        identification=Identification()
        assertEquals("id in quotes", identification.shortName)
    }

    /**
     * Test that definition creates a class, and that in a hasA decomposition,
     * occurrence of classes/elements can be retrieved via its name.
     */
    @Test fun classDefinitionTest() = testParser().run {
        input = """
            Global defines 
                class b isA Any. 
            Global hasA 
                feature a: b; 
                attribute c: Any = Any; 
                part d: Any;
                part e: Any; 
                part sysmlAllowsNoClass. 
        """
        Triple()
        Triple()
    }


    @Test
    fun prefixesTest() = testParser().run {
        input = """
            public in feature f; 
        """.trimIndent()
        NamespaceBodyElement() /*
        assertTrue { Scanner.Definitions.Token.Kind.IN in semantics.prefixes}
        assertTrue { Scanner.Definitions.Token.Kind.PUBLIC in semantics.prefixes}
        assertTrue { semantics.prefixes.size == 2} */
        assertTrue { token.kind == EOF }
    }


    /**
     * Triple with hasA
     */
    @Test fun hasATest1() = testParser().run {
        input = """
        Global hasA
            attribute name : Real  = 2 + 5 * 4; 
            part name1 : [1..2] Any = Any; 
            part name2 : Any. 
        A hasA 
            part b: Any.
        """.trimIndent()
        Triple()
    }


    /**
     * Triple with "defines"
     */
    @Test
    fun testDefinesTripleTest() = testParser().run {
        input = """
        Global defines 
            class a isA b; 
            class c isA d. 
        """.trimIndent()
        Triple()
        consume(EOF)
        Unit
    }


    /**
     * A list of elements.
     */
    @Test fun partListTest1() = testParser().run {
        input = """
            attribute name  : Real    = 2 + 5 * 4; 
            part name1 : [1..2] Any = Any; 
            part name2 : Any; 
        """.trimIndent()
        ElementList()
    }

    @Test
    fun parseClassDefinition(): Unit = testParser().run {
        input = "class a :> Any;"
        NamespaceBodyElement()
        assertEquals(0, model.status.exceptions.size, model.status.exceptions.toString())
    }

    @Test
    fun parsePartDefinition3(): Unit = testParser().run {
        input = "part def a :> Any;"
        NamespaceBodyElement()
        assertEquals(0, model.status.exceptions.size, model.status.exceptions.toString())
    }


    @Test
    fun parsePackage(): Unit = testParser().run {
        input = "package p;"
        NamespaceBodyElement()
        consume(EOF)
        assertEquals(0, model.status.exceptions.size, model.status.exceptions.toString())
    }

    @Test
    fun parseAssoc(): Unit = testParser().run {
        input = """
            assoc Link :> Base::Anything {
                end feature From: KerML::root::Element;
                end feature To:   KerML::root::Element;
            }
            assoc BinaryLink :> Link {
                end feature From: KerML::root::Element [1..1];
                end feature To:   KerML::root::Element [1..1]; 
            }
        """.trimIndent()
        NamespaceBodyElement()
        assertTrue( model.status.exceptions.isEmpty(), model.status.exceptions.toString())
        NamespaceBodyElement()
        assertTrue( model.status.exceptions.isEmpty(), model.status.exceptions.toString())
    }


    @Test
    fun tripleProductionTest2() = testParser().run  {
        input = """
            c hasA 
                attribute b: Real; 
                attribute c: Integer. 
            d defines 
                class d isA Global::Any. 
        """.trimIndent()
        Triple()
        Triple()
        assertTrue(model.status.exceptions.isEmpty(), model.status.exceptions.toString())
    }

    @Test
    fun tripleProductionTest3() = testParser().run {
        // model.loadSysMDFromResources("ScalarValues.md")
        input = """
            a hasA 
                attribute b: Real; 
                attribute c: Integer;  
                part d: [1 .. 3] Global::Any = Global::Any; 
                import x;
                import y; 
                import z. 
        """.trimIndent()
        Triple()
    }

    /**
     * Top-level: List of triples, separated by DOT and ended by EOF.
     */
    @Test fun tripleProductionTest4() = testParser().run {
        input = """
            P defines 
                class Component :> Base::Anything.
        """.trimIndent()
        parseSysMD()
        assertEquals(0, model.status.exceptions.size, model.status.exceptions.toString())
    }


    /**
     * Has - Relations can have **either** list of triples **or** list of occurrences.
     * In particular, Packages have lists of triples,
     * and Elements have lists of occurrences.
     */
    @Test fun tripleTest5() = testParser().run  {
        input = """
            // More complex test with "mixed challenges" for parsing. 
            attribute pi : Real = 3.1414; 
            attribute T  : Real; 
            
            package e; 
    
            e hasA import p2::easd. 
            
            package e. 
            
            e defines   
                class d isA Element; 
                assoc x isA Relation { end feature b; end feature blupp; }
                class x.
                
            d hasA  
                attribute p:  Real = 5.0 V; 
                feature r:  Real = a < b;  
                feature p:  Real = a*c; 
                feature b:  Integer = 2; 
                feature c:  Component = part1.
                    
            e hasA 
                Value a: Real = 5.0 V. 
                
            // Hierarchical components in separate statements, avoiding nesting.     
            d hasA 
                feature r: Real = 3; 
                feature x: Element [1..2] =  Element.
            it defines 
                assoc x isA Link { end feature a; end feature b; }
                class y.
                
        """.trimIndent()
        parseSysMD()
        assertEquals(0, model.status.exceptions.size, model.status.exceptions.toString())
    }


    @Test fun unitProductionTest() = testParser().run  {
        input = """
            1 / m , 
            m^2 / km^3 ,
            m^2 m m / k k k^4,  
            %, 
        """.trimIndent()
        assertEquals("1 / m", Unit())
        consume(COMMA)
        assertEquals("m^2 / km^3", Unit())
        consume(COMMA)
        assertEquals("m^2 m m / k k k^4", Unit())
        consume(COMMA)
        assertEquals("%", Unit())
    }

    @Test fun elementListTest() = testParser().run {
        input = """
            package    p;
            feature    v: Any;
            connector  r = source rel target;
            assoc      a specializes Link; 
            part       f: Any; 
            attribute  a: Any; 
            class      c isA Any; 
        """
        ElementList()
        // 9 new elements (Multiplicities, Specialization included!)
        assertEquals(18, model.getUnownedElements().size)
    }

    @Test
    fun prefixOperators() = testParser().run {
        input = """
        attribute a: Boolean = not a;
        attribute b: Real = +1.0;
        attribute c: Real = -1.0;
        """
        NamespaceBodyElement()
        NamespaceBodyElement()
        NamespaceBodyElement()
        EOF.consume()
        assertTrue(model.status.exceptions.isEmpty())
    }

    @Test
    fun postfixExpressionsTest() = testParser().run {
        input = """
        attribute a: Real = 1.0 m;
        attribute b: Real = -2.0 [m];
        """
        NamespaceBodyElement()
        NamespaceBodyElement()
        assertTrue(model.status.exceptions.isEmpty())
    }

    @Test  // ISSUE! sign is not considered properly
    fun rangesTest() = testParser().run {
        input = """
        attribute a: Real = 1.0 .. 2.0; 
        attribute b: Real = [-2.0 .. -1.0];
        attribute c: Integer = [-2 .. -1] m;
        attribute d: Real = [1.0 .. 3.0];
        attribute e: Real = [1.0 .. 3.0] m;
        """
        NamespaceBodyElement()
        assertTrue(model.status.exceptions.isEmpty() )
        NamespaceBodyElement()
        model.status.reset()
        NamespaceBodyElement()
        NamespaceBodyElement()
        NamespaceBodyElement()
        assertTrue(model.status.exceptions.isEmpty())
    }

    @Test
    fun requirementUsageTest() = testParser().run {
        input = """
            requirement <'req 1.1'> EnoughMass: EnoughMassDef {
                subject vehicle: Vehicle; 
                // attribute mass: Mass :>>  vehicle::mass; 
                attribute mass : Mass; 
                require r mass > 100.0 [kg]; 
            }
        """.trimIndent()
        NamespaceBodyElement()
        assertTrue(model.status.exceptions.isEmpty())
    }


    @Test
    fun requirementDefinitionTest() = testParser().run {
        input = """
            requirement def <'req 1.1'> EnoughMassDef {
                 // attribute mass: Mass :>>  vehicle::mass; 
                attribute mass : Mass; 
                require r mass > 100.0 [kg]; 
            }
        """.trimIndent()
        NamespaceBodyElement()
        assertTrue(model.status.exceptions.isEmpty())
    }
}
