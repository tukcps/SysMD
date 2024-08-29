package com.github.tukcps.sysmd.exports

class Constraint(
    val constraintName: String,
    val statement: String,
    val referenceValue: Double,
    val referenceUnit: String,
    val operator: String,
    val attributeQUalifiedName: String,
    val unit : String
)
