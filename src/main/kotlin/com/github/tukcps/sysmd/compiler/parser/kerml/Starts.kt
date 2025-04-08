package com.github.tukcps.sysmd.compiler.parser.kerml

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*

val memberPrefixStarts = mutableSetOf(PUBLIC, PRIVATE, PROTECTED, ABSTRACT)

val valuePartStart = setOf(EQ, DPEQ, DEFAULT)

val nonFeatureElementStart = annotatingElementStart +
        setOf(DEPENDENCY, NAMESPACE, TYPE, CLASSIFIER, DATATYPE, CLASS, STRUCT, METACLASS, ASSOC,
            INTERACTION, BEHAVIOR, FUNCTION, PREDICATE, PACKAGE, LIBRARY, STANDARD
        ) + specializationStart + DISJOINING_START + CONJUGATION_START

val typeBodyElementStarts =
    nonFeatureElementStart+ FEATURE_PREFIX_START + featureElementStart + ALIAS + IMPORT