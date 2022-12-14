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

    val pins: Map<Block, Pin>
    val pinsByNameAndIndex: Map<Pair<String, Int>, Pair<Pin.Type, List<Block>>>

    init {
        val pins = mutableMapOf<Block, Pin>()

        val visitedBlocks = mutableSetOf<Block>()
        val blocksToVisit = mutableListOf(block)
        while (blocksToVisit.isNotEmpty()) {
            @Suppress("NAME_SHADOWING") val block = blocksToVisit.removeFirst()
            if (visitedBlocks.contains(block)) {
                continue
            }

            neighborFaces.forEach {
                val neighborBlock = block.getRelative(it)
                val neighborBlockData = neighborBlock.blockData
                if (neighborBlockData is WallSign) {
                    val neighborBlockState = neighborBlock.state as Sign
                    when (PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(0))) {
                        "^" -> neighborBlock.getRelative(neighborBlockData.facing.oppositeFace).getRelative(0, 1, 0)
                        "°" -> neighborBlock.getRelative(neighborBlockData.facing.oppositeFace)
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
                    }?.let {
                        // Check if the sign is describing a pin block
                        if (it.type == Material.WHITE_CONCRETE) {
                            pins[it] = Pin(Pin.Type[PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(3))], PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(1)), (PlainTextComponentSerializer.plainText().serialize(neighborBlockState.line(2)).toIntOrNull() ?: 0))
                        }
                    }
                } else when (neighborBlock.type) {
                    Material.WHITE_CONCRETE, Material.LIGHT_GRAY_CONCRETE, Material.BLACK_CONCRETE, Material.REDSTONE_BLOCK -> blocksToVisit.add(neighborBlock)
                    else -> Unit
                }
            }

            boundingBox.add(block.x, block.y, block.z)

            visitedBlocks += block
        }

        this.pins = pins
        pinsByNameAndIndex = pins.entries.groupBy { it.value.name to it.value.index }.mapValues { it.value.first().value.type to it.value.map { it.key } }
    }

    class Pin(
        val type: Type,
        val name: String,
        val index: Int
    ) {
        enum class Type(
            val key: String
        ) {
            Self("self"), Neighbor("neighbor"), Value("value");

            companion object {
                private val byKey = values().associateBy { it.key }

                operator fun get(key: String) = byKey[key] ?: Self
            }
        }
    }

    companion object {
        private val neighborFaces = mutableListOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN)
    }
}
