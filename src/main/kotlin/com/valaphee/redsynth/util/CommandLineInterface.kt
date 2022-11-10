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

open class CommandLineInterface(
    process: Process,
    private val prompt: String
) {
    private val stdout = process.inputStream
    private val stderr = process.errorStream
    private val stdin = process.outputStream

    init {
        readUntilPrompt()
    }

    fun execute(command: String): String {
        stdin.write(command.toByteArray())
        stdin.write('\n'.code)
        stdin.flush()

        return readUntilPrompt()
    }

    private fun readUntilPrompt(): String {
        val buffer = StringBuffer()
        var value = stdout.read()
        while (value != -1 && !buffer.append(value.toChar()).takeLast(prompt.length).contentEquals(prompt)) {
            value = stdout.read()
        }
        if (value == -1) {
            throw CommandLineException(stderr.readAllBytes().decodeToString().replace('\n', ' ').trim())
        }
        return buffer.substring(buffer.indexOf('\n'), buffer.length - prompt.length).toString()
    }
}
