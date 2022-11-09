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

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.RedstoneWire
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class Simulation(
    val plugin: JavaPlugin,
    val pinLayout: PinLayout,
    val circuit: Circuit
) : Runnable/*, Listener*/ {
    private lateinit var task: BukkitTask
    private var update = true

    init {
        circuit.vertices.forEach { if (it.nameAndIndex != null && it.incomingEdges.isEmpty()) pinLayout.pinsByNameAndIndex[it.nameAndIndex]?.single()?.isBlockPowered?.let { value -> it.value = value } }
    }

    fun start() {
        task = plugin.server.scheduler.runTaskTimer(plugin, this@Simulation, 0, 1)
    }

    fun stop() {
        task.cancel()
    }

    /*@EventHandler*/
    fun on(event: BlockRedstoneEvent) {
        event.block.pin?.let {
            circuit[it]?.let {
                val value = event.newCurrent != 0
                if (it.value != value) {
                    it.value = value
                    update = true
                }
            }
        }
    }

    /*@EventHandler*/
    fun on(event: BlockBreakEvent) {
        val block = event.block
        if (block.isBlockIndirectlyPowered) event.block.pin?.let {
            circuit[it]?.let {
                if (it.value) {
                    it.value = false
                    update = true
                }
            }
        }
    }

    override fun run() {
        if (update) {
            update = false
            circuit.vertices.forEach {
                if (it.nameAndIndex != null && it.outgoingEdges.isEmpty()) {
                    val type = if (it.evaluate()) Material.REDSTONE_BLOCK else Material.WHITE_CONCRETE
                    pinLayout.pinsByNameAndIndex[it.nameAndIndex]?.forEach { it.type = type }
                }
            }
        }
    }

    private val Block.pin
        get() = when (val blockData = blockData) {
            is Directional -> pinLayout.pins[getRelative(blockData.facing.oppositeFace)]
            is RedstoneWire -> blockData.allowedFaces.firstNotNullOfOrNull { if (blockData.getFace(it) == RedstoneWire.Connection.SIDE) pinLayout.pins[getRelative(it)] else null }
            else -> null
        }
}
