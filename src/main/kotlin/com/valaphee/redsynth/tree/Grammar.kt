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

package com.valaphee.redsynth.tree

import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

@Suppress("ObjectPropertyName")
object Grammar : Grammar<Module>() {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Token
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Symbols
    private val `Left Parenthesis` by literalToken("(")
    private val Separator by literalToken(",")
    private val `Right Parenthesis` by literalToken(")")
    private val Semicolon by literalToken(";")
    private val `Left Square Bracket` by literalToken("[")
    private val Colon by literalToken(":")
    private val `Right Square Bracket` by literalToken("]")
    private val Equal by literalToken("=")
    private val Not by literalToken("~")
    private val And by literalToken("&")
    private val Xor by literalToken("^")
    private val Or by literalToken("|")
    // Keywords
    private val `Module Keyword` by literalToken("module")
    private val `End Module Keyword` by literalToken("endmodule")
    private val `Input Keyword` by literalToken("input")
    private val `Output Keyword` by literalToken("output")
    private val `Wire Keyword` by literalToken("wire")
    private val `Assign Keyword` by literalToken("assign")
    // Other
    private val Number by regexToken("\\d+")
    private val Identifier by regexToken("[A-Za-z_]\\w*")
    private val Whitespace by regexToken("\\s+", ignore = true)
    private val Newline by regexToken("[\r\n]+", ignore = true)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Token
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private val Range by (-`Left Square Bracket` * Number * -Colon * Number * -`Right Square Bracket`) map { (begin, end) -> end.text.toInt()..begin.text.toInt() }

    private val Primary: Parser<Expression> by ((Identifier * optional((-`Left Square Bracket` * Number * -`Right Square Bracket`) map { it.text.toInt()..it.text.toInt() } or Range)) map { (name, range) -> Port(name.text, range ?: 0..0) }) or (-`Left Parenthesis` * parser(this::Expression) * -`Right Parenthesis`)
    private val Expression by leftAssociative(Primary or -Not * Primary map { Operator(Operator.Type.Not, listOf(it)) }, And or Or or Xor) { a, _, b -> Operator(Operator.Type.Or, listOf(a, b)) }

    private val Assignment by (Identifier * optional((-`Left Square Bracket` * Number * -`Right Square Bracket`) map { it.text.toInt()..it.text.toInt() } or Range) * -Equal * Expression) map { (name, range, expression) -> Assignment(Port(name.text, range ?: 0..0), expression) }

    private val `Input Declaration` by (-`Input Keyword` * optional(Range) * separatedTerms(Identifier, Separator)) map { (range, inputs) -> Declaration(Declaration.Type.Input, inputs.map { Port(it.text, range ?: 0..0) }) }
    private val `Output Declaration` by (-`Output Keyword` * optional(Range) * separatedTerms(Identifier, Separator)) map { (range, outputs) -> Declaration(Declaration.Type.Output, outputs.map { Port(it.text, range ?: 0..0) }) }
    private val `Wire Declaration` by (-`Wire Keyword` * optional(Range) * separatedTerms(Identifier, Separator)) map { (range, wires) -> Declaration(Declaration.Type.Wire, wires.map { Port(it.text, range ?: 0..0) }) }
    private val `Continuous Assign` by (-`Assign Keyword` * Assignment)

    private val `Module Item` by `Input Declaration` or `Output Declaration` or `Wire Declaration` or `Continuous Assign`
    private val `Module Items` by separatedTerms(`Module Item`, Semicolon, acceptZero = true) * -Semicolon

    private val `List of Ports` by optional(-`Left Parenthesis` * separatedTerms(Identifier, Separator, acceptZero = true) * -`Right Parenthesis`)
    private val Module by (-`Module Keyword` * Identifier * `List of Ports` * -Semicolon * `Module Items` * -`End Module Keyword`) map { (name, _, body) ->
        val declarations = body.filterIsInstance<Declaration>().groupBy { it.type }

        Module(
            name.text,
            declarations[Declaration.Type.Input]?.flatMap { it.ports } ?: emptyList(),
            declarations[Declaration.Type.Output]?.flatMap { it.ports } ?: emptyList(),
            declarations[Declaration.Type.Wire]?.flatMap { it.ports } ?: emptyList(),
            body.filterIsInstance<Assignment>()
        )
    }

    override val rootParser = Module

    private class Declaration(
        val type: Type,
        val ports: List<Port>
    ) {
        enum class Type {
            Input, Output, Wire
        }
    }
}
