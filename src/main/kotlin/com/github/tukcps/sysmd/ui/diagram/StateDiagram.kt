package com.github.tukcps.sysmd.ui.diagram

import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import org.diagramsascode.core.Diagram
import org.diagramsascode.core.DiagramEdge
import org.diagramsascode.core.DiagramNode
import org.diagramsascode.image.Image
import org.diagramsascode.image.StateDiagramImage
import org.diagramsascode.state.node.InitialState
import org.diagramsascode.state.node.StateDiagramNode


class StateDiagram(statemachine: StateUsage) {
    private val initialState : InitialState = InitialState()
    private val diagram: Diagram

    init{
        diagram = createDiagram(statemachine)
    }

    val image: Image?
        get() = diagram.let{StateDiagramImage.of(it)}

    fun nodes(): List<DiagramNode> {
        return diagram.nodes?.let { ArrayList(it) } ?: emptyList()
    }

    fun edges(): List<DiagramEdge> {
        return diagram.edges?.let { ArrayList(it) } ?: emptyList()
    }

    private fun createDiagram(statemachine: StateUsage): Diagram {
        val ownedActions = statemachine.getOwnedElementsOfType<ActionUsage>()
            .filter { it !is TransitionUsage }
        val nodes = ownedActions.map { diagramNodeFor(it) }

        val edges = statemachine.getOwnedElementsOfType<TransitionUsage>()
            .map { createTransition(it) }

        val diagram = Diagram.builder()
            .withNodes(nodes)
            .withEdges(edges)
            .build()
        return diagram
    }

    private fun createTransition(transition: TransitionUsage): org.diagramsascode.state.edge.Transition {
        val fromNode = diagramNodeFor(transition.source.ref!!)
        val toNode = diagramNodeFor(transition.target.ref!!)

        val trigger = transition.triggerPayloadParameterType?.name ?: ""
        val transitionText = trigger

        return org.diagramsascode.state.edge.Transition(fromNode, toNode, transitionText)
    }

    private fun diagramNodeFor(action: ActionUsage): StateDiagramNode {
        return if (action is StateUsage) {
            org.diagramsascode.state.node.State(idOf(action),nameOf(action))
        } else {
            initialState
        }
    }

    private fun idOf(action: ActionUsage): String {
        return action.qualifiedName.replace("::", "__")
    }

    private fun nameOf(action: ActionUsage): String {
        return action.escapedName().orEmpty()
    }
}
