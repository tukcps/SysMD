package com.github.tukcps.sysmd.model.expression


import io.github.tukcps.aadd.*
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import java.util.UUID

/**
 * The class AstNode implements an interface for an attributed syntax tree (AST).
 * The base class of the expression tree is a simple value,
 * represented by result: a constant literal or a variable.
 * For variables, there is an id (name); for literals the result is its value.
 *
 * @param uuid A unique identifier for this node
 */
@Suppress("UNCHECKED_CAST")
abstract class AstNode(val model: Session, val uuid : UUID = UUID.randomUUID()!!) : Cloneable {
    init {
        model.astNodes[uuid] = this
    }

    lateinit var upQuantity: VectorQuantity
    lateinit var downQuantity: VectorQuantity

    fun upQuantityInitialized() = this::upQuantity.isInitialized
    fun downQuantityInitialized() = this::downQuantity.isInitialized

    /** Fields in all Ast subclasses */
    internal open var root: AstNode? = null // Reference to next-higher level of AST
    internal var parent: AstNode? = null    // Reference to the parent node or null, if root.

    val aadd: AADD                          // Returns value as AADD or throws error
        get() = upQuantity.values[0] as AADD
    val bdd: BDD                            // Returns value as BDD or returns error
        get() = upQuantity.values[0]as BDD
    val idd: IDD
        get() = upQuantity.values[0] as IDD
    val dd: DD<*>
        get() = upQuantity.values[0]

    val aadds: List<AADD>          // Returns value as AADD or throws error
        get() = upQuantity.values as List<AADD>
    val bdds: List<BDD>            // Returns value as BDD or returns error
        get() = upQuantity.values as List<BDD>
    val idds: List<IDD>
        get() = upQuantity.values as List<IDD>
    val dds: List<DD<*>>
        get() = upQuantity.values

    val isBool: Boolean
        get() = upQuantity.values[0] is BDD

    val isReal: Boolean
        get() = upQuantity.values[0] is AADD

    val isInt: Boolean
        get() = upQuantity.values[0] is IDD

    val isString: Boolean
        get() = upQuantity.values[0] is StrDD

    /**
     * The constructor only initializes the AST tree structures.
     * It does not search for properties etc. in the symbol table as these might not be declared.
     * This will be done after the complete model is read by initialize().
     * Initialize must take care of the late init variables upQuantity and downQuantity and
     * assign them a non-null variable of type DD<*>.
     */
    abstract fun initialize()

    abstract fun evalUpRec()    // Evaluates recursively to dependent properties
    abstract fun evalUp()
    abstract fun evalDownRec()  // Evaluates recursively to parameters
    abstract fun evalDown()

    abstract fun <R> runDepthFirst(block: AstNode.() -> R): R
    abstract fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R

    /** casts this to AADD */
    fun asAADD(aadd: DD<*>?): AADD =
        if (aadd == null) model.builder.Reals
        else aadd as AADD

    /** casts this to IDD */
    fun asIDD(idd: DD<*>?): IDD =
        if (idd == null) model.builder.Integers
        else idd as IDD

    /** get a collection of all leaf nodes */
    fun getLeaves(): Collection<AstLeaf> =
        this.runDepthFirst {
            when (this) {
                is AstLeaf -> arrayListOf(this)
                is AstBinOp -> l.getLeaves() + r.getLeaves()
                is AstUnaryOp -> operand.getLeaves()
                is AstFunction -> {
                    val r = arrayListOf<AstLeaf>()
                    for (p in parameters) r += p.getLeaves()
                    return@runDepthFirst r
                }
                is AstRoot -> dependency.getLeaves()
                else -> throw Exception("AstNode of unknown type.")
            }
        }

    /** Recursive collection of all dependencies */
    fun getDependencies(): Set<Variable> =
        this.runDepthFirst {
            when (this) {
                is AstLeaf -> if (variable != null) setOf(variable!!) else emptySet()
                is AstBinOp -> l.getDependencies() + r.getDependencies()
                is AstUnaryOp -> operand.getDependencies()
                is AstBySpecializations -> emptySet()
                is AstByImplements -> emptySet()
                is AstFunction -> {
                    val r = mutableSetOf<Variable>()
                    for (p in parameters) r += (p.getDependencies())
                    return@runDepthFirst r
                }
                is AstRoot -> dependency.getDependencies()
                else -> throw Exception("AstNode of unknown type.")
            }
        }

    fun getDependencyStrings(): Set<String> =
        this.runDepthFirst {
            when (this) {
                is AstLeaf -> {
                    if (qualifiedName != null) {
                        if (this.parent is AstUserDefinedFunction)
                            setOf("${namespace!!.owner.ref!!.qualifiedName}::${qualifiedName}")
                        else
                            setOf(qualifiedName as String)

                    } else emptySet()
                }
                is AstBinOp -> l.getDependencyStrings() + r.getDependencyStrings()
                is AstUnaryOp -> operand.getDependencyStrings()
                is AstBySpecializations -> emptySet()
                is AstByImplements -> emptySet()
                is AstFunction -> {
                    when(this){
                        is AstAggregationFunction -> {
                            return@runDepthFirst getDependentPropertyStrings()
                        }
                        else-> {
                            val r = mutableSetOf<String>()

                            for (p in parameters) {
                                r += (p.getDependencyStrings())
                            }
                            return@runDepthFirst r
                        }
                    }
                }
                is AstRoot -> dependency.getDependencyStrings()
                else -> throw Exception("AstNode of unknown type.")
            }
        }




    abstract fun toExpressionString(): String
    public override fun clone(): AstNode = super.clone() as AstNode
    override fun toString(): String = "AstNode($upQuantity, $downQuantity)"
}
