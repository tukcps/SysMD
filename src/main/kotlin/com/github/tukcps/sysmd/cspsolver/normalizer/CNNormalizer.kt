package com.github.tukcps.sysmd.cspsolver.normalizer

import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.functions.AstNot
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.services.session.Session

/**
 * Class to normalize constraints, i.e., for each combination of variables at maximum one constraint exists.
 */
class CNNormalizer {

    /**
     * a counter which is used to give the normalized constraints a name
     */
    private var counter : Int = 0

    /**
     * Normalization of constraints which are all expected to be boolean
     *
     * @param model the model which will be normalized (expected to contain only boolean constraints)
     * @return the normalized properties
     */
    fun normalizeBooleanConstraints(model: Session) : NormalizedProperties {

        var originalProperties : MutableList<Variable> = model.get().filter {
            it is Variable ||( it is Feature && it.variable is Variable ) }
            .map { if (it is Variable) it else (it as Feature).variable as Variable } as MutableList<Variable>
        originalProperties = originalProperties.filter { it.baseType == Variable.BaseType.Bool } as MutableList<Variable>

        originalProperties = extractRelevantProperties(model.builder.conds.indexes, originalProperties)


        // find the properties that need normalization and store them in a map with the leaves as a key
        val propertiesToNormalize : MutableMap<Collection<AstLeaf>,List<Variable>> = propertiesToBeNormalized(originalProperties)

        var normalizedProperties : MutableList<SimpleProperty<XBool>> = mutableListOf()

        // check if there are properties to be normalized
        if (propertiesToNormalize.isNotEmpty()) {

            // remove the properties which will be normalized since they will be replaced by the normalized one
            val removeList : MutableList<Variable> = mutableListOf()
            for(key in propertiesToNormalize.keys) {
                for(property in propertiesToNormalize[key]!!) {
                    removeList.add(property)
                }
            }
            for (property in removeList) {
                originalProperties.remove(property)
            }

            // normalization
            normalizedProperties = normalizationBoolean(propertiesToNormalize)
        }

        // add the properties that need no normalization
        var simpleProp : SimpleProperty<XBool>
        for(prop in originalProperties) {
             simpleProp = SimpleProperty(name = prop.membership.path(), expression = prop.feature.expression?:"", dd = prop.bdd(), simpleAst = prop.ast?.let {
                 SimpleAstRoot(it.dependency)
             }, valueSpecs = mutableListOf(prop.valueSpecs[0]))
            normalizedProperties.add(simpleProp)
        }


        val notNegatedProps = normalizedProperties as ArrayList
        val props = arrayListOf<SimpleProperty<XBool>>()//normalizedProperties.properties as ArrayList

        notNegatedProps.forEach {//for (prop in notNegatedProps) {
            if (it.boolSpec.firstOrNull()?.toString() == "False") {
                val negated = negate(it, model)

                props.remove(it)
                props.add(negated)
            }
            else props.add(it)
        }

        return NormalizedProperties(props, model)
    }

    /**
     *Extracting properties that are relevant for normalization
     *
     * @param variableIndexes the HashMap from the conds in the builder, mapping variable name to its index
     * @param originalProperties list of properties from which the relevant properties for normalization need to be extracted
     * @return the relevant properties for normalization out of the given list
     */
    private fun extractRelevantProperties(variableIndexes: HashMap<String, Int>, originalProperties: MutableList<Variable>): MutableList<Variable> {

        val properties = mutableListOf<Variable>()

        for(prop in originalProperties) {

            // prop defines a variable
            if (variableIndexes.containsKey(prop.elementId.toString())) {
                properties.add(prop)
                continue
            }

            // prop has an AST
            if(prop.ast != null) {
                // check if it is a property representing a condition/constraint
                var leavesAreVariables = true
                for(leaf in prop.ast?.getLeaves()!!) {
                    if(leaf.literalVal != null) {
                        leavesAreVariables = false
                    }
                }
                if(leavesAreVariables) {
                    properties.add(prop)
                }
            }

        }

        return properties
    }

    /**
     * Normalization of properties that are all assumed to be boolean
     *
     * @param propertiesToNormalize map containing leaves as key and mapped properties as value
     * @return list of normalized properties
     */
    private fun normalizationBoolean(propertiesToNormalize: MutableMap<Collection<AstLeaf>, List<Variable>>): MutableList<SimpleProperty<XBool>> {

        val normalizedProperties : MutableList<SimpleProperty<XBool>> = mutableListOf()

        var propertiesForVarSet : List<Variable>
        var normalizedProperty : SimpleProperty<XBool>

        for(entry in propertiesToNormalize) {
            propertiesForVarSet = entry.value
            normalizedProperty = normalizedBooleanProperty(propertiesForVarSet)
            normalizedProperties.add(normalizedProperty)
        }

        return normalizedProperties
    }

