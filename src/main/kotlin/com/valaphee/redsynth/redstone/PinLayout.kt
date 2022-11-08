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

import com.valaphee.redsynth.util.BoundingBox
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign

class PinLayout(
    block: Block
) {
    val boundingBox = BoundingBox()

    val pins: Map<Block, String>
    val pinsByName: Map<String, List<Block>>

    init {
        val pins = mutableMapOf<Block, String>()

        val visitedBlocks = mutableSetOf<Block>()
        val blocksToVisit = mutableListOf(block)
        while (blocksToVisit.isNotEmpty()) {
            @Suppress("NAME_SHADOWING") val block = blocksToVisit.removeFirst()
            if (visitedBlocks.contains(block)) {
                continue
            }

            BlockFace.values().forEach {
                val neighborBlock = block.getRelative(it)
                val neighborBlockData = neighborBlock.blockData
                if (neighborBlockData is WallSign) {
                    val neighborBlockState = neighborBlock.state as Sign
                    when (PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(0))) {
                        "^" -> neighborBlock.getRelative(neighborBlockData.facing.oppositeFace).getRelative(0, 1, 0)
                        "v" -> neighborBlock.getRelative(neighborBlockData.facing.oppositeFace).getRelative(0, -1, 0)
                        "<" -> when (neighborBlockData.facing.oppositeFace) {
                            BlockFace.NORTH -> neighborBlock.getRelative(-1, 0, -1)
                            BlockFace.EAST -> neighborBlock.getRelative(1, 0, -1)
                            BlockFace.SOUTH -> neighborBlock.getRelative(1, 0, 1)
                            BlockFace.WEST -> neighborBlock.getRelative(-1, 0, 1)
                            else -> null
                        }
                        ">" -> when (neighborBlockData.facing.oppositeFace) {
                            BlockFace.NORTH -> neighborBlock.getRelative(1, 0, -1)
                            BlockFace.EAST -> neighborBlock.getRelative(1, 0, 1)
                            BlockFace.SOUTH -> neighborBlock.getRelative(-1, 0, 1)
                            BlockFace.WEST -> neighborBlock.getRelative(-1, 0, -1)
                            else -> null
                        }
                        else -> null
                    }?.let { pins[it] = PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(1)) }
                } else when (neighborBlock.type) {
                    Material.WHITE_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.BLACK_CONCRETE, Material.REDSTONE_BLOCK -> blocksToVisit.add(neighborBlock)
                    else -> Unit
                }
            }

            boundingBox.add(block.x, block.y, block.z)

            visitedBlocks += block
        }

        this.pins = pins
        pinsByName = pins.entries.groupBy { it.value }.mapValues { it.value.map { it.key } }
    }
}
