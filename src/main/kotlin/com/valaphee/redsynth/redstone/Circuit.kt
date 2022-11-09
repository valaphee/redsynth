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

class Circuit(
    module: Netlist.Module
) {
    val vertices: List<Vertex>

    init {
        val vertices = mutableMapOf<Int, Vertex>()
        module.ports.forEach { (name, port) -> port.bits.forEachIndexed { index, bit -> vertices[bit as Int] = Vertex(name to index) } }
        module.cells.forEach { (name, cell) ->
            when (cell.type) {
                "\$_NOT_" -> {
                    val edgeA = vertices.getOrPut(cell.connections["A"]!!.single() as Int) { Vertex() }
                    val edgeY = vertices.getOrPut(cell.connections["Y"]!!.single() as Int) { Vertex(negation = true) }
                    edgeY.incomingEdges += edgeA
                    edgeA.outgoingEdges += edgeY
                }
                "\$_OR_" -> {
                    val edgeA = vertices.getOrPut(cell.connections["A"]!!.single() as Int) { Vertex() }
                    val edgeB = vertices.getOrPut(cell.connections["B"]!!.single() as Int) { Vertex() }
                    val edgeY = vertices.getOrPut(cell.connections["Y"]!!.single() as Int) { Vertex() }
                    edgeY.incomingEdges += listOf(edgeA, edgeB)
                    edgeA.outgoingEdges += edgeY
                    edgeB.outgoingEdges += edgeY
                }
                else -> error(name)
            }
        }
        this.vertices = vertices.values.toList()
    }

    operator fun get(nameAndIndex: Pair<String, Int>) = vertices.find { it.nameAndIndex == nameAndIndex }

    override fun toString() = StringBuilder().apply { vertices.filter { it.nameAndIndex != null }.forEach { appendLine(it.toString(it.outgoingEdges.isEmpty())) } }.toString()

    class Vertex(
        val nameAndIndex: Pair<String, Int>? = null,
        val negation: Boolean = false,
        val incomingEdges: MutableList<Vertex> = mutableListOf(),
        val outgoingEdges: MutableList<Vertex> = mutableListOf()
    ) {
        var value: Boolean = false

        fun updateAndEvaluate(value: Boolean) {
            this.value = if (negation) !value else value
            outgoingEdges.forEach(Vertex::updateAndEvaluate)
        }

        private fun updateAndEvaluate() {
            value = if (negation) incomingEdges.none(Vertex::value) else incomingEdges.any(Vertex::value)
            outgoingEdges.forEach(Vertex::updateAndEvaluate)
        }

        fun evaluate(): Boolean = if (negation) if (incomingEdges.size == 0) !value else incomingEdges.none(Vertex::evaluate) else if (incomingEdges.size == 0) value else incomingEdges.any(Vertex::evaluate)

        private fun toString(direction: Boolean, prefix: StringBuilder, right: Boolean, result: StringBuilder): StringBuilder {
            val edges = if (direction) incomingEdges else outgoingEdges
            edges.getOrNull(0)?.toString(direction, StringBuilder().append(prefix).append(if (right) "│ " else "  "), false, result)
            result.append(prefix).append(if (right) "└─" else "┌─").append(nameAndIndex?.let { "${it.first}:${it.second}" } ?: if (negation) "╡" else "┤").append("\n")
            edges.getOrNull(1)?.toString(direction, StringBuilder().append(prefix).append(if (right) "  " else "│ "), true, result)
            return result
        }

        fun toString(direction: Boolean) = this.toString(direction, StringBuilder(), true, StringBuilder()).toString()
    }
}
