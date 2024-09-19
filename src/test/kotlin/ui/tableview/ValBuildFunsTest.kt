package ui.tableview

import com.github.tukcps.sysmd.ui.tableview.TextReader
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.be
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.con0
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.con1_con2
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_be
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_mult_super
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_mult_super_const_v
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_super
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_super_con0
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_super_con1_to_con2
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_super_const_min_max_unit
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns.name_super_const_v_unit
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ValBuildFunsTest {
    
    @Test
    fun be() {
        val tr1 = TextReader("f::total_gain >= 2.0;").apply { next }
        val tr2 = TextReader("x.y(3) and v xor f == 11.8;").apply { next }
        
        assertEquals("f::total_gain >= 2.0", be(tr1)?.joinToString("_"))
        assertEquals("x.y(3) and v xor f == 11.8", be(tr2)?.joinToString("_"))
        
    }
    
    @Test
    fun con0() {
        val tr1 = TextReader("connect (a, b, c);").apply { next }
        val tr2 = TextReader("connect a;").apply { next }
        
        assertEquals("//_a_b_c_//", con0(tr1)?.joinToString("_"))
        assertEquals("//_a_//", con0(tr2)?.joinToString("_"))
    }
    
    @Test
    fun con1_con2() {
        val tr1 = TextReader("connect (a, b, c) to x;").apply { next }
        val tr2 = TextReader("connect a to (x, y, z);").apply { next }
        val tr3 = TextReader("connect (a, b, c) to (x, y, z);").apply { next }
        val tr4 = TextReader("connect a to x;").apply { next }
        val tr6 = TextReader("connect x to ;").apply { next }
        
        assertEquals("//_a_b_c_//_to_//_x_//", con1_con2(tr1)?.joinToString("_"))
        assertEquals("//_a_//_to_//_x_y_z_//", con1_con2(tr2)?.joinToString("_"))
        assertEquals("//_a_b_c_//_to_//_x_y_z_//", con1_con2(tr3)?.joinToString("_"))
        assertEquals("//_a_//_to_//_x_//", con1_con2(tr4)?.joinToString("_"))
        assertEquals("//_x_//_to_//__//", con1_con2(tr6)?.joinToString("_"))
    }
    
    @Test
    fun name() {
        val tr1 = TextReader("package p123pack;").apply { next }
        val tr2 = TextReader("import def id {").apply { next }
        
        assertEquals( "p123pack",name(tr1)?.joinToString("_"),)
        assertEquals( "id",name(tr2)?.joinToString("_"),)
    }
    
    @Test
    fun name_be() {
        val tr1 = TextReader("assert ass ;").apply { next }
        val tr2 = TextReader("assert ass { x.y(3::U) and v xor (f == 11.8) }").apply { next }
        
        assertEquals( "ass_", name_be(tr1)?.joinToString("_"),)
        assertEquals( "ass_x.y(3::U) and v xor (f == 11.8)", name_be(tr2)?.joinToString("_"),)
    }
    
    @Test
    fun name_super() {
        val tr1 = TextReader("part e;").apply { next }
        val tr2 = TextReader("in port def portD: Ports::Port {").apply { next }
        
        assertEquals( "e_", name_super(tr1)?.joinToString("_"),)
        assertEquals( "portD_Ports::Port", name_super(tr2)?.joinToString("_"),)
    }
    
    @Test
    fun name_super_con0() {
        val tr1 = TextReader("connection c123 connect (a, b, c);").apply { next }
        val tr2 = TextReader("connection c123 connect a;").apply { next }
        val tr3 = TextReader("connection c123: cons connect (a, b, c);").apply { next }
        val tr4 = TextReader("connection c123: cons connect a;").apply { next }
        val tr5 = TextReader("connection c123: cons connect ;").apply { next }
        val tr6 = TextReader("connection c123 connect;").apply { next }
        
        assertEquals( "c123__//_a_b_c_//", name_super_con0(tr1)?.joinToString("_"),)
        assertEquals( "c123__//_a_//", name_super_con0(tr2)?.joinToString("_"),)
        assertEquals( "c123_cons_//_a_b_c_//", name_super_con0(tr3)?.joinToString("_"),)
        assertEquals( "c123_cons_//_a_//", name_super_con0(tr4)?.joinToString("_"),)
        assertEquals( "c123_cons_//__//", name_super_con0(tr5)?.joinToString("_"),)
        assertEquals( "c123__//__//", name_super_con0(tr6)?.joinToString("_"),)
    }
    
    @Test
    fun name_super_con1_to_con2() {
        val tr1 = TextReader("connection some::conUse: conDef connect (a, b, c) to x;").apply { next }
        val tr2 = TextReader("connection some::conUse: conDef connect a to (x, y, z);").apply { next }
        val tr3 = TextReader("connection some::conUse connect (a, b, c) to (x, y, z);").apply { next }
        val tr4 = TextReader("connection some::conUse: conDef connect a to x;").apply { next }
        val tr6 = TextReader("connection c connect x to ;").apply { next }
        
        assertEquals("some::conUse_conDef_//_a_b_c_//_to_//_x_//", name_super_con1_to_con2(tr1)?.joinToString("_"), )
        assertEquals("some::conUse_conDef_//_a_//_to_//_x_y_z_//", name_super_con1_to_con2(tr2)?.joinToString("_"), )
        assertEquals("some::conUse__//_a_b_c_//_to_//_x_y_z_//", name_super_con1_to_con2(tr3)?.joinToString("_"), )
        assertEquals("some::conUse_conDef_//_a_//_to_//_x_//", name_super_con1_to_con2(tr4)?.joinToString("_"), )
        assertEquals("c__//_x_//_to_//__//", name_super_con1_to_con2(tr6)?.joinToString("_"), )
        
    }
    
    @Test
    fun name_mult_super() {
        val tr1 = TextReader("part x : Parts::Part [3];").apply { next }
        val tr2 = TextReader("part x references [1.0 .. 273.1] Parts::Part;").apply { next }
        val tr3 = TextReader("part x [2 .. *] {").apply { next }
        val tr4 = TextReader("part p:> k;").apply { next }
        val tr5 = TextReader("part p;").apply { next }
        
        assertEquals( "x_3_Parts::Part", name_mult_super(tr1)?.joinToString("_"),)
        assertEquals( "x_1.0 .. 273.1_Parts::Part", name_mult_super(tr2)?.joinToString("_"),)
        assertEquals( "x_2 .. *_", name_mult_super(tr3)?.joinToString("_"),)
        assertEquals( "p__k", name_mult_super(tr4)?.joinToString("_"),)
        assertEquals( "p__", name_mult_super(tr5)?.joinToString("_"),)
    }
    
    @Test
    fun name_mult_super_const_v() {
        val tr1 = TextReader("feature f::t;").apply { next }
        val tr2 = TextReader("feature a: ScalarValues::Real(1 .. 2) = [1.5 .. 2.5];").apply { next }
        val tr3 = TextReader("feature a: ScalarValues::Boolean(true);").apply { next }
        val tr4 = TextReader("feature p2: l::c2 [1..1];").apply { next }
        
        assertEquals( "f::t___", name_mult_super_const_v(tr1)?.joinToString("_"),)
        assertEquals( "a__ScalarValues::Real(1 .. 2)_[1.5 .. 2.5]", name_mult_super_const_v(tr2)?.joinToString("_"),)
        assertEquals( "a__ScalarValues::Boolean(true)_", name_mult_super_const_v(tr3)?.joinToString("_"),)
        assertEquals( "p2_1..1_l::c2_", name_mult_super_const_v(tr4)?.joinToString("_"),)
    }
    
    @Test
    fun name_super_const_v_unit() {
        val tr1 = TextReader("attribute ambient :>> Real(3.. 8) = 22 °C;").apply { next }
        val tr2 = TextReader("attribute ambient :>> Real := 22 °C;").apply { next }
        val tr3 = TextReader("attribute ambient :>> Real(3 ..8) = 22;").apply { next }
        val tr4 = TextReader("item i default 15 > d {").apply { next }
        
        assertEquals("ambient_Real(3.. 8)_22_°C_=", name_super_const_v_unit(tr1)?.joinToString("_"),  )
        assertEquals("ambient_Real_22_°C_:=", name_super_const_v_unit(tr2)?.joinToString("_"),  )
        assertEquals("ambient_Real(3 ..8)_22__=", name_super_const_v_unit(tr3)?.joinToString("_"),  )
        assertEquals("i__15 > d__default", name_super_const_v_unit(tr4)?.joinToString("_"),  )
    }
    
    @Test
    fun name_super_const_min_max_unit() {
        val tr1 = TextReader("attribute a :>> Real(3..8) [dB] = (48 .. 1) dB;").apply { next }
        val tr2 = TextReader("attribute a :>> Real := [-2.2 .. *] °C;").apply { next }
        val tr3 = TextReader("item i = [7 .. 1] {").apply { next }
        
        assertEquals( "a_Real(3..8)_(48_1)_dB", name_super_const_min_max_unit(tr1)?.joinToString("_"),)
        assertEquals( "a_Real_-2.2_*_°C", name_super_const_min_max_unit(tr2)?.joinToString("_"),)
        assertEquals( "i__7_1_", name_super_const_min_max_unit(tr3)?.joinToString("_"),)
    }
}