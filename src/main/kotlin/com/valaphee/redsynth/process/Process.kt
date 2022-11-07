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

package com.valaphee.redsynth.process

import com.valaphee.redsynth.layout.Layout
import com.valaphee.redsynth.tree.Expression
import com.valaphee.redsynth.tree.Module
import com.valaphee.redsynth.tree.Operator
import com.valaphee.redsynth.tree.Port
import org.bukkit.scheduler.BukkitTask

class Process(
    val module: Module,
    val layout: Layout
) : Runnable {
    private val values = mutableMapOf<String, Boolean>()

    lateinit var task: BukkitTask
    private var update = true

    operator fun set(name: String, value: Boolean) {
        if (values[name] != value) {
            values[name] = value
            update = true
        }
    }

    override fun run() {
        if (update) {
            update = false
        }
    }

    private fun Expression.evaluate(): Boolean = when (this) {
        is Port -> values[name] ?: false
        is Operator -> {
            when (type) {
                Operator.Type.Not -> inputs[0].evaluate()
                Operator.Type.And -> inputs[0].evaluate() and inputs[1].evaluate()
                Operator.Type.Or -> inputs[0].evaluate() or inputs[1].evaluate()
                Operator.Type.Xor -> inputs[0].evaluate() xor inputs[1].evaluate()
            }
        }
    }
}