    /**
     * Normalization of a list of boolean properties
     *
     * @param propertiesForVarSet the list of the properties containing the same set of variables
     * @return the property which results from the normalization of the given properties
     */
    private fun normalizedBooleanProperty(propertiesForVarSet: List<Variable>): SimpleProperty<XBool> {

        var currentBDD: BDD = propertiesForVarSet.first().ast?.bdd ?: throw RuntimeException("Property has no AST/BDD")
        var lastBDD: BDD? = null

        var currentAst: AstNode = propertiesForVarSet.first().ast!!
        var lastAst: AstNode? = null

        // calculate the normalized AST
        for (property in propertiesForVarSet) {

            if (!property.ast?.isBool!!) {
                throw RuntimeException("The boolean normalization was called although one of the properties is not boolean.")
            }

            // apply "and" on the BDDs
            currentBDD = property.ast!!.bdd
            if (lastBDD != null) {
                currentBDD = currentBDD.and(lastBDD)
            }
            lastBDD = currentBDD

            // apply "and" on the ASTs
            currentAst = property.ast!!
            if (lastAst != null) {
                currentAst = com.github.tukcps.sysmd.model.expression.AstBinOp(currentAst, Token.Kind.AND, lastAst)
            }
            lastAst = currentAst
        }

        // create the normalized ValueFeature/Property
        val name = "normalizedConstraint$counter"
        counter++
        //val currentQuantity = Quantity(currentBDD)

        return SimpleProperty(name = name, expression = currentAst.toExpressionString(), dd = currentBDD, simpleAst = SimpleAstRoot(currentAst), valueSpecs = mutableListOf(XBool.X))
    }

    /**
     * Extract a map containing properties that need to be normalized out of a list of Properties/ValueFeatures.
     *
     * @param properties the list of properties from which the properties that need to be normalized will be extracted
     * @return map of Properties/ValueFeatures that need to be normalized with the variables (leaves) as keys
     */
    private fun propertiesToBeNormalized(properties : List<Variable>) : MutableMap<Collection<AstLeaf>,List<Variable>> {

        val propertiesToNormalize : MutableMap<Collection<AstLeaf>,List<Variable>> = mutableMapOf()
        val variablesAndProperties : MutableMap<Collection<AstLeaf>,MutableList<Variable>> = mutableMapOf()
        var leaves : Collection<AstLeaf>?
        var beforeFoundProperties : MutableList<Variable>

        // create a map with list of variables as keys and list of properties as value
        for(property in properties) {
            // extract the leaves, which represent the variables
            leaves = property.ast?.getLeaves()
            if(leaves != null) {
                // check if the list of variables was inserted before
                var leavesFound = false
                for(key in variablesAndProperties.keys) {
                    // need to have same size
                    if(leaves.size == key.size) {
                        var sameSet = true
                        // search each leaf in key
                        for(leaf in leaves) {
                            var leafFound = false
                            for(leafToCompare in key) {
                                // same name means same leaf
                                if(leaf.qualifiedName == leafToCompare.qualifiedName) {
                                    leafFound = true
                                    break
                                }
                            }
                            if(!leafFound) {
                                sameSet = false
                                break
                            }
                        }
                        if(sameSet) {
                            beforeFoundProperties = variablesAndProperties.getValue(key)
                            beforeFoundProperties.add(property)
                            variablesAndProperties.replace(key, beforeFoundProperties)
                            leavesFound = true
                            break
                        }
                    }
                }
                // list of variables was not inserted before
                if(!leavesFound) {
                    variablesAndProperties[leaves] = mutableListOf(property)
                }
            }
        }

        // only return entries with their list of variables having more than one property mapped
        for(entry in variablesAndProperties) {
            if(entry.key.isNotEmpty() && entry.value.size > 1) {
                propertiesToNormalize[entry.key] = entry.value
            }
        }

        return propertiesToNormalize
    }

    private fun negate(property: SimpleProperty<XBool>, model: Session): SimpleProperty<XBool> {

        val originalAST = property.simpleAst
        val negatedAST = AstNot(model, arrayListOf(originalAST!!.originalAst))
        negatedAST.initialize()
        val negatedExpression = "not(${property.expression})"


        return SimpleProperty(
            name = property.name,
            expression = negatedExpression,
            dd = negatedAST.bdd,
            simpleAst = SimpleAstRoot(negatedAST),
            valueSpecs = mutableListOf(model.builder.True)
        )
    }

}
