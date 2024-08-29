package com.github.tukcps.sysmd.exports.systemCElements


class Usage(val instanceName : String, var className : String, val amount : Int, val module : Module) {

    /**Full qualified name of the location where the Module is instanced**/
    var instanceLocation : String? = null
}