package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.FeatureMembership

interface StateSubactionMembership : FeatureMembership {
    enum class StateSubactionKind { Entry, Do, Exit }
    val action: ActionUsage
    var kind: StateSubactionKind?
}
