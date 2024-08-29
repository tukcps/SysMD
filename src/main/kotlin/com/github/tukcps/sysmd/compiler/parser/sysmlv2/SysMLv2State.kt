package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Body
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.Identification

fun KerML.State() {
    val stateUsage = sysMLSemantics.StateUsageSemantics()
    STATE.consume()
    Identification().also {
        stateUsage.identification = it
    }
    stateUsage.create()
    Body(Resolved(stateUsage.created!!))
}

fun KerML.StateDef() {
    var identification: Identification?
    STATE.consume()
    DEF.consume()
    Identification().also { identification=it }
    Body(Resolved(identification?.name?:identification?.shortName!!))
}

fun KerML.Action() {
    if (token.kind == ENTRY) {
        consume()
        val actionUsage = sysMLSemantics.ActionUsageSemantics()
        ACTION.consume()
        Identification().also {
            actionUsage.identification = it
        }
        actionUsage.create()
        val identification = actionUsage.identification
        Body(Resolved(identification?.name ?: identification?.shortName!!))
    }
}


fun KerML.Transition() {
    TRANSITION.consume()
    val transitionUsage = sysMLSemantics.TransitionUsageSemantics()

    // We need to push the owner since we're creating owned features, but without the usual curly braces of the body
    val transId = transitionUsage.identification
    semantics.pushOwner(Resolved(transId?.name ?: transId?.shortName!!))

    // Process the source of the connection
    FIRST.consume()
    val succession = sysMLSemantics.SuccessionAsUsageSemantics()
    QualifiedName().also { succession.source += it }

    optional(start=ACCEPT, consume = true) {
        // We need to push the owner since we're creating owned features, but without the usual curly braces of the body
        val acceptActionUsage = sysMLSemantics.AcceptActionUsageSemantics()
        val acceptId = acceptActionUsage.identification
        semantics.pushOwner(Resolved(acceptId?.name ?: acceptId?.shortName!!))

        val triggerPayloadParameter =
            sysMLSemantics.PayloadParameterSemantics(typeName = QualifiedName())
        triggerPayloadParameter.create()

        triggerPayloadParameter.created.also { acceptActionUsage.payloadParameter = it }
        acceptActionUsage.create()

        // We're done with creating owned features, so we need to pop the owner (i.e. the transition) again
        semantics.popOwner()
    }

    // Process the target of the connection & create the connection
    THEN.consume()
    QualifiedName().also { succession.target += it }
    succession.create()

    // We're done with creating owned features, so we need to pop the owner (i.e. the transition) again
    SEMICOLON.consume()
    semantics.popOwner()

    // Finally, we can create the transition
    transitionUsage.create()
}
