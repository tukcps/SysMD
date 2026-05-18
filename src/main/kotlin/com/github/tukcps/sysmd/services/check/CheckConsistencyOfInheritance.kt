package com.github.tukcps.sysmd.services.check

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.services.session.*
import io.github.tukcps.aadd.values.*
import kotlin.math.*


/**
 * We look at an element and its superclass(es).
 * - The subclass properties must be a subset of the superclass properties with the same name.
 * - Maybe additional needs for other types; t.b.d.
 */
fun Session.checkConsistencyOfInheritance(element: Type) {
	element.allSupertypes().forEach { supertype ->
		element.ownedElement.filterIsInstance<Feature>().forEach { feature ->
			val superclassFeature = supertype.getOwnedElement(Identification(feature)) as? Feature ?: return@forEach

			if(feature.isFeatureWithValue() && feature !is Multiplicity) {
				if(feature.redefining === null && superclassFeature in feature.allSupertypes(true))
					status.inconsistency(
						"specialization ${feature.escapedName()} has feature that must be specialization of feature of its general class ${supertype.escapedName()}",
						element = feature
					)

				when {
					feature.specializes(repo.realType) -> {
						feature.typeConstraint.indices.forEach {
							val ownedRangeSpec = feature.typeConstraint.getOrNull(it)?.ifBlank { "*..*" } ?: "*..*"
							if(feature.type.first().specializes(repo.realType)) {
								val ownedRange = Quantity(builder.real(Range(ownedRangeSpec)), feature.unitConstraint ?: "").getRange()

								val superClassRangeSpec = superclassFeature.typeConstraint.getOrNull(it)?.ifBlank { "0..*" } ?: "0..*"
								val superClassRangeObj = Range(superClassRangeSpec)

								val extendedRangeSuperclass = builder.real(
									superClassRangeObj.min - abs(superClassRangeObj.min * 0.000001)..
									superClassRangeObj.max + abs(superClassRangeObj.max * 0.000001)
								)
								val superClassRange = Quantity(extendedRangeSuperclass, superclassFeature.unitConstraint ?: "").getRange()
								
								// Check range compatibility with unit conversion
								// Quantity handles unit conversion internally - values are always in SI units
								val ownedQuantity = Quantity(builder.real(Range(ownedRangeSpec)), feature.unitConstraint ?: "")
								val superQuantity = Quantity(builder.real(Range(superClassRangeSpec)), superclassFeature.unitConstraint ?: "")
								
								// Get the ranges (Quantity handles unit conversion internally)
								val ownedRangeFromQuantity = ownedQuantity.getRange()
								val superRangeFromQuantity = superQuantity.getRange()
								
								// Compare the ranges directly (both are in SI units now)
								if (ownedRangeFromQuantity !in superRangeFromQuantity && ownedRangeFromQuantity != Range.Reals) {
									val rangesAreEffectivelyIdentical =
										abs(ownedRangeFromQuantity.min - superRangeFromQuantity.min) < 0.001 &&
										abs(ownedRangeFromQuantity.max - superRangeFromQuantity.max) < 0.001

									if (!rangesAreEffectivelyIdentical) {
										val featureUnit = feature.unitConstraint ?: ""
										val superUnit = superclassFeature.unitConstraint ?: ""
										val unitInfo = if (featureUnit.isNotEmpty() || superUnit.isNotEmpty()) {
											" (unit: ${featureUnit.ifEmpty { superUnit }})"
										} else {
											""
										}
										
										status.inconsistency(
											"value ${feature.typeConstraint}$unitInfo of specialization must be refinement of general ${superclassFeature.escapedName()} with value ${superclassFeature.typeConstraint}${if (superUnit.isNotEmpty()) " (unit: $superUnit)" else ""}",
											element = feature
										)
									}
								}
								if(!(feature.type[0]).specializes(superclassFeature.type[0]) && feature.type[0] != superclassFeature.type[0]) {
									val isLegitimateRedefinition = try {
										val featureDataType = feature.type[0]
										val superDataType = superclassFeature.type.firstOrNull()
											?.let { if (it is AttributeUsage && it.type.isNotEmpty()) it.type[0] else it }
										featureDataType == superDataType
									} catch (_: Exception) {
										true
									}

									if (!isLegitimateRedefinition) {
										val featureTypeName = feature.type[0].let { it::class.simpleName } ?: "unknown"
										val superTypeName = superclassFeature.type[0].let { it::class.simpleName } ?: "unknown"
										val featureName = feature.type[0].escapedName() ?: "unnamed"
										val superName = superclassFeature.type[0].escapedName() ?: "unnamed"

										status.inconsistency(
											"Type mismatch in feature '${feature.escapedName()}': Cannot use '$featureName' ($featureTypeName) where '$superName' ($superTypeName) is expected.\n" +
											"Hint: Ensure the redefined feature uses a compatible type. " +
											"If you're trying to assign a specific value, make sure the types are compatible or use explicit type conversion.",
											element = feature
										)
									}
								}
							}
						}
					}

					feature.specializes(repo.integerType) -> feature.typeConstraint.indices.forEach {
						if(superclassFeature.indices?.contains(it) != false) {
							try {
								val featureRange = IntegerRange(feature.typeConstraint[it])
								val superRange = IntegerRange(superclassFeature.typeConstraint[it])

								if (featureRange != superRange && featureRange !in superRange && featureRange != IntegerRange.Integers) {
									status.inconsistency(
										"value ${feature.typeConstraint[it]} of specialization must be refinement of general value with value ${superclassFeature.typeConstraint[it]}",
										element = feature
									)
								}
							} catch (_: Exception) {
							}
						}
					}

					feature.specializes(repo.booleanType) -> { /* Boolean handling TODO */ }
				}
			}

			if(feature.multiplicityRange !in superclassFeature.multiplicityRange)
				status.inconsistency(
					message = "INCONSISTENCY: ${feature.qualifiedName}'s multiplicity (${feature.multiplicity()}) must be subset of supertype ${superclassFeature.qualifiedName}'s multiplicity (${superclassFeature.multiplicity()}).",
					element = superclassFeature
				)

			if(element is Function && supertype is Function) {
				val membership = feature.owningRelationship as? ParameterMembership
				val superMembership = superclassFeature.owningRelationship as? ParameterMembership

				if(membership != null && superMembership != null)
					membership.parameterIndex = superMembership.parameterIndex
			}
		}
	}
}
