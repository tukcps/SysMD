package com.github.tukcps.sysmd.services.check

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElement
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import kotlin.math.abs


/**
 * We look at an element and its superclass(es).
 * - The subclass properties must be a subset of the superclass properties with the same name.
 * - Maybe additional needs for other types; t.b.d.
 */
fun Session.checkConsistencyOfInheritance(element: Type) {
    element.allSupertypes().asSequence().forEach { supertype ->
        element.ownedElement.asSequence().filterIsInstance<Feature>().forEach { feature ->
            val superclassFeature = supertype.getOwnedElement(
                feature.declaredName,
                feature.declaredShortName)
            if (feature.isFeatureWithValue() && feature !is Multiplicity && superclassFeature is Feature) {
                // Checks for supertype and subclass property
                // Basic requirement for inheritance must hold in all cases otherwise something went wrong before ...
                if (feature.redefining == null && superclassFeature in feature.allSupertypes(true))
                    status.inconsistency("specialization ${feature.escapedName()} has feature that must be specialization of feature of its general class ${supertype.escapedName()}",
                        element = feature
                    )
                when {
                    feature.specializes(repo.realType) -> {
                        //Convert Ranges or owned and supertype to SI
                        feature.typeConstraint.indices.forEach {
                            var ownedRangeSpec = feature.typeConstraint.getOrNull(it) ?: "*..*" // Default: all Reals
                            if (ownedRangeSpec.isBlank()) ownedRangeSpec = "*..*"
                            if ( (feature.type.first()).specializes(repo.realType)) {
                                val ownedRange = Quantity(
                                    builder.real(Range(ownedRangeSpec)),
                                    feature.unitConstraint ?: ""
                                ).getRange()

                            val superClassRangeSpec = if (superclassFeature.typeConstraint.getOrNull(it).isNullOrBlank())
                                Range.Reals
                            else
                                Range(superclassFeature.typeConstraint.getOrNull(it)!!)

                            val extendedRangeSuperclass = builder.real(
                                superClassRangeSpec.min - abs(superClassRangeSpec.min * 0.000001)..superClassRangeSpec.max + abs(superClassRangeSpec.max * 0.000001)
                            )
                            val superClassRange = Quantity(extendedRangeSuperclass, superclassFeature.unitConstraint?:"").getRange()
                            if (ownedRange !in superClassRange && ownedRange != Range.Reals)
                                status.inconsistency(
                                    "value ${feature.typeConstraint} of specialization must be refinement of general ${superclassFeature.escapedName()} with value ${superclassFeature.typeConstraint}",
                                    element = feature
                                )
                            if ((!(feature.type[0]).specializes(superclassFeature.type[0]) && feature.type[0] != superclassFeature.type[0]))
                                status.inconsistency(
                                    "Type of specialization ${feature.type} of '${superclassFeature.escapedName()}' must be the same as '${superclassFeature.type}'",
                                    element = feature
                                )
                        }
                    }
                }

                    feature.specializes(repo.integerType) ->
                        feature.typeConstraint.indices.forEach {
                        if (superclassFeature.indices?.contains(it) != false) {
                            if (IntegerRange(feature.typeConstraint[it]) !in IntegerRange(superclassFeature.typeConstraint[it]) && IntegerRange(
                                    feature.typeConstraint[it]
                                ) != IntegerRange.Integers
                            )
                                status.inconsistency(
                                    "subclass value ${feature.typeConstraint} of ${feature.escapedName()} must be refinement of supertype value ${superclassFeature.typeConstraint}",
                                    element = feature
                                )
                        }
                    }

                    feature.specializes(repo.booleanType) -> { // if (owned.boolSpec !in superclassFeature.boolSpec) {
                        // TODO: Agree with Axel & Sebastian how to handle digital inconsistencies.
                        // reportError(get(it),
                        //    "INCONSISTENCY: subclass value ${owned.boolSpec} of ${owned.effectiveName} must be refinement of supertype value ${superclassProperty.boolSpec}"
                        //)
                    }
                }
            }
            if (superclassFeature is Feature && feature.multiplicityRange !in superclassFeature.multiplicityRange)
                status.inconsistency(
                    message = "INCONSISTENCY: ${feature.qualifiedName}'s multiplicity (${feature.multiplicity()}) must be subset of supertype ${superclassFeature.qualifiedName}'s multiplicity (${superclassFeature.multiplicity()}).",
                    element = superclassFeature
                )
        }
    }
}
