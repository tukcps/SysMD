package com.github.tukcps.sysmd.quantities

class IsolationError internal constructor(msg: String) :
    Exception("Invalid Prefix or Unit: $msg")

class UnknownUnitError internal constructor(msg: String) :
    Exception("Unit is not known: $msg")

class AdditionError internal constructor(msg: String) :
    Exception("Addition impossible or not allowed with these units: $msg")

class SubtractionError internal constructor(msg: String) :
    Exception("Subtraction impossible or not allowed with these units:  $msg")

class TransformationError internal constructor(msg: String) :
    Exception("Units do not match - No transformation possible: $msg")

class SquareRootError internal constructor(msg: String) :
    Exception("Square Root cannot be applied to this unit: $msg")

class BDDError internal constructor(msg: String) :
    Exception("Problem with BDD: $msg")

class DDError internal constructor(msg: String) :
    Exception("Problem with DD: $msg")

class VectorDimensionError internal constructor(msg: String):
    Exception("Problem with vector size: $msg")