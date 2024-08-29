package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Connector
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.*
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

/**
 * The function basically works the same way as bySubclasses, but instead of subclass relationship it uses the
 * implements sources of the relationship Component --> implements (Connector) --> Function.
 */
class AstByImplements(model: Session, namespace: Namespace, args: ArrayList<AstNode>) :
    AstFunction("byImplements", model, 1, args) {

    private val inNameSpace: Namespace = namespace
    private var propertyName: QualifiedName? = null
    private var feature: Feature? = null
    private var implementsAssociation: Association? = null
    private var implements: Connector? = null
    var component: Namespace? = null    // source
    var function: Namespace? = null     // target

    override fun initialize() {
        propertyName = (getParam(0) as AstLeaf).qualifiedName!!

        feature = inNameSpace.resolve<Feature>(propertyName!!)
        if (feature == null) model.report(this.feature, "Could not find property '$propertyName'")

        implementsAssociation = model.global.resolve<Association>("ISO26262::implements")
        if (implementsAssociation == null)
            model.report(this.feature, "Could not find Association 'implements' in model.")

        implements = model.getRelationshipsTo(feature?.owner?.ref!!, "*", implementsAssociation).firstOrNull() as Connector?
        if (implements == null)
            model.report(this.feature, "could not find suitable connector typed by 'implements'")

        component = implements!!.source.firstOrNull()?.ref as Namespace
        function = implements!!.target.firstOrNull()?.ref as Namespace

        upQuantity = when (super.getParam(0).upQuantity.values[0]) {
            is BDD -> Quantity(model.builder.Bool)
            is IDD -> Quantity(model.builder.Integers)
            is AADD -> Quantity(model.builder.Reals, "?")
            else -> throw Exception("Unknown data type!")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }


    /**
     * Searches in all sources for the respective property and sets it in this element to the ITE-combination of
     * all found properties in subclasses.
     */
    override fun evalUp() {
        // TODO: Currently simple 1:1 relationship, extend to n:m
        val c = (component as Namespace).resolveVar(propertyName!!)!!
        this.upQuantity = c.vectorQuantity
    }

    /**
     * Does a depth-first traversal and applies the lambda parameter block.
     * @param receiver the ast node that is visited
     * @param block the lambda that is applied depth-first
     */
    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R {
        return block()
    }

    /**
     * Does nothing so far.
     */
    override fun evalDown() {}

    /**
     * Does nothing so far.
     */
    override fun evalDownRec() {}

    override fun toExpressionString() = "byImplements($propertyName)"
}
