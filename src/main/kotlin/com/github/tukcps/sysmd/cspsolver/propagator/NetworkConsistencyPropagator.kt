package com.github.tukcps.sysmd.cspsolver.propagator

import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedValueFeature
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

class NetworkConsistencyPropagator(override val model: Session): Propagator(model) {

    override fun execute(updatedValueFeature: Variable) {
        require(updatedValueFeature is RelatedValueFeature)

        //enforceNodeConsistency(updatedValueFeature)
        //enforcePathConsistency(updatedValueFeature)
    }

    private fun enforceNodeConsistency(updatedValueFeature: RelatedValueFeature) {
        model.builder.conds.x[updatedValueFeature.relatedIndex] = updatedValueFeature.boolSpecs[0].bddLeafOf(model.builder)

        //TODO: Update updates!
    }

    private fun enforcePathConsistency(updatedValueFeature: RelatedValueFeature) {
        if (updatedValueFeature.createdBy.ast != null) {
            updatedValueFeature.createdBy.ast!!.getDependencies().forEach {
                val currentDependency = it
                currentDependency.vectorQuantity.values = mutableListOf(currentDependency.vectorQuantity.values[0].evaluate())
            }
        }
        updatedValueFeature.createdBy.vectorQuantity.values = mutableListOf(updatedValueFeature.createdBy.vectorQuantity.values[0].evaluate())
        //TODO: Update updates!
    }
}