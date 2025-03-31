package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.XBool
import java.util.*

/**
 *  Handles discrete propagation. Currently only through one propagate method. Eventually will split in initialization and update.
 */

/**
 * TODO's: - ConflictTracer (own class?)
 *         - Human readable reasoning (string-msg?)
 */

class DDBasedDiscreteSolver(
    val model: Session
) : DiscreteSolverIF {
    val builder: DDBuilder
        get() = model.builder

    /** A boolean expression that may or may not be true.
     * @param about The set of Element IDs referenced in `body`
     * @param body A BDD expressing this statement
     */
    class Statement(val about : Set<Int>, val body : BDD) {
        fun merge(other : Statement) = Statement( about.plus(other.about), body.and(other.body) )

        /** Merge two variable states for finding profiles. */
        private fun merge(l : BDD.Leaf?, r : BDD.Leaf) : BDD.Leaf = when {
            l === null -> r
            l === body.builder.Bool || r == body.builder.Bool -> body.builder.Bool
            l === r -> l
            l === body.builder.True && r === body.builder.False -> body.builder.Bool
            l === body.builder.False && r === body.builder.True -> body.builder.Bool
            else -> throw IllegalStateException("Arguments to merge() must be one of Bool, True or False, got $l, $r")
        }

        /** Inspects every true leaf of `b`, and updates `spec` with a profile of every satisfying assignment
         * @return Whether `b` is satisfiable
         * */
        private fun profile(spec : MutableMap<Int, BDD.Leaf>, b : BDD) : Boolean
            = if(b is BDD.Internal) {
                val t = profile(spec, b.T)
                val f = profile(spec, b.F)

                if(t && f)
                    spec[b.index] = body.builder.Bool
                else if(t)
                    spec.compute(b.index) { _, x -> merge(x, body.builder.True) }
                else if(f)
                    spec.compute(b.index) { _, x -> merge(x, body.builder.False) }

                t || f
            } else
                b === b.builder.True || b === b.builder.Bool

        private fun simplify(spec : Map<Int, BDD.Leaf>, b : BDD) : BDD
            = if(b is BDD.Internal) {
                when(spec[b.index]) {
                    null -> b.builder.False
                    b.builder.True -> simplify(spec, b.T)
                    b.builder.False -> simplify(spec, b.F)
                    else -> {
                        val t = simplify(spec, b.T)
                        val f = simplify(spec, b.F)

                        if(t === f) t else BDD.Internal(b.builder, b.index, t, f)
                    }
                }
            } else b

        /** Simplifies the theorem based on the possible satisfying assignments.
         * Effectively performs an evaluate()
         * @param spec The map built by profile()
         */
        private fun simplify(spec : Map<Int, BDD.Leaf>) : Statement {
            // set of variables that can be removed
            val rm = about.filter {
                when(spec[it]) {
                    null -> true
                    body.builder.True -> true
                    body.builder.False -> true
                    else -> false
                }
            }

            return if(rm.isNotEmpty())
                Statement(about.minus(rm.toSet()), simplify(spec, body))
            else
                this
        }

        /** Determines the possible set of satisfying assignments.
         * Then applies that set to simplify the theorem
         * @param spec Populated with True, False, Bool or Infeasible
         * @returns The simplified theorem, or null if UNSAT
         */
        fun profileAndSimplify(spec : MutableMap<Int, BDD.Leaf>) : Statement? {
            if(! profile(spec, body))
            {
                // ensures that theorems are disjunct
                assert(spec.keys.intersect(about).isEmpty())

                for(a in about)
                    spec[a] = body.builder.InfeasibleB

                return null
            }

            val simpler = simplify(spec)

            // fill in unused variables
            for(a in about)
                spec.computeIfAbsent(a) { body.builder.Bool }

            return simpler
        }
    }

    private fun openVars(bdd : BDD, ids : MutableSet<Int>)
    {
        if(bdd is BDD.Internal) {
            ids.add(bdd.index)
            openVars(bdd.T, ids)
            openVars(bdd.F, ids)
        }
    }

    private fun openVars(bdd : BDD) : Set<Int>
    {
        val acc = mutableSetOf<Int>()

        openVars(bdd, acc)

        return acc
    }

    fun statement(body : BDD) : Statement = Statement(openVars(body), body)

    /** Forest of BDDs known to be satisfiable */
    private val theorems : MutableList<Statement> = mutableListOf()

    /** Whether an initialize() overload has been called yet */
    private var initialzed : Boolean = false

    /** Set of expressions to check for updates */
    private val updatedProperties : MutableSet<Feature> = mutableSetOf()

    override fun isInitialized() : Boolean = initialzed

    fun initialize(model: Session, ignored: Int)
        = initialize(model)

    override fun initialize(model: Session) {
        initialzed = true
        model.get().filterIsInstance<Variable>().forEach{ update(it) }
    }

    override fun update(scheduledProperties: List<Variable>) {
        updatedProperties.addAll(scheduledProperties.filter { it.updated && (it.feature !is Multiplicity && !(it.baseType === Variable.BaseType.String && it.feature.name?.endsWith("range") == true) && it.baseType === Variable.BaseType.Bool) }.map { it.feature })
    }

    override fun update(updatedProperty: Variable) {
        // skip all multiplicity vars
        if(updatedProperty.feature is Multiplicity)
            return
        // why are these fed into the discrete solver??
        if(updatedProperty.baseType === Variable.BaseType.String && updatedProperty.feature.name?.endsWith("range") == true)
            return

        // Only handle boolean variables
        if(updatedProperty.baseType !== Variable.BaseType.Bool)
            return//TODO("Only bools are supported currently")

        updatedProperties.add(updatedProperty.feature)
    }

    /** Learns new theorems from any updated variables */
    private fun collectUpdated() : Set<Statement> {
        val newTheorems = mutableSetOf<Statement>()

        for (p in updatedProperties) {
            if(p is Multiplicity)
                continue

            val v = p.variable!!

            if(v.valueSpecs.size != 1)
                continue //TODO()

            val q = v.vectorQuantity.bdd()
            val s = v.boolSpecs[0]
            val id = builder.conds.indexes[p.elementId.toString()]

            when {
                s === XBool.True -> {
                    newTheorems.add(statement(q))

                    if(id !== null)
                        newTheorems.add(Statement( setOf(id), BDD.Internal(builder, id, builder.True, builder.False) ))
                }
                s === XBool.False -> {
                    newTheorems.add(statement(q.not()))

                    if(id !== null)
                        newTheorems.add(Statement( setOf(id), BDD.Internal(builder, id, builder.False, builder.True) ))
                }
                s === XBool.NaB -> continue//TODO()
                id !== null -> {
                    val ov = openVars(q)

                    if(ov.contains(id))
                    {
                        require(q is BDD.Internal) { "A self-referencing definition must be an inner node" }

                        when(Pair(q.T, q.F)) {
                            Pair(q.builder.True, q.builder.False) -> {} // simple tautology
                            Pair(q.builder.False, q.builder.True) -> { // simple contradiction
                                // Should it be entered into the theorem list to "corrupt" other theorems after merge or be handled right now?
                                newTheorems.add( Statement(ov, builder.False) ) // not in simplest form, may cause problems
                            }
                            else -> continue//TODO("A self-referencing definition should be a simple tautology or contradiction")
                        }
                    }
                    else
                        newTheorems.add( Statement(
                            ov.plus(id),
                            BDD.Internal(builder, id, builder.True, builder.False).ite(q, q.not())
                        ) )
                }
                else -> continue//TODO()
            }
        }

        return newTheorems
    }

    /** Ensures all units in the builder.conds are applied to theorems */
    private fun evaluateTheorems() {
        val fixed = builder.conds.x.entries
            .filter { it.value === builder.True || it.value === builder.False }
            .map { it.key }
            .toSet()

        for(i in 0 until theorems.size) {
            val th = theorems[i]
            if (fixed.any { th.about.contains(it) }) {
                theorems[i] = Statement(
                    th.about.minus(fixed),
                    th.body.evaluate()
                )
            }
        }
    }

    /** Scans theorems and merges any that share variables */
    private fun simplifyTheorems() {
        for(i in theorems.size-2 downTo 0) {
            for(j in theorems.size-1 downTo i+1) {
                if(theorems[i].about.any { theorems[j].about.contains(it) }) {
                    val m = theorems[i].merge(theorems[j])
                    theorems[i] = m
                    theorems.removeAt(j)
                }
            }
        }

        // delete any tautologies
        theorems.removeAll{ it.body is BDD.Leaf && (it.body.value === XBool.True || it.body.value === XBool.X) }
    }

    /**
     * Called after `update()` overloads register variable updates.
     * External state is only updated by `assertConstraints()`.
     */
    override fun advanceState() {
        evaluateTheorems()

        if(theorems.addAll(collectUpdated()))
            simplifyTheorems()

        updatedProperties.clear()
    }

    /** Deduces new units and ensures that they comply with the known theorems.
     * Then writes them back to builder.conds, valueSpecs and vectorQuantities
     */
    override fun assertConstraints() {
        val spec = mutableMapOf<Int, BDD.Leaf>()

        // build specs, check theorem satisfiability & propagate found units
        for (i in theorems.size - 1 downTo 0) {
            val th = theorems[i].profileAndSimplify(spec)

            if(th === null)
                theorems.removeAt(i)
            else
                theorems[i] = th
        }

        for (s in spec.entries) {
            if(s.value === builder.True || s.value === builder.False || s.value === builder.InfeasibleB)
                builder.conds.x[s.key] = s.value;

            builder.conds.indexes
                .filter { it.value == s.key }
                .map {
                    try {
                        UUID.fromString(it.key)
                    } catch(e : IllegalArgumentException) {
                        null
                    }
                }.filterNotNull()
                .forEach {
                    val f = model[it]

                    if(f is Feature) {
                        val v = f.variable

                        if(v !== null) {
                            v.valueSpecs = mutableListOf(s.value)
                            v.vectorQuantity.values = mutableListOf(s.value)
                        }
                        else
                            TODO("Builder.conds feature has no variable attached")
                    }
                    else
                        TODO("Builder.conds ID does not point to feature")
                }
        }
    }
}
