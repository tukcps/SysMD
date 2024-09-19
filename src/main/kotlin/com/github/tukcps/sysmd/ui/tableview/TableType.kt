package com.github.tukcps.sysmd.ui.tableview

import com.github.tukcps.sysmd.ui.tableview.LineBuildFuns as LF
import com.github.tukcps.sysmd.ui.tableview.ValBuildFuns as TF

enum class TableType(
    //sysml/-md keyword
    val kw: String? = null,
    //display name
    val rlName: String? = null,
    //default column setup (amount, names) of this table
    val columnSetup: List<String>? = null,
    //build function used to build this table's values from text
    val buildValues: TableFun = { null },
    //function used to convert this table's values into a line of sysml2 code
    val buildLine: LineFun = { null },
) {
// @formatter:off
    //order of these enums = order of the tables
//  ANON: unknown/hidden table
    ANON,
    ANON_EXP(""               ,""                            ,listOf()                                                          ,TF.be                           ,LF.anonExp          ),
    PACKAGE ("package"        ,"Package"                     ,listOf("", "Name")                                                ,TF.name                         ,LF.default          ),
    IMPORT  ("import"         ,"Import"                      ,listOf("", "Name")                                                ,TF.name                         ,LF.default          ),
//  defs {
//  creatable:
    INTR_DEF("interface def"  ,"Interface Definition"        ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    ITEM_DEF("item def"       ,"Item Definition"             ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    ATTR_DEF("attribute def"  ,"Attribute Definition"        ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    CLSS_DEF("class def"      ,"Class Definition"            ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    PART_DEF("part def"       ,"Part Definition"             ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    PORT_DEF("port def"       ,"Port Definition"             ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    RQNT_DEF("requirement def","Requirement Definition"      ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    CONN_DEF("connection def" ,"Connection Definition"       ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
//  unused/not creatable through UI:
    ASRT_DEF("assert def"     ,"Assertion Definition"        ,listOf("", "Name", "Assertion")                                   ,TF.name_be                      ,LF.requireAss       ),
    ASUM_DEF("assume def"     ,"Assume Definition"           ,listOf("", "Name", "Assumption")                                  ,TF.name_be                      ,LF.requireAss       ),
    DATT_DEF("datatype def"   ,"Datatype Definition"         ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    INV_DEF ("inv def"        ,"Inv Definition"              ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    CNST_DEF("constraint def" ,"Constraint Definition"       ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    EXPR_DEF("expression def" ,"Expression Definition"       ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    CTR_DEF ("connector def"  ,"Connector Definition"        ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    SUBJ_DEF("subject def"    ,"Subject Definition"          ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    REQ_DEF ("require def"    ,"Require Definition"          ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    ASOC_DEF("assoc def"      ,"Assoc Definition"            ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default),
//  }
//  uses {
    ASSOC   ("assoc"          ,"Assoc"                       ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    DATATYPE("datatype"       ,"Datatype"                    ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    CLASS   ("class"          ,"Class"                       ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    SUBJECT ("subject"        ,"Subject"                     ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    ASSERT  ("assert"         ,"Assertion"                   ,listOf("", "Name", "Assertion")                                   ,TF.name_be                      ,LF.requireAss       ),
    ASSUME  ("assume"         ,"Assumption"                  ,listOf("", "Name", "Assumption")                                  ,TF.name_be                      ,LF.requireAss       ),
    REQUIRE ("require"        ,"Require"                     ,listOf("", "Name", "Requirement")                                 ,TF.name_be                      ,LF.requireAss       ),
    PART_USE("part"           ,"Part"                        ,listOf("", "Name", "Amount", "is a")                              ,TF.name_mult_super              ,LF.defaultMult      ),
    ITEM    ("item"           ,"Item"                        ,listOf("", "Name", "Amount", "is a")                              ,TF.name_mult_super              ,LF.defaultMult      ),
    FEAT    ("feature"        ,"Feature"                     ,listOf("", "Name", "Amount", "is a", "Value")                     ,TF.name_mult_super_const_v      ,LF.defaultMult      ),
    PORT_USE("port"           ,"Port"                        ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    ATTR_EXP("attribute"      ,"Attribute Assignment"        ,listOf("", "Name", "Value Type", "Value", "Unit")                 ,TF.name_super_const_v_unit      ,LF.attrAssign       ),
    ATTR_RNG("attribute"      ,"Attribute Range"             ,listOf("", "Name", "Value Type", "Min Value", "Max Value", "Unit"),TF.name_super_const_min_max_unit,LF.attrRange        ),
    EXPR    ("expression"     ,"Expression"                  ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    INV     ("inv"            ,"Inv"                         ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    REQMENT ("requirement"    ,"Requirement"                 ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
    AN_CON  (""               ,"Unnamed Connection"          ,listOf("Connected Elements")                                      ,TF.con0                         ,LF.anonConnection   ),
    AN_COND (""               ,"Directed Unnamed Connection" ,listOf("From", "To")                                              ,TF.con1_con2                    ,LF.dirAnonConnection),
    INTR_USD("interface"      ,"Directed Interface"          ,listOf("", "Name", "is a", "From", "To")                          ,TF.name_super_con1_to_con2      ,LF.dirConnection    ),
    INTR_USE("interface"      ,"Interface"                   ,listOf("", "Name", "is a")                                        ,TF.name_super_con0              ,LF.connection       ),
    CONNECTR("connector"      ,"Connector"                   ,listOf("", "Name", "is a", "From","To")                           ,TF.name_super_con1_to_con2      ,LF.connector        ),
    CONN_USD("connection"     ,"Directed Connection"         ,listOf("", "Name", "is a", "From", "To")                          ,TF.name_super_con1_to_con2      ,LF.dirConnection    ),
    CONN_USE("connection"     ,"Connection"                  ,listOf("", "Name", "is a")                                        ,TF.name_super_con0              ,LF.connection       ),
    CONSTRN ("constraint"     ,"Constraint"                  ,listOf("", "Name", "is a")                                        ,TF.name_super                   ,LF.default          ),
//  }
    ERROR,
    ;
// @formatter:on
    
    //general constant Table attributes
    companion object{
        const val ROWS_DEFAULT = 2
        //column setup for variable length tables (which columns are used for values? null means: just put all of them in a row below)
        val TableType.varColSetup: List<Boolean?>? get() = Maps.varColSetup[this]
        //returns the allowed types of children that can be added to this table through UI
        val TableType.recommendedChildren get() = Maps.allowedChildren[this]
        //returns the table type mapped to this keyword
        fun tableTypeOf(keyword: String?): TableType = Maps.typeMap[keyword] ?: ANON
        //all currently supported table types
        val allTypes = linkedHashSetOf(
            INTR_DEF, ITEM_DEF, ATTR_DEF, CLSS_DEF, PART_DEF, PORT_DEF, RQNT_DEF, CONN_DEF, ASRT_DEF, ASUM_DEF, DATT_DEF, INV_DEF, CNST_DEF,
            EXPR_DEF, CTR_DEF, SUBJ_DEF, REQ_DEF, ASOC_DEF, PACKAGE, IMPORT, ASSOC, DATATYPE, CLASS, SUBJECT, ASSERT, ASSUME, REQUIRE, PART_USE,
            ITEM, FEAT, PORT_USE, ATTR_EXP, ATTR_RNG, EXPR, INV, REQMENT, INTR_USD, INTR_USE, CONNECTR, CONN_USD, CONN_USE, CONSTRN
        )
    }
    
    //all maps needed for the companion object
    private object Maps{
        //which children can be added in the UI for each table type
        private val default: LinkedHashSet<TableType> = linkedHashSetOf(PORT_USE, PART_USE, ATTR_EXP, REQMENT, INTR_USE, CONN_USE)
        
        private val requirementDefault: LinkedHashSet<TableType> = linkedHashSetOf(ATTR_EXP, SUBJECT, REQUIRE, ASSERT, ASSUME)
        
        private val pack: LinkedHashSet<TableType> = linkedHashSetOf(
            *(arrayOf(IMPORT, INTR_DEF, ITEM_DEF, ATTR_DEF, PART_DEF, PORT_DEF, RQNT_DEF, CONN_DEF) + default)
        )
    
        val allowedChildren = hashMapOf (
            ANON     to linkedHashSetOf(),
            ASRT_DEF to linkedHashSetOf(),
            ASUM_DEF to linkedHashSetOf(),
            DATT_DEF to linkedHashSetOf(),
            INV_DEF  to linkedHashSetOf(),
            CNST_DEF to linkedHashSetOf(),
            EXPR_DEF to linkedHashSetOf(),
            CLSS_DEF to linkedHashSetOf(),
            CTR_DEF  to linkedHashSetOf(),
            SUBJ_DEF to linkedHashSetOf(),
            REQ_DEF  to linkedHashSetOf(),
            RQNT_DEF to requirementDefault,
            ASOC_DEF to default,
            ATTR_DEF to linkedHashSetOf(),
            CONN_DEF to linkedHashSetOf(),
            INTR_DEF to linkedHashSetOf(PORT_USE, PART_USE),
            ITEM_DEF to linkedHashSetOf(),
            PART_DEF to default,
            PORT_DEF to default,
            AN_CON   to linkedHashSetOf(),
            AN_COND  to linkedHashSetOf(),
            PACKAGE  to pack,
            IMPORT   to linkedHashSetOf(),
            ASSUME   to linkedHashSetOf(),
            REQUIRE  to linkedHashSetOf(),
            ASSERT   to linkedHashSetOf(),
            DATATYPE to linkedHashSetOf(),
            INV      to linkedHashSetOf(),
            CONSTRN  to linkedHashSetOf(),
            EXPR     to linkedHashSetOf(),
            CLASS    to linkedHashSetOf(ATTR_RNG, ATTR_EXP, FEAT, IMPORT, INV, EXPR),
            SUBJECT  to linkedHashSetOf(),
            REQMENT  to requirementDefault,
            ASSOC    to linkedHashSetOf(IMPORT, FEAT, INV),
            PART_USE to default,
            PORT_USE to linkedHashSetOf(ATTR_EXP, ATTR_RNG, ITEM),
            CONN_USE to linkedHashSetOf(), //TODO add end notation to connection, interface
            INTR_USE to linkedHashSetOf(),
            CONNECTR to linkedHashSetOf(),
            INTR_USD to linkedHashSetOf(),
            CONN_USD to linkedHashSetOf(),
            ITEM     to linkedHashSetOf(),
            FEAT     to linkedHashSetOf(),
            ATTR_EXP to linkedHashSetOf(),
            ATTR_RNG to linkedHashSetOf(),
        )
        //see above
        val varColSetup = hashMapOf(
            AN_CON   to listOf(null),
            AN_COND  to listOf(true,true),
            CONN_USE to listOf(null),
            INTR_USE to listOf(null),
            CONNECTR to listOf(false,false,false,true,true),
            INTR_USD to listOf(false,false,false,true,true),
            CONN_USD to listOf(false,false,false,true,true),
        )
        //keyword to type map, ambiguous mappings commented out
        val typeMap: Map<String?, TableType> = hashMapOf(
            // @formatter:off
            null        to ANON,
            //ANON.kw     to ANON,
            //AN_CON.kw   to AN_CON,
            //AN_COND.kw  to AN_COND,
            PACKAGE.kw  to PACKAGE,
            IMPORT.kw   to IMPORT,
            ASSUME.kw   to ASSUME,
            REQUIRE.kw  to REQUIRE,
            ASRT_DEF.kw to ASRT_DEF,
            ASSERT.kw   to ASSERT,
            ASUM_DEF.kw to ASUM_DEF,
            DATATYPE.kw to DATATYPE,
            DATT_DEF.kw to DATT_DEF,
            INV.kw      to INV,
            INV_DEF.kw  to INV_DEF,
            CONSTRN.kw  to CONSTRN,
            CNST_DEF.kw to CNST_DEF,
            EXPR.kw     to EXPR,
            EXPR_DEF.kw to EXPR_DEF,
            CLASS.kw    to CLASS,
            CLSS_DEF.kw to CLSS_DEF,
            CTR_DEF.kw  to CTR_DEF,
            SUBJECT.kw  to SUBJECT,
            SUBJ_DEF.kw to SUBJ_DEF,
            REQ_DEF.kw  to REQ_DEF,
            REQMENT.kw  to REQMENT,
            RQNT_DEF.kw to RQNT_DEF,
            ASSOC.kw    to ASSOC,
            ASOC_DEF.kw to ASOC_DEF,
            ATTR_DEF.kw to ATTR_DEF,
            CONN_DEF.kw to CONN_DEF,
            INTR_DEF.kw to INTR_DEF,
            ITEM_DEF.kw to ITEM_DEF,
            PART_DEF.kw to PART_DEF,
            PORT_DEF.kw to PORT_DEF,
            PORT_USE.kw to PORT_USE,
            CONN_USE.kw to CONN_USE,
            INTR_USE.kw to INTR_USE,
            CONNECTR.kw to CONNECTR,
            //INTR_USD.kw to INTR_USD,
            //CONN_USD.kw to CONN_USD,
            PART_USE.kw to PART_USE,
            ITEM.kw     to ITEM,
            FEAT.kw     to FEAT,
            //ATTR_EXP.kw to ATTR_EXP,
            ATTR_RNG.kw to ATTR_RNG,
            // @formatter:on
        )
    }
}
