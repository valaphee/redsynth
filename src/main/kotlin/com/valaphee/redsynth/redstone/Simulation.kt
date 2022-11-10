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
import org.bukkit.block.BlockFace
import org.bukkit.block.data.AnaloguePowerable
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
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
        // Set initial state
        circuit.vertices.forEach { if (it.input) pinLayout.pinsByNameAndIndex[it.nameAndIndex]?.second?.single()?.isBlockPowered?.let { value -> it.value = value } }
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
            circuit[it.name to it.index]?.let {
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
            circuit[it.name to it.index]?.let {
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
                if (it.output) {
                    val value = it.evaluate()
                    pinLayout.pinsByNameAndIndex[it.nameAndIndex]?.let {
                        when (it.first) {
                            PinLayout.Pin.Type.Self -> it.second.forEach { it.type = if (value) Material.REDSTONE_BLOCK else Material.WHITE_CONCRETE }
                            PinLayout.Pin.Type.Neighbor -> it.second.forEach {
                                horizontalNeighborFaces.forEach { face ->
                                    val block = it.getRelative(face)
                                    when (val blockData = block.blockData) {
                                        is AnaloguePowerable -> {
                                            blockData.power = if (value) blockData.maximumPower else 0
                                            block.blockData = blockData
                                        }

                                        is Lightable -> {
                                            blockData.isLit = value
                                            block.blockData = blockData
                                        }

                                        is Openable -> {
                                            blockData.isOpen
                                            block.blockData = blockData
                                        }

                                        is Powerable -> {
                                            blockData.isPowered = value
                                            block.blockData = blockData
                                        }
                                    }
                                }
                            }

                            PinLayout.Pin.Type.Value -> it.second.forEach { it.type = if (value) Material.WHITE_CONCRETE else Material.BLACK_CONCRETE }
                        }
                    }
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

    companion object {
        private val horizontalNeighborFaces = mutableListOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
    }
}
