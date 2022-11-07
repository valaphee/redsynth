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

package com.valaphee.redsynth.parse

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
    private val Colon by literalToken(":")
    private val `Left Square Bracket` by literalToken("[")
    private val `Right Square Bracket` by literalToken("]")
    private val `Left Parenthesis` by literalToken("(")
    private val `Right Parenthesis` by literalToken(")")
    private val Not by literalToken("~")
    private val And by literalToken("&")
    private val Xor by literalToken("^")
    private val Or by literalToken("|")
    private val Equal by literalToken("=")
    private val Separator by literalToken(",")
    private val `Statement Separator` by literalToken(";")
    // Keywords
    private val Module by literalToken("module")
    private val `End Module` by literalToken("endmodule")
    private val Input by literalToken("input")
    private val Output by literalToken("output")
    private val Wire by literalToken("wire")
    private val Assign by literalToken("assign")
    // Literals
    private val Number by regexToken("\\d+")
    private val Identifier by regexToken("[A-Za-z][\\w$]*")
    // Other
    private val Whitespace by regexToken("\\s+", ignore = true)
    private val Newline by regexToken("[\r\n]+", ignore = true)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Grammar
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private val BitSelect by (-`Left Square Bracket` * Number * -`Right Square Bracket`) map { it.text.toInt()..it.text.toInt() }
    private val PartSelect by (-`Left Square Bracket` * Number * -Colon * Number * -`Right Square Bracket`) map { (begin, end) -> end.text.toInt()..begin.text.toInt() }

    private val `Wire Reference` by Identifier * optional(BitSelect or PartSelect) map { (name, range) -> Wire(name.text, range ?: 0..0) }

    private val Negation by -Not * parser(this::Term) map { Operator(Operator.Type.Not, listOf(it)) }
    private val Parentheses by -`Left Parenthesis` * parser(this::`And Chain`) * -`Right Parenthesis`
    private val Term: Parser<Expression> by `Wire Reference` or Negation or Parentheses
    private val `Or Chain` by leftAssociative(Term, Or) { a, _, b -> Operator(Operator.Type.Or, listOf(a, b)) }
    private val `Xor Chain` by leftAssociative(`Or Chain`, Xor) { a, _, b -> Operator(Operator.Type.Xor, listOf(a, b)) }
    private val `And Chain` by leftAssociative(`Xor Chain`, And) { a, _, b -> Operator(Operator.Type.And, listOf(a, b)) }

    private val `Wire Declaration` by (-Wire * optional(PartSelect) * separatedTerms(Identifier, Separator)) map { (range, wires) -> WireDeclaration(WireDeclaration.Type.Wire, wires.map { Wire(it.text, range ?: 0..0) }) }
    private val `Input Declaration` by (-Input * optional(PartSelect) * separatedTerms(Identifier, Separator)) map { (range, inputs) -> WireDeclaration(WireDeclaration.Type.Input, inputs.map { Wire(it.text, range ?: 0..0) }) }
    private val `Output Declaration` by (-Output * optional(PartSelect) * separatedTerms(Identifier, Separator)) map { (range, outputs) -> WireDeclaration(WireDeclaration.Type.Output, outputs.map { Wire(it.text, range ?: 0..0) }) }
    private val Assignment by (-Assign * `Wire Reference` * -Equal * `And Chain`) map { (output, input) -> Assignment(output, input) }
    private val Statement by `Wire Declaration` or `Input Declaration` or `Output Declaration` or Assignment
    private val `Statement Chain` by separatedTerms(Statement, `Statement Separator`, acceptZero = true)

    private val `Module Declaration` by (-Module * Identifier * -`Left Parenthesis` * separatedTerms(Identifier, Separator, acceptZero = true) * -`Right Parenthesis` * `Statement Chain` * -`End Module`) map { (name, _, body) ->
        val wireDeclarations = body.filterIsInstance<WireDeclaration>().groupBy { it.type }
        Module(name.text, wireDeclarations[WireDeclaration.Type.Input]?.flatMap { it.wires } ?: emptyList(), wireDeclarations[WireDeclaration.Type.Output]?.flatMap { it.wires } ?: emptyList(), wireDeclarations[WireDeclaration.Type.Wire]?.flatMap { it.wires } ?: emptyList(), body.filterIsInstance<Assignment>())
    }

    override val rootParser = `Module Declaration`

    private class WireDeclaration(
        val type: Type,
        val wires: List<Wire>
    ) {
        enum class Type {
            Input, Output, Wire
        }
    }
}
