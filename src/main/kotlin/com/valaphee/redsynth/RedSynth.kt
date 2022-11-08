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

package com.valaphee.redsynth

import com.valaphee.redsynth.layout.Interface
import com.valaphee.redsynth.util.BoundingBox
import com.valaphee.redsynth.yosys.Yosys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.RedstoneWire
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class RedSynth : JavaPlugin(), Listener {
    private val simulations = mutableMapOf<Block, Simulation>()

    override fun onLoad() {
        dataFolder.mkdir()
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val player = event.player

        event.clickedBlock?.let { 
            val blockData = it.blockData
            if (blockData is WallSign) {
                val blockState = it.state as Sign
                if (PlainTextComponentSerializer.plainText().serialize(blockState.line(0)).equals("[red synth]", ignoreCase = true)) {
                    blockState.line(0, LegacyComponentSerializer.legacySection().deserialize("§1[Red Synth]"))
                    if (!simulations.contains(it)) {
                        try {
                            val simulation = Simulation(Yosys(PlainTextComponentSerializer.plainText().serialize(blockState.line(1))), Interface(it.getRelative(blockData.facing.oppositeFace)))
                            simulations[it] = simulation
                            simulation.task = server.scheduler.runTaskTimer(this, simulation, 0, 1)
                            blockState.line(3, LegacyComponentSerializer.legacySection().deserialize("§2Running"))
                        } catch (ex: Exception) {
                            player.sendMessage(ex.message!!)
                            blockState.line(3, LegacyComponentSerializer.legacySection().deserialize("§4Failure"))
                        }
                    } else simulations.remove(it)?.let {
                        server.scheduler.cancelTask(it.task.taskId)
                        blockState.line(3, Component.empty())
                    }
                    blockState.update()
                }
            }
        }
    }

    @EventHandler
    fun on(event: BlockRedstoneEvent) {
        val block = event.block
        val boundingBox = BoundingBox(block.x - 1, block.y, block.z - 1, block.x + 1, block.y, block.z + 1)
        for (simulation in simulations.values) {
            if (!simulation.`interface`.boundingBox.intersects(boundingBox)) {
                continue
            }

            when (val blockData = block.blockData) {
                is Directional -> simulation.`interface`.ports[block.getRelative(blockData.facing.oppositeFace)]
                is RedstoneWire -> blockData.allowedFaces.firstNotNullOfOrNull { if (blockData.getFace(it) == RedstoneWire.Connection.SIDE) simulation.`interface`.ports[block.getRelative(it)] else null }
                else -> null
            }?.let { simulation[it] = event.newCurrent != 0 }
        }
    }
}
