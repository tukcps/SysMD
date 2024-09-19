package com.github.tukcps.sysmd.ui.styles

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.tukcps.sysmd.ui.tableview.TableType
import com.github.tukcps.sysmd.ui.tableview.TableType.*


//TODO test in dark mode
data class TableColors(
    val back: Color,
    val backInput: Color,
    val text: Color,
    val textInput: Color,
    val border: Color,
    val button: Color,
    val buttonHovered: Color,
    val buttonDisabled: Color,
    val delete: Color,
    val part: Color,
    val port: Color,
    val partDef: Color,
    val portDef: Color,
    val attrDef: Color,
    val attr: Color,
    val undirConn: Color,
    val dirConn: Color,
)
@Composable
fun getTableColors(): TableColors = TableColors(
    back = colorScheme.background,
    backInput = colorScheme.background,
    text = colorScheme.onBackground,
    textInput = colorScheme.onBackground,
    border = colorScheme.onBackground,
    button = colorScheme.outline.copy(.75f),
    buttonHovered = colorScheme.primary,
    buttonDisabled = colorScheme.outlineVariant,
    delete = Color.Red.copy(.75f),
    part = Color(0.0f, 1.0f, 1.0f, 0.15f),
    port = Color(0.5f, 0.5f, 1.0f, 0.15f),
    partDef = Color(1.0f, 1.0f, 0.0f, 0.15f),
    portDef = Color(0.5f, 1.0f, 0.5f, 0.15f),
    attrDef = Color(0.5f, 0.0f, 0.0f, 0.15f),
    attr = colorScheme.primaryContainer,
    undirConn = Color(0.0f, 1.0f, 0.0f, 0.15f),
    dirConn = Color(0.0f, 0.0f, 1.0f, 0.15f),
)

@Composable
fun defaultTableColorOf(type: TableType) = mapOf(
    ANON to colorScheme.errorContainer,
    AN_CON to getTableColors().dirConn,
    AN_COND to getTableColors().undirConn,
    PACKAGE to getTableColors().back,
    IMPORT to getTableColors().back,
    ASSUME to getTableColors().part,
    REQUIRE to getTableColors().part,
    ASRT_DEF to getTableColors().partDef,
    ASSERT to getTableColors().part,
    ASUM_DEF to getTableColors().partDef,
    DATATYPE to getTableColors().part,
    DATT_DEF to getTableColors().partDef,
    INV to getTableColors().part,
    INV_DEF to getTableColors().partDef,
    CONSTRN to getTableColors().part,
    CNST_DEF to getTableColors().partDef,
    EXPR to getTableColors().part,
    EXPR_DEF to getTableColors().partDef,
    CLASS to getTableColors().part,
    CLSS_DEF to getTableColors().partDef,
    CTR_DEF to getTableColors().partDef,
    SUBJECT to getTableColors().part,
    SUBJ_DEF to getTableColors().partDef,
    REQ_DEF to getTableColors().partDef,
    REQMENT to getTableColors().part,
    RQNT_DEF to getTableColors().partDef,
    ASSOC to getTableColors().part,
    ASOC_DEF to getTableColors().partDef,
    ATTR_DEF to getTableColors().attrDef,
    CONN_DEF to getTableColors().partDef,
    INTR_DEF to getTableColors().partDef,
    ITEM_DEF to getTableColors().partDef,
    PART_DEF to getTableColors().partDef,
    PORT_DEF to getTableColors().portDef,
    PORT_USE to getTableColors().port,
    CONN_USE to getTableColors().undirConn,
    INTR_USE to getTableColors().undirConn,
    CONNECTR to getTableColors().dirConn,
    INTR_USD to getTableColors().dirConn,
    CONN_USD to getTableColors().dirConn,
    PART_USE to getTableColors().part,
    ITEM to getTableColors().part,
    FEAT to getTableColors().attr,
    ATTR_EXP to getTableColors().attr,
    ATTR_RNG to getTableColors().attr,
    )[type]
