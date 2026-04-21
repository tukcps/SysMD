package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

interface FeatureValue: OwningMembership
{
   var isInitial: Boolean
   var isDefault: Boolean

   /** The Feature to be provided a value */
   var featureWithValue : Feature
      get() = membershipOwningNamespace as Feature
      set(value) { membershipOwningNamespace = value }

   /** The Expression that provides the value of the featureWithValue as its result */
   var value : Expression
      get() = ownedMemberElement as Expression
      set(value) { ownedMemberElement = value }

   override fun clone() : FeatureValue
}