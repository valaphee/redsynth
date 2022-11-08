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

package com.valaphee.redsynth.logic

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.redsynth.util.CommandLineInterface

class Yosys(
    path: String
) : CommandLineInterface(ProcessBuilder("yosys").start(), "yosys> ") {
    init {
        execute("read_verilog $path")
    }

    fun synthesis() = apply { execute("hierarchy -check; proc; flatten; opt_expr; opt_clean; check; opt -nodffe -nosdff; fsm; opt; wreduce; peepopt; opt_clean; alumacc; share; opt; memory -nomap; opt_clean; opt -fast -full; memory_map; opt -full; techmap; opt -fast; abc -fast -g OR; opt -fast; hierarchy -check; check") }

    fun netlist() = jacksonObjectMapper().readValue<Netlist>(execute("json"))
}
