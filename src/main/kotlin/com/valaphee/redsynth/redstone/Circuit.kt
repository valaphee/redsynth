/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.redsynth.redstone

import com.valaphee.redsynth.logic.Netlist
import org.optaplanner.core.api.domain.solution.PlanningSolution

@PlanningSolution
class Circuit(
    module: Netlist.Module
) {
    val vertices: List<Vertex>

    init {
        val vertices = mutableMapOf<Int, Vertex>()
        module.cells.values.forEach {
            when (it.type) {
                "\$_NOT_" -> {
                    val incomingEdge = vertices.getOrPut(it.connections["A"]!!.single() as Int) { ConnectionVertex() } as ConnectionVertex
                    vertices[it.connections["A"]!!.single() as Int] = NegationVertex(incomingEdge).also { incomingEdge.edges += it }
                }
                "\$_OR_" -> {
                    val edgeA = vertices.getOrPut(it.connections["A"]!!.single() as Int) { ConnectionVertex() }
                    val edgeB = vertices.getOrPut(it.connections["B"]!!.single() as Int) { ConnectionVertex() }
                    val edgeY = vertices.getOrPut(it.connections["Y"]!!.single() as Int) { ConnectionVertex() }
                    vertices[it.connections["A"]!!.single() as Int] = ConnectionVertex(mutableListOf(edgeA, edgeB, edgeY)).also {
                        edgeA.edges += it
                        edgeB.edges += it
                    }
                }
                else -> error(it)
            }
        }
        this.vertices = vertices.values.toList()
    }

    sealed class Vertex(
        val edges: MutableList<Vertex> = mutableListOf()
    )

    class ConnectionVertex(
        edges: MutableList<Vertex> = mutableListOf()
    ) : Vertex(edges)

    class NegationVertex(
        val incomingEdge: ConnectionVertex,
        edges: MutableList<ConnectionVertex> = mutableListOf()
    ) : Vertex(edges.toMutableList())
}
