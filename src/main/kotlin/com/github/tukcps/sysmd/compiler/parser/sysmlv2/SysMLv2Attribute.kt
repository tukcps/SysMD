@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions


/**
 * AttributeDef :- "attribute" "def" Identification ";"
 */
fun KerML.AttributeDef() {
    val attributeDef = sysMLSemantics.AttributeDefinitionSemantics()
    ATTRIBUTE.consume()
    DEF.consume()
    Identification().also {
        attributeDef.identification = it
    }
    attributeDef.create()
    Body(Resolved(attributeDef.created!!))
}



/**
 * Attribute :- "attribute" ["redefines"] Identification ':'
 *                  [All|One] QualifiedName ["(" (Range Unit | true | false ")" ] [ '[' Unit ']' ] [ '=' Expression ]
 */
fun KerML.AttributeUsage() {
    var attribute = semantics.attributeActions()
    alternatives {
        RETURN starts {
            RETURN.consume()
            attribute?.context?.prefixes?.add(OUT)
        }
        // for Calculations, a feature starting with in is an input feature
        IN starts {
            IN.consume()
            attribute?.context?.prefixes?.add(IN)
        }
        OUT starts {
            OUT.consume()
            attribute?.context?.prefixes?.add(OUT)
        }
        INOUT starts {
            INOUT.consume()
            attribute?.context?.prefixes?.add(INOUT)
        }
        others {  }
    }
    ATTRIBUTE.optional()
    alternatives {
        REDEFINES starts  {attribute = AttributeUsageRedefines(attribute)}
        others {
            Identification().also { attribute?.identification = it }
            optional(DP) {
                DP.consume()
                optional(ALL or ONE, consume = true) {
                    when (consumedToken.kind) {
                        ALL -> attribute?.isSufficient = true
                        else -> {}
                    }
                }
                QualifiedName().also { attribute?.type = mutableListOf(it) }
                TypeConstraint().also { attribute?.typeConstraint = it.toMutableList() }

                optional(LCBRACE, consume = true) {
                    Unit().also { attribute?.unitConstraint = it }
                    RCBRACE.consume()
                }
                attribute?.create()
                optional(EQ, consume = true) {
                    val iBeforeExpression = token.indices.first
                    semantics.expression = attribute?.created
                    Expression().also {
                        attribute?.created?.featureWithValue = if (attribute == null) null else AstRoot(model, attribute?.created!!, it)
                        attribute?.created?.indices = iBeforeExpression..consumedToken.indices.last
                        attribute?.created?.expression = if (attribute == null) "" else input.subSequence(attribute?.created?.indices!!).toString().trim()
                    }
                }
            }
            optional(REFERENCES) {
                REFERENCES.consume()
                QualifiedName().also { attribute?.references = it }
                attribute?.createReference()
            }
        }
    }
    Body(Resolved(attribute!!.created!!))
}
/**
 * Attribute :- "redefines" Identification '=' Expression
 */
fun KerML.AttributeUsageRedefines(attribute: FeatureActions?): FeatureActions? {
    REDEFINES.consume()
    Identification().also {
        attribute?.identification = it
        attribute?.redefines = it.name
        optional(DP) { //if the type is specified (maybe more concrete type than superclass)
            DP.consume()
            optional(ALL or ONE, consume = true) {
                when (consumedToken.kind) {
                    ALL -> attribute?.isSufficient = true
                    else -> {}
                }
            }
            QualifiedName().also { attribute?.type = mutableListOf(it) }
            TypeConstraint().also { attribute?.typeConstraint = it.toMutableList() }

            optional(LCBRACE, consume = true) {
                Unit().also { attribute?.unitConstraint = it }
                RCBRACE.consume()
            }
        }
        attribute?.create()
        optional(EQ, consume = true) {
            val iBeforeExpression = token.indices.first
            semantics.expression = attribute?.created
            Expression().also {
                attribute?.created?.featureWithValue =
                    if (attribute == null) null else AstRoot(model, attribute.created!!, it)
                attribute?.created?.indices = iBeforeExpression..consumedToken.indices.last
                attribute?.created?.expression =
                    if (attribute == null) "" else input.subSequence(attribute.created?.indices!!).toString().trim()
            }
        }
    }
    return attribute
}