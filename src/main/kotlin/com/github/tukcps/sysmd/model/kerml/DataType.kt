package com.github.tukcps.sysmd.model.kerml

/**
 * Data types are classifiers that classify data values (see 9.2.2.2.2).
 * Certain primitive data types have specified extents of values, such as the numerical and other types
 * from the ScalarValues library model (see 9.3.2).
 * Other data types have features whose values can distinguish one instance of the data type from another.
 * But, otherwise, different data values are not distinguishable.
 * This means that data types cannot also be classes or associations, or share instances with them.
 * It also means that data types classify things that do not exist in time or space,
 * because they require changing relations to other things.
 * The feature values of a data value cannot change over time, because different feature values would inherently
 * identify a different data value.
 */
interface DataType: Classifier