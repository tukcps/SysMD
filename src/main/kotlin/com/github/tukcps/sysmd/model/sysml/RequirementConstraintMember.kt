package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature

interface RequirementConstraintMember: Feature {
    enum class Kind { ASSUME, REQUIRE }
    var kind: Kind
}