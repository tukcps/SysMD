package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.NamespaceImportImplementation
import com.github.tukcps.sysmd.model.sysml.NamespaceExpose
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: ExposeImplementation. */
class NamespaceExposeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : NamespaceExpose, NamespaceImportImplementation(model,elementId = elementId)
