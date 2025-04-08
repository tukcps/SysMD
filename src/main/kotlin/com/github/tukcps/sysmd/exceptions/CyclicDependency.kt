package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element

/**
 * A cyclic dependency between two elements of a model has occurred.
 * E.g., a type that is defined by itself.
 */
class CyclicDependency(
    message: String = "A definition has been cyclic",
    element: Element? = null,
): SysMDError(
    message = message,
    element = element,
)