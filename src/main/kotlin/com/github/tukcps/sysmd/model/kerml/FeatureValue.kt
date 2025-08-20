package com.github.tukcps.sysmd.model.kerml

interface FeatureValue: OwningMembership {
   var isInitial: Boolean
   var isDefault: Boolean
}