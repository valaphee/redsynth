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

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.valaphee.redsynth.tree.Grammar

fun main() {
    val verilog = """
            module pr_en();
              input [7:0] a;
              input [7:0] b;
              input [7:0] c;
              input [7:0] d;
              input [1:0] sel;
              output [7:0] out;

              always @ (a or b or c or d or sel) begin  
                if (sel == 2'b00)  
                  out <= a;  
                else if (sel == 2'b01)  
                  out <= b;  
                else if (sel == 2'b10)  
                   out <= c;  
                else  
                   out <= d;  
              end
            endmodule
    """.trimIndent()
    println(Grammar.parseToEnd(verilog))
}
