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
    netlist: Netlist
) {
    private val connections: Map<String, Connection>

    init {
        val module = netlist.modules.values.first()
        val connections = mutableMapOf<Int, Connection>()
        module.ports.forEach { (name, port) -> port.bits.forEach { connection -> connections[connection as Int] = Connection(name) } }
        module.cells.values.forEach {
            when (it.type) {
                "\$_OR_" -> OrComponent(listOf(connections.getOrPut(it.connections["A"]!!.single() as Int) { Connection() }, connections.getOrPut(it.connections["B"]!!.single() as Int) { Connection() }), listOf(connections.getOrPut(it.connections["Y"]!!.single() as Int) { Connection() }))
                "\$_AND_" -> AndComponent(listOf(connections.getOrPut(it.connections["A"]!!.single() as Int) { Connection() }, connections.getOrPut(it.connections["B"]!!.single() as Int) { Connection() }), listOf(connections.getOrPut(it.connections["Y"]!!.single() as Int) { Connection() }))
                "\$_XOR_" -> XorComponent(connections.getOrPut(it.connections["A"]!!.single() as Int) { Connection() }, connections.getOrPut(it.connections["B"]!!.single() as Int) { Connection() }, listOf(connections.getOrPut(it.connections["Y"]!!.single() as Int) { Connection() }))
            }
        }
        this.connections = connections.values.mapNotNull { it.name?.let { name -> name to it } }.toMap()
    }

    private class Connection(
        val name: String? = null
    ) {
        val inputs = mutableListOf<Component>()
        val outputs = mutableListOf<Component>()
    }

    private sealed interface Component

    private class NotComponent(
        val input: Connection,
        val outputs: List<Connection>
    ) : Component {
        init {
            input.outputs += this
            outputs.forEach { it.inputs += this }
        }
    }

    private class OrComponent(
        val inputs: List<Connection>,
        val outputs: List<Connection>
    ) : Component {
        init {
            inputs.forEach { it.outputs += this }
            outputs.forEach { it.inputs += this }
        }
    }

    companion object {
        private fun List<Connection>.invert() = map { Connection().apply { NotComponent(it, listOf(this)) } }

        @Suppress("FunctionName")
        private fun NorComponent(inputs: List<Connection>, outputs: List<Connection>) {
            NotComponent(Connection().apply { OrComponent(inputs.invert(), listOf(this)) }, outputs)
        }

        @Suppress("FunctionName")
        private fun AndComponent(inputs: List<Connection>, outputs: List<Connection>) {
            NorComponent(inputs.invert(), outputs)
        }

        @Suppress("FunctionName")
        private fun XorComponent(inputA: Connection, inputB: Connection, outputs: List<Connection>) {
            val tmp1 = Connection()
            val tmp2 = Connection()
            AndComponent(listOf(inputA, inputB), listOf(tmp1))
            NorComponent(listOf(inputA, inputB), listOf(tmp2))
            OrComponent(listOf(tmp1, tmp2), outputs)
        }
    }
}
