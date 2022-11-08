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

package com.valaphee.redsynth.yosys

import com.fasterxml.jackson.annotation.JsonProperty

data class Netlist(
    @JsonProperty("creator") val creator: String,
    @JsonProperty("modules") val modules: Map<String, Module>
) {
    data class Module(
        @JsonProperty("attributes") val attributes: Map<String, String>,
        @JsonProperty("ports") val ports: Map<String, Port>,
        @JsonProperty("cells") val cells: Map<String, Cell>,
        @JsonProperty("netnames") val netnames: Map<String, Netname>
    ) {
        enum class Direction {
            @JsonProperty("input") Input,
            @JsonProperty("output") Output
        }

        data class Port(
            @JsonProperty("direction") val direction: Direction,
            @JsonProperty("bits") val bits: List<Int>
        )

        data class Cell(
            @JsonProperty("hide_name") val hideName: Int,
            @JsonProperty("type") val type: String,
            @JsonProperty("parameters") val parameters: Map<String, Any>,
            @JsonProperty("attributes") val attributes: Map<String, String>,
            @JsonProperty("port_directions") val portDirections: Map<String, Direction>,
            @JsonProperty("connections") val connections: Map<String, List<Any>>
        )

        data class Netname(
            @JsonProperty("hide_name") val hideName: Int,
            @JsonProperty("bits") val bits: List<Int>,
            @JsonProperty("attributes") val attributes: Map<String, String>
        )
    }
}
