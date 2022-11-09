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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.redsynth.logic.Yosys
import com.valaphee.redsynth.redstone.Circuit
import com.valaphee.redsynth.redstone.PinLayout
import com.valaphee.redsynth.redstone.Simulation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockRedstoneEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RedSynth : JavaPlugin(), Listener {
    private lateinit var config: RedSynthConfig

    private val simulations = mutableMapOf<Block, Simulation>()

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdir()

        val configFile = File(dataFolder, "config.json")
        config = if (!configFile.exists()) RedSynthConfig().apply { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValue(configFile, this) } else jacksonObjectMapper().readValue(configFile)

        server.pluginManager.registerEvents(this, this)

        config.simulations.forEach { toggleSimulation(server.consoleSender, it.block) }
    }

    override fun onDisable() {
        jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValue(File(dataFolder, "config.json"), config)
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        event.clickedBlock?.let { toggleSimulation(event.player, it) }
    }

    @EventHandler
    fun on(event: BlockRedstoneEvent) {
        if ((event.oldCurrent != 0).xor(event.newCurrent != 0)) {
            val block = event.block
            simulations.values.find { it.pinLayout.boundingBox.contains(block.x, block.y, block.z) }?.on(event)
        }
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val block = event.block
        simulations.values.find { it.pinLayout.boundingBox.contains(block.x, block.y, block.z) }?.on(event)
    }

    private fun toggleSimulation(sender: CommandSender, block: Block) {
        val blockData = block.blockData
        if (blockData is WallSign) {
            val blockState = block.state as Sign
            if (PlainTextComponentSerializer.plainText().serialize(blockState.line(0)).equals("[RedSynth]", ignoreCase = true)) {
                blockState.line(0, LegacyComponentSerializer.legacySection().deserialize("§1[RedSynth]"))
                if (!simulations.contains(block)) {
                    try {
                        val simulation = Simulation(this, PinLayout(block.getRelative(blockData.facing.oppositeFace)), Circuit(Yosys(File(dataFolder, PlainTextComponentSerializer.plainText().serialize(blockState.line(1))).path).synthesis().netlist().modules.values.single()))
                        simulations[block] = simulation
                        simulation.start()

                        blockState.line(3, LegacyComponentSerializer.legacySection().deserialize("§2Running"))

                        config.simulations += block.location
                    } catch (ex: Exception) {
                        sender.sendMessage(ex.message!!)
                        blockState.line(3, LegacyComponentSerializer.legacySection().deserialize("§4Failure"))
                    }
                } else simulations.remove(block)?.let {
                    config.simulations -= block.location

                    it.stop()

                    blockState.line(3, Component.empty())
                }
                blockState.update()
            }
        }
    }
}
