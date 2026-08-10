package com.github.tukcps.sysmd.model.util

import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Namespace

/**
 * All interfaces that are handled as Elements in the element owning hierarchy.
 * Not Elements in general, as Relationships inherit from Element,
 */
interface HandledAsElement:
        Namespace,
        AnnotatingElement,
        Dependency





