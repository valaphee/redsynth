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

package com.valaphee.redsynth.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.bukkit.Bukkit
import org.bukkit.Location

class BlockLocationSerializer : JsonSerializer<Location>() {
    override fun serialize(value: Location, generator: JsonGenerator, provider: SerializerProvider) {
        generator.writeStartObject()
        generator.writeStringField("world", value.world.name)
        generator.writeNumberField("x", value.blockX)
        generator.writeNumberField("y", value.blockY)
        generator.writeNumberField("z", value.blockZ)
        generator.writeEndObject()
    }
}

class BlockLocationDeserializer : JsonDeserializer<Location>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Location {
        val node = parser.codec.readTree<JsonNode>(parser)
        return Location(Bukkit.getWorld(node.get("world").asText()), node.get("x").asInt().toDouble(), node.get("y").asInt().toDouble(), node.get("z").asInt().toDouble())
    }
}
