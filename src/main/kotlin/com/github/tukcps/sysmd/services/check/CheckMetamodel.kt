package com.github.tukcps.sysmd.services.check

/**
 * This method implements, for each Element of the Model, a check that
 * verifies the constraints specified in the Metamodel of KerML and SysMLv2.
 */
interface CheckMetamodel {
    fun checkMetamodel() {}
}
