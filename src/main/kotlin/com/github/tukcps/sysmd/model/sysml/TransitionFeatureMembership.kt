package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.FeatureMembership
import com.github.tukcps.sysmd.model.kerml.Step

interface TransitionFeatureMembership : FeatureMembership {
    enum class TransitionFeatureKind { Trigger, Guard, Effect }

    var kind: TransitionFeatureKind?
    val transitionFeature: Step
}
