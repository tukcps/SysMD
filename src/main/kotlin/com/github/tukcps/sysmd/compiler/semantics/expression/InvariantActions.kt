package com.github.tukcps.sysmd.compiler.semantics.expression

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.MultiplicityImplementation
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification


class InvariantActions(
    var context: ActionsContext,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var multiplicity: Multiplicity? = null,
    var references: QualifiedName? = null,
    var ast: AstRoot? = null,
    var indices: IntRange = 0..0,
    var created: Feature? = null,
){
     fun create() {
        // if classId is referring to a class, we simply declare the occurrence of this class.
        created = InvariantImplementation(
            declaredName = identification!!.name,
            declaredShortName = identification!!.shortName,
            textualRepresentation = mutableListOf(context.textualRepresentation),
        ).also {
            it.featureWithValue = ast
            it.indices = indices
        }

        // remember namespace for the following AST elements that need it for constructor
        context.namespace = created!!

        val specialization = FeatureTypingImplementation(
            owner = Resolved(ref = created!!),
            typedFeature = Resolved(ref = created!!),
            type = Resolved(str = "ScalarValues::Boolean")
        )

        val multiplicity = MultiplicityImplementation(
            name = "multiplicity",
            owner = Resolved(ref = created!!),
            multiplicity = IntegerRange(1, 1).toString()
        )

        if (references != null)
            context.addReferenceSubsetting(created!!, null, references!!)

        context.model.addUnownedElement(created!!, (owner))
        context.model.addUnownedElement(multiplicity, (owner + "::${identification!!.toName()}"))
        context.model.addUnownedElement(specialization, (owner + "::${identification!!.toName()}"))
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}
