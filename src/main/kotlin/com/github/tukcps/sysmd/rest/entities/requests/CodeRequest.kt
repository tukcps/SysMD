package com.github.tukcps.sysmd.rest.entities.requests

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language

data class CodeRequest(
    val language: String = Language.SYS_ML.name,
    val namespace: String? = null,
    val body: String = "",
    val runlevel: String = Runlevel.NAMES_RESOLVED.toString(),
)