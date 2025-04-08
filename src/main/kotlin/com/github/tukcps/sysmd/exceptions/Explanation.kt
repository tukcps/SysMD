package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.exceptions.Issue.Kind.*

val explanation = hashMapOf(
    TRACE to "Trace",
    DEBUG to "Debug information",
    INFO  to "Information",
    WARN  to "Warning",
    WARN_INCONSISTENCY to """
        An 'inconsisteny' is reported if a model has some internal contradiction.
           (1) In the inheritance from a general class, the specialization must allow all possible valuations. Hence, constraints for a specialization can only be less strict. E.g., if a part 'car' is constrained to have a weight between 100 and 1000 kg, a subclass 'van' cannot have a value that is in the range of 100 and 2000 kg.
           (2) In the computation of a value, if there is no possible valuation that satisfies all constraints. 
    """.trimIndent(),
    WARN_UNRESOLVED_OWNER to """
        In SysMD notebook, each element has an owner. If the owner is given, e.g., in the title of a cell, and the owner does not exist or is not properly compiled and hence not created, this error is reported. 
        Check where the owner shall be created and fix its creation. 
    """.trimIndent(),
    ERROR to "Error",
    ERROR_UNRESOLVED_NAME to "Unresolved name",
    ERROR_SEMANTIC to "Semantic error",
    ERROR_TYPE_WRONG to "Element with different type expected",
    ERROR_CYCLIC_DEPENDENCY to "Cyclic dependency",
    ERROR_SYNTACTICAL to """
        This error has been caused during parsing the textual representation.
        Check the syntax around the current token given above.
        If there is an error before this error, first fix the error before this one.""".trimIndent(),
    ERROR_LEXICAL to "Lexical error",
    FATAL  to "Fatal error",
    null  to "No explanation",
)