package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.model.sysml.RequirementUsage
import com.github.tukcps.sysmd.model.sysml.SatisfyRequirementUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class SatisfyRequirementUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    SatisfyRequirementUsage,
    RequirementUsageImplementation(model,elementId = elementId)
{
    override val satisfiedRequirement: RequirementUsage = TODO()
    override val satisfyingFeature: Feature = TODO()
    override val isModelLevelEvaluable: Boolean
        get() = TODO("Not yet implemented")
    override var upQuantity: com.github.tukcps.sysmd.quantities.VectorQuantity
        get() = TODO("Not yet implemented")
        set(value) {}
    override var downQuantity: com.github.tukcps.sysmd.quantities.VectorQuantity
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        TODO("Not yet implemented")
    }

    override fun evaluate(target: com.github.tukcps.sysmd.model.kerml.Element): Set<com.github.tukcps.sysmd.model.kerml.Element> {
        TODO("Not yet implemented")
    }

    override fun checkCondition(target: com.github.tukcps.sysmd.model.kerml.Element): Boolean {
        TODO("Not yet implemented")
    }

    /** An approximation of the SysML code that was parsed to produce this expression. */
    override val astString: String
        get() = TODO("Not yet implemented")

    /** Initializes this expression subtree recursively.
     * Assigns default domains to `upQuantity` and `downQuantity` of the correct types for this AST.
     * Does not search for properties etc. in the symbol table as these might not be declared.
     */
    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun evalUp() {
        TODO("Not yet implemented")
    }

    override fun evalDown() {
        TODO("Not yet implemented")
    }

    override fun evalUpRec() {
        TODO("Not yet implemented")
    }

    override fun evalDownRec() {
        TODO("Not yet implemented")
    }

    /** Called once to propagate type information, after setting model and adding sub-expressions
     * Populates `type` by adding an appropriate FeatureTyping relation.
     * */
    override fun initType() {
        TODO("Not yet implemented")
    }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        TODO("Not yet implemented")
    }

    override var isNegated: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun clone() = SatisfyRequirementUsageImplementation(model).also { it.updateFrom(this) }
    override val individualDefinition: OccurrenceDefinition?
        get() = TODO("Not yet implemented")
    override var isIndividual: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val occurrenceDefinition: MutableList<Class>
        get() = TODO("Not yet implemented")
    override var portionKind: OccurrenceUsage.PortionKind?
        get() = TODO("Not yet implemented")
        set(value) {}
}
