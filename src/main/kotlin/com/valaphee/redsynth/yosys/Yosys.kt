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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class Yosys(
    path: String
) {
    private val process = ProcessBuilder("yosys").start()
    private val outputWriter = process.outputWriter()
    private val inputStream = process.inputStream

    init {
        readPrompt()
        execute("read_verilog $path")
        execute("hierarchy -check; proc; opt; fsm; opt; memory; opt; techmap; opt")
    }

    val netlist by lazy { jacksonObjectMapper().readValue<Netlist>(execute("json")) }

    fun evaluate(values: Map<String, Any>) {
        execute("eval ${values.entries.joinToString(" ") { "-set ${it.key} ${it.value}" }}")
    }

    private fun execute(command: String): String {
        outputWriter.write("$command\n")
        outputWriter.flush()
        return readPrompt()
    }

    private fun readPrompt(): String {
        val buffer = StringBuffer()
        var value = inputStream.read()
        while (value != -1) {
            value = if (!buffer.append(value.toChar()).takeLast(prompt.length).contentEquals(prompt)) inputStream.read() else -1
        }
        return buffer.substring(buffer.indexOf('\n'), buffer.length - prompt.length).toString()
    }

    companion object {
        private const val prompt = "yosys> "
    }
}
