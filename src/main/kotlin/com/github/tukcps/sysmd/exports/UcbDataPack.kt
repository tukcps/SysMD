package com.github.tukcps.sysmd.exports

import com.github.tukcps.sysmd.exports.systemCElements.Channel
import com.github.tukcps.sysmd.exports.systemCElements.Module


class UcbDataPack(
     val modules : MutableList<Module>,
     val channels : MutableList<Channel>,
     val macros : BooleanArray,
     val requirements : MutableList<Requirement>,
     val packageName : String)