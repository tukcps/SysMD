package com.github.tukcps.sysmd.exports.systemCElements

enum class DataType {
    REAL,
    INT,
    BOOLEAN,
    STRING,
}

fun DataType.toCPPDataType() : String{
    return when (this) {
        DataType.BOOLEAN ->  "bool"
        DataType.REAL ->  "double"
        DataType.INT ->  "int"
        DataType.STRING ->  "string"
    }
}