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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin

class RedSynth : JavaPlugin(), Listener {
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
        event.clickedBlock?.let { 
            val blockData = it.blockData
            if (blockData is WallSign) {
                val blockState = it.state as Sign
                if (PlainTextComponentSerializer.plainText().serialize(blockState.line(0)).equals("[interface]", ignoreCase = true)) {
                    blockState.line(0, LegacyComponentSerializer.legacySection().deserialize("§1[Interface]"))
                    blockState.update()
                }
            }
        }
    }
}
