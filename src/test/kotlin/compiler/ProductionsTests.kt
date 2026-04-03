package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.NamespaceBodyElement
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.Unit
import com.github.tukcps.sysmd.compiler.parser.sysmd.ElementList
import com.github.tukcps.sysmd.compiler.parser.sysmlv2.RequirementDefinition
import com.github.tukcps.sysmd.compiler.parser.sysmlv2.RequirementUsage
import com.github.tukcps.sysmd.compiler.parser.sysmd.Triple
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.COMMA
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.services.session.SessionImplementation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.assertNoIssues
import kotlin.test.assertTrue


/**
 * Tests of the SysMD2 language's production rules.
 * To access the protected rules, the test class must be inherited from the Parser class.
 */
class ProductionsTests {

    private fun kerMLParser(): KerML {
        val model = SessionImplementation( )
        return KerML(model = model)
    }

    private fun sysMLv2Parser(): SysMLv2 {
        val model = SessionImplementation( )
        return SysMLv2(model = model)
    }

    private fun sydMDParser(): SysMD {
        val model = SessionImplementation( )
        return SysMD(model = model)
    }

    /**
     * The identification statement following SysMLv2 textual is tested here.
     */
    @Test fun identificationTest()  = kerMLParser().run {
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
    @Test fun classDefinitionTest() = sydMDParser().run {
        input = """
            Global hasA 
                class b isA Any. 
            Global hasA 
                feature a: b; 
                feature c: Any = Any; 
                feature d: Any;
                feature e: Any; 
                feature sysmlAllowsNoClass. 
        """
        Triple()
        Triple()
    }


    @Test
    fun prefixesTest() = kerMLParser().run {
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
    @Test fun hasATest1() = sydMDParser().run {
        input = """
        Global hasA
            feature name : Real  = 2 + 5 * 4; 
            feature name1 : Any [1..2]; 
            feature name2 : Any.
        A hasA 
            feature b: Any.
        """.trimIndent()
        Triple()
    }


    @Test
    fun parseClassDefinition(): Unit = kerMLParser().run {
        input = "class a :> Any;"
        NamespaceBodyElement()
        model.assertNoIssues()
    }

    @Test
    fun parsePackage(): Unit = kerMLParser().run {
        input = "package p;"
        NamespaceBodyElement()
        consume(EOF)
        model.assertNoIssues()
    }

    @Test
    fun parseAssoc(): Unit = kerMLParser().run {
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
        model.assertNoIssues()
        NamespaceBodyElement()
        model.assertNoIssues()
    }


    @Test
    fun tripleProductionTest2() = sydMDParser().run  {
        input = """
            c hasA 
                feature b: Real; 
                feature c: Integer. 
            d hasA 
                class e isA Base::Anything. 
        """.trimIndent()
        Triple()
        Triple()
        model.assertNoIssues()
    }

    @Test
    fun tripleProductionTest3() = sydMDParser().run {
        // model.loadSysMDFromResources("ScalarValues.md")
        input = """
            a hasA 
                feature b: Real; 
                feature c: Integer;  
                feature d: Global::Any[1 .. 3] ; 
                import x;
                import y; 
                import z. 
        """.trimIndent()
        Triple()
    }

    /**
     * Top-level: List of triples, separated by DOT and ended by EOF.
     */
    @Test fun tripleProductionTest4() = sydMDParser().run {
        input = """
            P hasA 
                class Component :> Base::Anything.
        """.trimIndent()
        parse()
        model.assertNoIssues()
    }


    /**
     * Has - Relations can have **either** a list of triples **or** list of occurrences.
     * In particular, Packages have lists of triples,
     * and Elements have lists of occurrences.
     */
    @Test fun tripleTest5() = sydMDParser().run  {
        input = """
            // More complex test with "mixed challenges" for parsing. 
            Global hasA feature pi : Real = 3.1414.
            Global hasA feature T  : Real. 
            
            Global hasA package e. 
    
            e hasA private import p2::easd. 
            
            Global hasA package e. 
            e hasA   
                class d isA Element; 
                assoc x isA Relation { end feature b; end feature blupp; }
                class y.
                
            d hasA  
                feature p:  Real = 5.0 V; 
                feature r:  Real = a < b;  
                feature p:  Real = a*c; 
                feature b:  Integer = 2; 
                feature c:  Component = part1.
                    
            e hasA 
                feature a: Real = 5.0 V. 
                
            // Hierarchical components in separate statements, avoiding nesting.     
            d hasA 
                feature r: Real = 3; 
                feature x: Element [1..2] =  Element.
            it hasA 
                assoc x :> Link { end feature a; end feature b; }
                class y.     
        """
        parse()
        model.assertNoIssues()
    }


    @Test fun unitProductionTest() = kerMLParser().run  {
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

    @Test fun elementListTest() = kerMLParser().run {
        input = """
            package    p;
            feature    v: Base::Anything;
            connector  r from source to target;
            assoc      a specializes Link; 
            feature    f: Base::Anything; 
            feature    a: Base::Anything; 
            class      c :> Base::Anything; 
        """
        ElementList()
        val elements = model.repo.elements.size
        assertTrue(18 <= elements)
    }

    @Test
    fun prefixOperators() = kerMLParser().run {
        input = """
        feature a: Boolean = not a;
        feature b: Real = +1.0;
        feature c: Real = -1.0;
        """
        NamespaceBodyElement()
        NamespaceBodyElement()
        NamespaceBodyElement()
        EOF.consume()
        model.assertNoIssues()
    }

    @Test
    fun postfixExpressionsTest() = kerMLParser().run {
        input = """
        feature a: Real = 1.0 m;
        feature b: Real = -2.0 [m];
        """
        NamespaceBodyElement()
        NamespaceBodyElement()
        model.assertNoIssues()
    }

    @Test  // ISSUE! sign is not considered properly
    fun rangesTest() = kerMLParser().run {
        input = """
        feature a: Real = 1.0 .. 2.0; 
        feature b: Real = [-2.0 .. -1.0];
        feature c: Integer = [-2 .. -1] m;
        feature d: Real = [1.0 .. 3.0];
        feature e: Real = [1.0 .. 3.0] m;
        """
        NamespaceBodyElement()
        model.assertNoIssues()
        NamespaceBodyElement()
        model.status.reset()
        NamespaceBodyElement()
        NamespaceBodyElement()
        NamespaceBodyElement()
        model.assertNoIssues()
    }

    @Test
    fun requirementUsageTest() = sysMLv2Parser().run {
        input = """
            requirement <'req 1.1'> EnoughMass: EnoughMassDef {
                subject vehicle: Vehicle; 
                // feature mass: Mass :>>  vehicle::mass; 
                attribute mass : Mass; 
                assume constraint { mass > 100.0 [kg] }
            }
        """
        RequirementUsage()
        model.assertNoIssues()
    }


    @Test
    fun requirementDefinitionTest() = sysMLv2Parser().run {
        input = """
            requirement def <'req 1.1'> EnoughMassDef {
                 // feature mass: Mass :>>  vehicle::mass; 
                attribute mass : Mass; 
                require constraint r { mass > 100.0 [kg] }
            }
        """.trimIndent()
        RequirementDefinition()
        model.assertNoIssues()
    }
}
