package ui.tableview

import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.anonConnection
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.anonExp
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.attrAssign
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.attrRange
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.connection
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.connector
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.default
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.defaultMult
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.dirAnonConnection
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.dirConnection
import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns.requireAss
import com.github.tukcps.sysmd.ui.tableview.TableTreeNode
import com.github.tukcps.sysmd.ui.tableview.TableType.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LineBuildFunsTest {
    
    @Test
    fun anonExp() {
        val node1 = TableTreeNode(ANON_EXP, null, values = listOf("f::total_gain >= 2.0"))
        val node2 = TableTreeNode(ANON_EXP, null, values = listOf("x.y(3) and v xor f == 11.8"))
        
        assertEquals("f::total_gain >= 2.0", anonExp(node1)?.trim())
        assertEquals("x.y(3) and v xor f == 11.8", anonExp(node2)?.trim())
    }
    
    @Test
    fun anonConnection() {
        val node1 = TableTreeNode(AN_CON, null, values = listOf("a", "b", "c"))
        val node2 = TableTreeNode(AN_CON, null, values = listOf("a"))
        
        assertEquals("connect (a, b, c)", anonConnection(node1)?.trim())
        assertEquals("connect a", anonConnection(node2)?.trim())
    }
    
    @Test
    fun dirAnonConnection() {
        val node1 = TableTreeNode(AN_COND, null, values = listOf("//", "a", "b", "c", "//", "to", "//", "x", "//"))
        val node2 = TableTreeNode(AN_COND, null, values = listOf("//", "a", "//", "to", "//", "x", "y", "z", "//"))
        val node3 = TableTreeNode(AN_COND, null, values = listOf("//", "a", "b", "c", "//", "to", "//", "x", "y", "z", "//"))
        val node4 = TableTreeNode(AN_COND, null, values = listOf("//", "a", "//", "to", "//", "x", "//"))
        
        assertEquals("connect (a, b, c) to x", dirAnonConnection(node1)?.trim())
        assertEquals("connect a to (x, y, z)", dirAnonConnection(node2)?.trim())
        assertEquals("connect (a, b, c) to (x, y, z)", dirAnonConnection(node3)?.trim())
        assertEquals("connect a to x", dirAnonConnection(node4)?.trim())
        
    }
    
    @Test
    fun connection() {
        val node1 = TableTreeNode(CONN_USE, null, values = listOf("con", "", "//", "a", "b", "c", "//"))
        val node2 = TableTreeNode(CONN_USE, null, values = listOf("con", "", "//", "a"))
        val node3 = TableTreeNode(CONN_USE, null, ":", values = listOf("con", "superCon", "//", "a", "b", "c", "//"))
        val node4 = TableTreeNode(CONN_USE, null, ":", values = listOf("con", "superCon", "//", "a", "//"))
        
        assertEquals("connection con connect (a, b, c)", connection(node1)?.trim())
        assertEquals("connection con connect a", connection(node2)?.trim())
        assertEquals("connection con : superCon connect (a, b, c)", connection(node3)?.trim())
        assertEquals("connection con : superCon connect a", connection(node4)?.trim())
    }
    
    @Test
    fun connector() {
        val node1 = TableTreeNode(CONNECTR, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "b", "c", "//", "to", "//", "x", "//"))
        val node2 = TableTreeNode(CONNECTR, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "//", "to", "//", "x", "y", "z", "//"))
        val node3 = TableTreeNode(CONNECTR, null, values = listOf("some::conUse", "", "//", "a", "b", "c", "//", "to", "//", "x", "y", "z", "//"))
        val node4 = TableTreeNode(CONNECTR, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "//", "to", "//", "x", "//"))
        
        assertEquals("connector some::conUse : conDef from (a, b, c) to x", connector(node1)?.trim())
        assertEquals("connector some::conUse : conDef from a to (x, y, z)", connector(node2)?.trim())
        assertEquals("connector some::conUse from (a, b, c) to (x, y, z)", connector(node3)?.trim())
        assertEquals("connector some::conUse : conDef from a to x", connector(node4)?.trim())
    }
    
    @Test
    fun dirConnection() {
        val node1 = TableTreeNode(CONN_USD, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "b", "c", "//", "to", "//", "x", "//"))
        val node2 = TableTreeNode(CONN_USD, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "//", "to", "//", "x", "y", "z", "//"))
        val node3 = TableTreeNode(CONN_USD, null, values = listOf("some::conUse", "", "//", "a", "b", "c", "//", "to", "//", "x", "y", "z", "//"))
        val node4 = TableTreeNode(CONN_USD, null, ":", values = listOf("some::conUse", "conDef", "//", "a", "//", "to", "//", "x", "//"))
        
        assertEquals("connection some::conUse : conDef connect (a, b, c) to x", dirConnection(node1)?.trim())
        assertEquals("connection some::conUse : conDef connect a to (x, y, z)", dirConnection(node2)?.trim())
        assertEquals("connection some::conUse connect (a, b, c) to (x, y, z)", dirConnection(node3)?.trim())
        assertEquals("connection some::conUse : conDef connect a to x", dirConnection(node4)?.trim())
    }
    
    @Test
    fun default() {
        val node1 = TableTreeNode(PART_USE, null, values = listOf("e", "", ""))
        val node2 = TableTreeNode(PORT_DEF, null, ":", "in", values = listOf("portD","Ports::Port"))
        
        assertEquals("part e", default(node1)?.trim())
        assertEquals("in port def portD : Ports::Port", default(node2)?.trim())
    }
    
    @Test
    fun defaultMult() {
        val node1 = TableTreeNode(PART_USE, null, ":", values = listOf("x", "3", "Parts::Part"))
        val node2 = TableTreeNode(PART_USE, null, "references", values = listOf("x", "1.0 .. 273.1", "Parts::Part"))
        val node3 = TableTreeNode(PART_USE, null, values = listOf("x", "2 .. *", ""))
        val node4 = TableTreeNode(PART_USE, null, ":>", values = listOf("p", "", "k"))
        val node5 = TableTreeNode(PART_USE, null, values = listOf("p", "", ""))
        
        assertEquals("part x : [3] Parts::Part", defaultMult(node1)?.trim())
        assertEquals("part x references [1.0 .. 273.1] Parts::Part", defaultMult(node2)?.trim())
        assertEquals("part x [2 .. *]", defaultMult(node3)?.trim())
        assertEquals("part p :> k", defaultMult(node4)?.trim())
        assertEquals("part p", defaultMult(node5)?.trim())
    }
    
    @Test
    fun requireAss() {
        val node1 = TableTreeNode(ASSERT, null, values = listOf("ass"))
        val node2 = TableTreeNode(ASSERT, null, values = listOf("ass", "x.y(3::U) and v xor (f == 11.8)"))
        
        assertEquals("assert ass", requireAss(node1)?.trim())
        assertEquals("assert ass { x.y(3::U) and v xor (f == 11.8) }", requireAss(node2)?.trim())
    }
    
    @Test
    fun attrAssign() {
        val node1 = TableTreeNode(ATTR_EXP, null, ":>>", values = listOf("ambient", "Real(3.. 8)", "22", "°C", ""))
        val node2 = TableTreeNode(ATTR_EXP, null, ":>>", values = listOf("ambient", "Real", "22", "°C", ""))
        val node3 = TableTreeNode(ATTR_EXP, null, ":>>", values = listOf("ambient", "Real(3 ..8)", "22", "", ""))
        val node4 = TableTreeNode(ITEM, null, values = listOf("i", "", "15 > d", ""))
        val node5 = TableTreeNode(ATTR_EXP, null,":", values = listOf("value","ScalarValues::Real","","",""))
        
        assertEquals("attribute ambient :>> Real(3.. 8) [°C] = 22", attrAssign(node1)?.trim())
        assertEquals("attribute ambient :>> Real [°C] = 22", attrAssign(node2)?.trim())
        assertEquals("attribute ambient :>> Real(3 ..8) = 22", attrAssign(node3)?.trim())
        assertEquals("item i = 15 > d", attrAssign(node4)?.trim())
        assertEquals("attribute value : ScalarValues::Real", attrAssign(node5)?.trim())
    }
    
    @Test
    fun attrRange() {
        val node1 = TableTreeNode(ATTR_RNG, null, ":>>", values = listOf("a", "Real(3..8)", "48", "1", "dB"))
        val node2 = TableTreeNode(ATTR_RNG, null, ":>>", values = listOf("a", "Real", "-2.2", "*", "°C"))
        val node3 = TableTreeNode(ITEM, null, values = listOf("i", "", "7", "1", ""))
        
        assertEquals("attribute a :>> Real(3..8) [dB] = [48 .. 1] dB", attrRange(node1)?.trim())
        assertEquals("attribute a :>> Real [°C] = [-2.2 .. *] °C", attrRange(node2)?.trim())
        assertEquals("item i = [7 .. 1]", attrRange(node3)?.trim())
    }
}