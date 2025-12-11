package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.InternalError
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD

/**
 *  A user defined function call.
 *  @param model reference to the model in which the call is made
 *  @param namespace reference to the namespace in which features will be resolved
 *  @param args the arguments as list of AstNode
 *  @param functionName the name of the called function
 */
internal class AstUserDefinedFunction(
    model: Session,
    private val namespace: Namespace,
    args: ArrayList<AstNode>,
    functionName: String
) : AstFunction(functionName, model, 0, args) {
    private lateinit var function: Function
    private lateinit var functionInputs: MutableMap<String, VectorQuantity>
    private lateinit var functionCalculations: List<Feature>
    private lateinit var inputParamPositions: MutableMap<String, Int>
    private lateinit var astToInputConnection: MutableMap<String, AstLeaf>
    private lateinit var functionAST: AstNode

    /**
     * Initialization; starts from bottom-up
     */
    override fun initialize() {

        // BUG? shouldn't the return feature define the return type?
        upQuantity = when (getParam(0).upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "?")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            is BDD -> VectorQuantity(mutableListOf(model.builder.Bool))
            is StrDD -> VectorQuantity(mutableListOf(model.builder.Strings))
            else -> throw SemanticError("UserDefinedFunction must have Real, Boolean or Integer result")
        }
        // Get Function with given name
        val resultResolvingFunctionName = namespace.resolve(name)?.memberElement as Function?
        if (resultResolvingFunctionName != null)
            function = resultResolvingFunctionName
        else
            throw SemanticError("identifier $name not a known function or calculation")
        // Insert the input values into the function
        functionInputs = mutableMapOf()
        astToInputConnection = mutableMapOf()
        inputParamPositions = mutableMapOf()
        val expectedFunctionInputs =
            function.getOwnedElementsOfType<Feature>().filter { it.direction == Feature.FeatureDirectionKind.IN }
        if (expectedFunctionInputs.size != parameters.size)
            throw SemanticError("Function $name expects ${expectedFunctionInputs.size} parameters, but ${parameters.size} parameters given", this.function)
        parameters.indices.forEach {
            // Test if units are matching
            // create Quantity, to make Unit of expectedFunctionInputs canonical.
            // After that it can be compared with the unit of the parameter
            //if(expectedFunctionInputs[it].subUnitConstraint!=null)
            val expectedUnit = Quantity(model.builder.real(1.0), Unit(expectedFunctionInputs[it].variable?.vectorQuantity?.unit.toString())).unit
            if (expectedUnit != parameters[it].upQuantity.unit)
                throw SemanticError("Unit error for function $name: for the ${it + 1}. parameter the unit ${expectedFunctionInputs[it].unitConstraint} was expected, but the unit is ${parameters[it].upQuantity.unit}")
            functionInputs[expectedFunctionInputs[it].escapedName()!!] = parameters[it].upQuantity
            inputParamPositions[expectedFunctionInputs[it].escapedName()!!] = it
        }
        // Get expression for the calculation of the result
        val resultExpressions =
            function.getOwnedElementsOfType<Feature>().filter { it.direction == Feature.FeatureDirectionKind.OUT }
        if (resultExpressions.size != 1)
            throw SemanticError("Exactly one return parameter for function $name expected, but there are ${resultExpressions.size}")
        val resultExpression = resultExpressions[0]
        //if there is more than one calculationStep, these are also needed
        functionCalculations = function.getOwnedElementsOfType<Feature>()
            .filter { it.direction == Feature.FeatureDirectionKind.INOUT }
        functionCalculations.forEach {
            it.variable!!.compileExpression()
        }

        if (resultExpression.variable?.ast == null)
            resultExpression.variable?.compileExpression()

        //Build the AST using all the calculation steps of the input
        functionAST = resultExpression.variable?.ast?.let { buildAst(it) }
            ?: throw InternalError("No AST for result expression found.")
        functionAST.runDepthFirst { initialize() } //initialize Real fkt in AST
        upQuantity = functionAST.upQuantity.clone()
        downQuantity = upQuantity.clone()
    }

    /**
     * Generates an AST for the function
     */
    private fun buildAst(node: AstNode): AstNode {
        when (node) {
            is AstLeaf -> {
                return if (node.literalVal != null) //for values return node
                    node
                else if (node.qualifiedName in functionInputs) {// is an input parameter
                    val position = functionInputs.keys.indexOf(node.qualifiedName)
                    val parameter = parameters[position]
                    val leaf = AstLeaf(model, parameter)
                    // val leaf = AstLeaf(parameter.)
                    astToInputConnection[node.qualifiedName!!] = leaf
                    return leaf
                } else if (node.qualifiedName in functionCalculations.map { it.escapedName() }) { //is used in another expression
                    functionCalculations.find { it.escapedName() == node.qualifiedName }?.variable?.ast?.let { buildAst(it) } ?:
                        throw InternalError("(Internal) AST is missing.")
                } else {
                    throw SemanticError("${node.qualifiedName} is not defined in the function $name.")
                }
            }

            is AstBinOp -> {
                return AstBinOp(buildAst(node.l), node.op, buildAst(node.r))
            }

            is AstUnaryOp -> {
                return AstUnaryOp(node.op, buildAst(node.operand))
            }

            is AstFunction -> {
                if (node is AstAggregationFunction)
                    throw SemanticError("User defined functions with aggregation functions are not allowed")
                //Build an AST for all parameters
                val parameters = ArrayList<AstNode>()
                node.parameters.forEach { parameters.add(buildAst(it)) }
                when (node.name) {
                    "ITE" -> return AstIte(model, parameters)
                    "allOf" -> return AstAllOf(model, parameters)
                    "anyOf" -> return AstAnyOf(model, parameters)
                    "sum_i" -> return AstSumI(namespace, model, parameters)
                    "sum" -> return AstSum(namespace, model, parameters)
                    "characterizedResult" -> return AstCharacterizedResult(model, namespace, parameters)
                    "ln" -> return AstLn(model, parameters)
                    "exp" -> return AstExp(model, parameters)
                    "sqr" -> return AstSqr(model, parameters)
                    "sqrt" -> return AstSqrt(model, parameters)
                    "ceil" -> return AstCeil(model, parameters)
                    "floor" -> return AstFloor(model, parameters)
                    "power2" -> return AstPower2(model, parameters)
                    "pow2" -> return AstPower2(model, parameters)
                    "powerb" -> return AstPower(model, parameters)
                    "power" -> return AstPower(model, parameters)
                    "powb" -> return AstPower(model, parameters)
                    "pow" -> return AstPower(model, parameters)
                    "sin" -> return AstSin(model,parameters)
                    "cos" -> return AstCos(model,parameters)
                    "toReal" -> return AstToReal(model, parameters)
                    "DateTime" -> return AstDateTime(model, parameters)
                    "Date" -> return AstDate(model, parameters)
                    "Month" -> return AstMonth(model, parameters)
                    "Year" -> return AstYear(model, parameters)
                    "max" -> return AstMax(model, parameters)
                    "min" -> return AstMin(model, parameters)
                    "abs" -> return AstAbs(model, parameters)
                    "intersect" -> return AstIntersect(model, parameters)
                    "bySpecializations" -> return AstBySpecializations(model, namespace, parameters)
                    "byParts" -> return AstByParts(model, namespace, parameters)
                    "byImplements" -> return AstByImplements(model, namespace, parameters)
                    "linear" -> return AstLinearInterpolation(model, parameters)
                    "stepInterpolation" -> return AstStepInterpolation(model,parameters)
                    "ToReal" -> return AstReal(model, parameters)
                    "ToInteger" -> return AstInteger(model, parameters)
                    "norm" -> return AstNormalizeVector(model,parameters)
                    "size" -> return AstVectorSize(model, parameters)
                    "angle" -> return AstVectorAngle(model,parameters)
                    "cityBlockDistance" -> return AstCityBlockDistance(model,parameters)
                    "quantityOfVectorAtPosition" -> return AstQuantityOfVectorAtPosition(model,parameters)
                    else -> return AstUserDefinedFunction(model, namespace, parameters, node.name)
                }
            }

            is AstRoot -> {
                return buildAst(node.dependency)
            }

            else -> throw SemanticError("NodeType for user defined function not implemented")
        }
    }

    override fun evalUp() {
        functionAST.evalUpRec()
        upQuantity = functionAST.upQuantity.clone()
    }

    override fun evalDown() {
        //eval Down result
        functionAST.downQuantity = downQuantity.clone()
        functionAST.upQuantity = downQuantity.clone()
        functionAST.evalDownRec()
        //write back the results of the downwards evaluation to the parameters
        for (input in astToInputConnection.keys)
            getParam(inputParamPositions[input]!!).downQuantity = astToInputConnection[input]!!.downQuantity.clone()
    }

    override fun clone(): AstUserDefinedFunction {
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p.clone())
        return AstUserDefinedFunction(model, namespace, parClone, name)
    }
}
